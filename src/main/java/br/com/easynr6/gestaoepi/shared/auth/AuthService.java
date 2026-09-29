package br.com.easynr6.gestaoepi.shared.auth;

import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "easy-nr6.auth.provider", havingValue = "jdbc", matchIfMissing = true)
public class AuthService implements AuthenticationProvider, CredentialManager {

  private static final String SELECT_USUARIO_SQL =
      """
      SELECT id, nome, login, senha_hash, ativo, credencial_troca_obrigatoria, tentativas_invalidas, bloqueado_ate
      FROM usuario
      WHERE login = ?
      """;

  private static final String SELECT_PAPEIS_SQL =
      """
      SELECT p.codigo
      FROM papel p
      JOIN usuario_papel up ON up.papel_id = p.id
      WHERE up.usuario_id = ?
      """;
  private static final String UPDATE_FALHA_AUTENTICACAO_SQL =
      """
      UPDATE usuario
      SET tentativas_invalidas = ?,
          bloqueado_ate = ?
      WHERE id = ?
      """;
  private static final String UPDATE_SUCESSO_AUTENTICACAO_SQL =
      """
      UPDATE usuario
      SET tentativas_invalidas = 0,
          bloqueado_ate = NULL
      WHERE id = ?
      """;
  private static final String UPDATE_CREDENCIAL_SQL =
      """
      UPDATE usuario
      SET senha_hash = ?,
          credencial_troca_obrigatoria = 0,
          tentativas_invalidas = 0,
          bloqueado_ate = NULL,
          credencial_atualizada_em = CURRENT_TIMESTAMP
      WHERE id = ?
      """;
  private static final int MAX_TENTATIVAS_INVALIDAS = 5;
  private static final int BLOQUEIO_MINUTOS = 15;

  private final JdbcTemplate jdbcTemplate;
  private final PasswordEncoder passwordEncoder;
  private final AuditTrail auditTrail;
  private final PasswordPolicy passwordPolicy;

  public AuthService(
      JdbcTemplate jdbcTemplate,
      PasswordEncoder passwordEncoder,
      AuditTrail auditTrail,
      PasswordPolicy passwordPolicy) {
    this.jdbcTemplate = jdbcTemplate;
    this.passwordEncoder = passwordEncoder;
    this.auditTrail = auditTrail;
    this.passwordPolicy = passwordPolicy;
  }

  @Override
  public AuthenticationResult autenticar(String login, String senha) {
    String loginNormalizado = login == null ? "" : login.trim();
    List<UsuarioRow> usuarios =
        jdbcTemplate.query(SELECT_USUARIO_SQL, usuarioRowMapper(), loginNormalizado);
    if (usuarios.isEmpty()) {
      return AuthenticationResult.denied(AuthenticationStatus.INVALID_CREDENTIAL);
    }

    UsuarioRow usuario = usuarios.get(0);
    if (!usuario.ativo()) {
      return AuthenticationResult.denied(AuthenticationStatus.INACTIVE_USER);
    }
    LocalDateTime agora = LocalDateTime.now();
    if (usuario.bloqueadoAte() != null && usuario.bloqueadoAte().isAfter(agora)) {
      auditTrail.registrarEventoCritico(
          usuario.id(),
          "LOGIN_BLOQUEADO",
          "USUARIO",
          String.valueOf(usuario.id()),
          "Tentativa de login em conta bloqueada");
      return AuthenticationResult.blocked(usuario.bloqueadoAte());
    }

    if (!passwordEncoder.matches(senha == null ? "" : senha, usuario.senhaHash())) {
      int tentativas = usuario.tentativasInvalidas() + 1;
      if (tentativas >= MAX_TENTATIVAS_INVALIDAS) {
        LocalDateTime bloqueadoAte = agora.plusMinutes(BLOQUEIO_MINUTOS);
        jdbcTemplate.update(
            UPDATE_FALHA_AUTENTICACAO_SQL, 0, bloqueadoAte.toString(), usuario.id());
        auditTrail.registrarEventoCritico(
            usuario.id(),
            "LOGIN_BLOQUEIO_TEMPORARIO",
            "USUARIO",
            String.valueOf(usuario.id()),
            "Conta bloqueada por tentativas invalidas consecutivas");
        return AuthenticationResult.blocked(bloqueadoAte);
      }

      jdbcTemplate.update(UPDATE_FALHA_AUTENTICACAO_SQL, tentativas, null, usuario.id());
      auditTrail.registrarEventoCritico(
          usuario.id(),
          "LOGIN_FALHA_CREDENCIAL",
          "USUARIO",
          String.valueOf(usuario.id()),
          "Credencial invalida");
      return AuthenticationResult.denied(AuthenticationStatus.INVALID_CREDENTIAL);
    }

    jdbcTemplate.update(UPDATE_SUCESSO_AUTENTICACAO_SQL, usuario.id());
    EnumSet<Papel> papeis = carregarPapeis(usuario.id());
    if (papeis.isEmpty()) {
      return AuthenticationResult.denied(AuthenticationStatus.NO_ROLE);
    }

    UsuarioAutenticado autenticado =
        new UsuarioAutenticado(usuario.id(), usuario.nome(), usuario.login(), papeis);
    if (usuario.credencialTrocaObrigatoria()) {
      return AuthenticationResult.forcePasswordChange(autenticado);
    }
    return AuthenticationResult.success(autenticado);
  }

  @Override
  public void alterarCredencialObrigatoria(Long usuarioId, String login, String novaSenha) {
    passwordPolicy.validar(login, novaSenha);
    String novaSenhaHash = passwordEncoder.encode(novaSenha);
    jdbcTemplate.update(UPDATE_CREDENCIAL_SQL, novaSenhaHash, usuarioId);
    auditTrail.registrarEventoCritico(
        usuarioId,
        "CREDENCIAL_TROCADA",
        "USUARIO",
        String.valueOf(usuarioId),
        "Troca obrigatoria de credencial concluida");
  }

  private EnumSet<Papel> carregarPapeis(Long usuarioId) {
    EnumSet<Papel> papeis = EnumSet.noneOf(Papel.class);
    List<String> rows = jdbcTemplate.queryForList(SELECT_PAPEIS_SQL, String.class, usuarioId);
    for (String codigo : rows) {
      papeis.add(Papel.valueOf(codigo));
    }
    return papeis;
  }

  private RowMapper<UsuarioRow> usuarioRowMapper() {
    return (rs, rowNum) -> mapUsuario(rs);
  }

  private UsuarioRow mapUsuario(ResultSet rs) throws SQLException {
    return new UsuarioRow(
        rs.getLong("id"),
        rs.getString("nome"),
        rs.getString("login"),
        rs.getString("senha_hash"),
        rs.getInt("ativo") == 1,
        rs.getInt("credencial_troca_obrigatoria") == 1,
        rs.getInt("tentativas_invalidas"),
        parseDateTime(rs.getString("bloqueado_ate")));
  }

  private static LocalDateTime parseDateTime(String valor) {
    if (valor == null || valor.isBlank()) {
      return null;
    }
    return LocalDateTime.parse(valor);
  }

  private record UsuarioRow(
      Long id,
      String nome,
      String login,
      String senhaHash,
      boolean ativo,
      boolean credencialTrocaObrigatoria,
      int tentativasInvalidas,
      LocalDateTime bloqueadoAte) {}
}
