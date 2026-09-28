package br.com.easynr6.gestaoepi.shared.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private static final String SELECT_USUARIO_SQL =
      """
      SELECT id, nome, login, senha_hash
      FROM usuario
      WHERE login = ? AND ativo = 1
      """;

  private static final String SELECT_PAPEIS_SQL =
      """
      SELECT p.codigo
      FROM papel p
      JOIN usuario_papel up ON up.papel_id = p.id
      WHERE up.usuario_id = ?
      """;

  private final JdbcTemplate jdbcTemplate;
  private final PasswordEncoder passwordEncoder;

  public AuthService(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
    this.jdbcTemplate = jdbcTemplate;
    this.passwordEncoder = passwordEncoder;
  }

  public Optional<UsuarioAutenticado> autenticar(String login, String senha) {
    List<UsuarioRow> usuarios =
        jdbcTemplate.query(
            SELECT_USUARIO_SQL, usuarioRowMapper(), login == null ? "" : login.trim());
    if (usuarios.isEmpty()) {
      return Optional.empty();
    }

    UsuarioRow usuario = usuarios.get(0);
    if (!passwordEncoder.matches(senha == null ? "" : senha, usuario.senhaHash())) {
      return Optional.empty();
    }

    EnumSet<Papel> papeis = carregarPapeis(usuario.id());
    if (papeis.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(
        new UsuarioAutenticado(usuario.id(), usuario.nome(), usuario.login(), papeis));
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
        rs.getLong("id"), rs.getString("nome"), rs.getString("login"), rs.getString("senha_hash"));
  }

  private record UsuarioRow(Long id, String nome, String login, String senhaHash) {}
}
