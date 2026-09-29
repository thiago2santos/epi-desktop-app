package br.com.easynr6.gestaoepi.shared.auth;

import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import java.util.List;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdministrationService {

  private static final String SELECT_PAPEIS_BY_USUARIO_SQL =
      """
      SELECT p.codigo
      FROM papel p
      JOIN usuario_papel up ON up.papel_id = p.id
      WHERE up.usuario_id = :usuarioId
      """;
  private static final String SELECT_USUARIO_BY_LOGIN_SQL =
      """
      SELECT id, nome, login, ativo
      FROM usuario
      WHERE login = :login
      """;
  private static final String SELECT_USUARIOS_SQL =
      """
      SELECT u.id,
             u.nome,
             u.login,
             u.ativo,
             u.credencial_troca_obrigatoria,
             COALESCE(GROUP_CONCAT(p.codigo, ', '), '-') AS papeis
      FROM usuario u
      LEFT JOIN usuario_papel up ON up.usuario_id = u.id
      LEFT JOIN papel p ON p.id = up.papel_id
      GROUP BY u.id, u.nome, u.login, u.ativo, u.credencial_troca_obrigatoria
      ORDER BY u.id DESC
      """;
  private static final String UPDATE_USUARIO_SQL =
      """
      UPDATE usuario
      SET nome = :nome,
          login = :login,
          ativo = :ativo
      WHERE id = :usuarioId
      """;
  private static final String UPDATE_BLOQUEIO_USUARIO_SQL =
      """
      UPDATE usuario
      SET ativo = :ativo
      WHERE id = :usuarioId
      """;
  private static final String DELETE_USUARIO_PAPEIS_SQL =
      "DELETE FROM usuario_papel WHERE usuario_id = :usuarioId";
  private static final String DELETE_USUARIO_SQL = "DELETE FROM usuario WHERE id = :usuarioId";

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final PasswordEncoder passwordEncoder;
  private final PasswordPolicy passwordPolicy;
  private final AuditTrail auditTrail;

  public UserAdministrationService(
      NamedParameterJdbcTemplate jdbcTemplate,
      PasswordEncoder passwordEncoder,
      PasswordPolicy passwordPolicy,
      AuditTrail auditTrail) {
    this.jdbcTemplate = jdbcTemplate;
    this.passwordEncoder = passwordEncoder;
    this.passwordPolicy = passwordPolicy;
    this.auditTrail = auditTrail;
  }

  @Transactional
  public Long cadastrarUsuario(
      Long adminId,
      String nome,
      String login,
      String senhaInicial,
      boolean ativo,
      Set<Papel> papeis) {
    validarAdmin(adminId);
    validarCamposObrigatorios(nome, login);
    if (papeis == null || papeis.isEmpty()) {
      throw new IllegalArgumentException("AUTH-008 Defina ao menos um papel para o usuario.");
    }
    passwordPolicy.validar(login, senhaInicial);

    String loginNormalizado = login.trim();
    String senhaHash = passwordEncoder.encode(senhaInicial);
    try {
      jdbcTemplate.update(
          """
          INSERT INTO usuario (nome, login, senha_hash, ativo, credencial_troca_obrigatoria)
          VALUES (:nome, :login, :senhaHash, :ativo, 1)
          """,
          new MapSqlParameterSource()
              .addValue("nome", nome.trim())
              .addValue("login", loginNormalizado)
              .addValue("senhaHash", senhaHash)
              .addValue("ativo", ativo ? 1 : 0));
    } catch (DuplicateKeyException ex) {
      throw new DuplicateLoginException("AUTH-005 Login ja utilizado.", ex);
    }

    Long usuarioCriadoId =
        jdbcTemplate.queryForObject(
            "SELECT id FROM usuario WHERE login = :login",
            new MapSqlParameterSource().addValue("login", loginNormalizado),
            Long.class);

    if (usuarioCriadoId == null) {
      throw new IllegalStateException("Falha ao obter identificador do usuario criado.");
    }
    for (Papel papel : papeis) {
      vincularPapel(usuarioCriadoId, papel);
    }
    auditTrail.registrarEventoCritico(
        adminId,
        "USUARIO_CRIADO",
        "USUARIO",
        String.valueOf(usuarioCriadoId),
        "Cadastro de usuario com troca obrigatoria de credencial");
    return usuarioCriadoId;
  }

  @Transactional
  public void atribuirPapel(Long adminId, Long usuarioId, Papel papel) {
    validarAdmin(adminId);
    if (!usuarioExiste(usuarioId)) {
      throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
    }
    vincularPapel(usuarioId, papel);
    auditTrail.registrarEventoCritico(
        adminId,
        "PAPEL_ATRIBUIDO",
        "USUARIO",
        String.valueOf(usuarioId),
        "Papel atribuido: " + papel.name());
  }

  @Transactional
  public void resetarCredencial(Long adminId, Long usuarioId, String login, String novaSenha) {
    validarAdmin(adminId);
    if (!usuarioExiste(usuarioId)) {
      throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
    }
    passwordPolicy.validar(login, novaSenha);
    String novoHash = passwordEncoder.encode(novaSenha);
    jdbcTemplate.update(
        """
        UPDATE usuario
        SET senha_hash = :hash,
            credencial_troca_obrigatoria = 1,
            tentativas_invalidas = 0,
            bloqueado_ate = NULL
        WHERE id = :usuarioId
        """,
        new MapSqlParameterSource().addValue("hash", novoHash).addValue("usuarioId", usuarioId));
    auditTrail.registrarEventoCritico(
        adminId,
        "CREDENCIAL_RESETADA",
        "USUARIO",
        String.valueOf(usuarioId),
        "Reset administrativo de credencial");
  }

  @Transactional
  public void editarUsuario(
      Long adminId, Long usuarioId, String nome, String login, boolean ativo) {
    validarAdmin(adminId);
    if (!usuarioExiste(usuarioId)) {
      throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
    }
    validarCamposObrigatorios(nome, login);
    try {
      jdbcTemplate.update(
          UPDATE_USUARIO_SQL,
          new MapSqlParameterSource()
              .addValue("nome", nome.trim())
              .addValue("login", login.trim())
              .addValue("ativo", ativo ? 1 : 0)
              .addValue("usuarioId", usuarioId));
    } catch (DuplicateKeyException ex) {
      throw new DuplicateLoginException("AUTH-005 Login ja utilizado.", ex);
    }
    auditTrail.registrarEventoCritico(
        adminId,
        "USUARIO_EDITADO",
        "USUARIO",
        String.valueOf(usuarioId),
        "Cadastro basico do usuario atualizado");
  }

  @Transactional
  public void bloquearUsuario(Long adminId, Long usuarioId) {
    validarAdmin(adminId);
    if (!usuarioExiste(usuarioId)) {
      throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
    }
    jdbcTemplate.update(
        UPDATE_BLOQUEIO_USUARIO_SQL,
        new MapSqlParameterSource().addValue("ativo", 0).addValue("usuarioId", usuarioId));
    auditTrail.registrarEventoCritico(
        adminId,
        "USUARIO_BLOQUEADO",
        "USUARIO",
        String.valueOf(usuarioId),
        "Usuario bloqueado para autenticacao");
  }

  @Transactional
  public void reativarUsuario(Long adminId, Long usuarioId) {
    validarAdmin(adminId);
    if (!usuarioExiste(usuarioId)) {
      throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
    }
    jdbcTemplate.update(
        UPDATE_BLOQUEIO_USUARIO_SQL,
        new MapSqlParameterSource().addValue("ativo", 1).addValue("usuarioId", usuarioId));
    auditTrail.registrarEventoCritico(
        adminId,
        "USUARIO_REATIVADO",
        "USUARIO",
        String.valueOf(usuarioId),
        "Usuario reativado para autenticacao");
  }

  @Transactional
  public void excluirUsuario(Long adminId, Long usuarioId) {
    validarAdmin(adminId);
    if (adminId.equals(usuarioId)) {
      throw new IllegalArgumentException("AUTH-016 Nao e permitido excluir o proprio usuario.");
    }
    if (!usuarioExiste(usuarioId)) {
      throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
    }
    try {
      jdbcTemplate.update(
          DELETE_USUARIO_PAPEIS_SQL, new MapSqlParameterSource().addValue("usuarioId", usuarioId));
      int removidos =
          jdbcTemplate.update(
              DELETE_USUARIO_SQL, new MapSqlParameterSource().addValue("usuarioId", usuarioId));
      if (removidos == 0) {
        throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
      }
    } catch (DataIntegrityViolationException ex) {
      throw new IllegalStateException(
          "AUTH-015 Usuario possui historico vinculado e nao pode ser excluido.", ex);
    }
    auditTrail.registrarEventoCritico(
        adminId,
        "USUARIO_EXCLUIDO",
        "USUARIO",
        String.valueOf(usuarioId),
        "Usuario removido da base");
  }

  public void atribuirPapelPorLogin(Long adminId, String loginAlvo, Papel papel) {
    UsuarioAdminResumo usuario = buscarUsuarioPorLogin(loginAlvo);
    atribuirPapel(adminId, usuario.id(), papel);
  }

  public void resetarCredencialPorLogin(Long adminId, String loginAlvo, String novaSenha) {
    UsuarioAdminResumo usuario = buscarUsuarioPorLogin(loginAlvo);
    resetarCredencial(adminId, usuario.id(), usuario.login(), novaSenha);
  }

  public void editarUsuarioPorLogin(
      Long adminId, String loginAlvo, String novoNome, String novoLogin, boolean ativo) {
    UsuarioAdminResumo usuario = buscarUsuarioPorLogin(loginAlvo);
    editarUsuario(adminId, usuario.id(), novoNome, novoLogin, ativo);
  }

  public void bloquearUsuarioPorLogin(Long adminId, String loginAlvo) {
    UsuarioAdminResumo usuario = buscarUsuarioPorLogin(loginAlvo);
    bloquearUsuario(adminId, usuario.id());
  }

  public void reativarUsuarioPorLogin(Long adminId, String loginAlvo) {
    UsuarioAdminResumo usuario = buscarUsuarioPorLogin(loginAlvo);
    reativarUsuario(adminId, usuario.id());
  }

  public void excluirUsuarioPorLogin(Long adminId, String loginAlvo) {
    UsuarioAdminResumo usuario = buscarUsuarioPorLogin(loginAlvo);
    excluirUsuario(adminId, usuario.id());
  }

  public List<UsuarioAdminResumo> listarUsuarios() {
    return jdbcTemplate.query(
        SELECT_USUARIOS_SQL,
        (rs, rowNum) ->
            new UsuarioAdminResumo(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("login"),
                rs.getInt("ativo") == 1,
                rs.getInt("credencial_troca_obrigatoria") == 1,
                rs.getString("papeis")));
  }

  private void validarAdmin(Long adminId) {
    List<String> papeis =
        jdbcTemplate.queryForList(
            SELECT_PAPEIS_BY_USUARIO_SQL,
            new MapSqlParameterSource().addValue("usuarioId", adminId),
            String.class);
    if (!papeis.contains(Papel.ADMIN.name())) {
      throw new AuthorizationDeniedException(
          "AUTH-004 Voce nao tem permissao para executar esta acao.");
    }
  }

  private void validarCamposObrigatorios(String nome, String login) {
    if (nome == null || nome.isBlank() || login == null || login.isBlank()) {
      throw new IllegalArgumentException("AUTH-010 Nome e login sao obrigatorios.");
    }
  }

  private boolean usuarioExiste(Long usuarioId) {
    Integer quantidade =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM usuario WHERE id = :usuarioId",
            new MapSqlParameterSource().addValue("usuarioId", usuarioId),
            Integer.class);
    return quantidade != null && quantidade > 0;
  }

  private UsuarioAdminResumo buscarUsuarioPorLogin(String loginAlvo) {
    if (loginAlvo == null || loginAlvo.isBlank()) {
      throw new IllegalArgumentException("AUTH-011 Login alvo obrigatorio.");
    }
    List<UsuarioAdminResumo> rows =
        jdbcTemplate.query(
            SELECT_USUARIO_BY_LOGIN_SQL,
            new MapSqlParameterSource().addValue("login", loginAlvo.trim()),
            (rs, rowNum) ->
                new UsuarioAdminResumo(
                    rs.getLong("id"),
                    rs.getString("nome"),
                    rs.getString("login"),
                    rs.getInt("ativo") == 1,
                    false,
                    "-"));
    if (rows.isEmpty()) {
      throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
    }
    return rows.get(0);
  }

  private void vincularPapel(Long usuarioId, Papel papel) {
    jdbcTemplate.update(
        """
        INSERT OR IGNORE INTO usuario_papel (usuario_id, papel_id)
        VALUES (:usuarioId, (SELECT id FROM papel WHERE codigo = :codigo))
        """,
        new MapSqlParameterSource()
            .addValue("usuarioId", usuarioId)
            .addValue("codigo", papel.name()));
  }

  public record UsuarioAdminResumo(
      Long id, String nome, String login, boolean ativo, boolean trocaObrigatoria, String papeis) {}
}
