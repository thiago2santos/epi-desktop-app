package br.com.easynr6.gestaoepi.shared.auth;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "easy-nr6.bootstrap-admin.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class UserBootstrapService implements ApplicationRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserBootstrapService.class);
  private static final String DEFAULT_ADMIN_LOGIN = "admin";
  private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final PasswordEncoder passwordEncoder;

  public UserBootstrapService(
      NamedParameterJdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
    this.jdbcTemplate = jdbcTemplate;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    Integer quantidadeUsuarios =
        jdbcTemplate.queryForObject("SELECT COUNT(1) FROM usuario", Map.of(), Integer.class);
    if (quantidadeUsuarios != null && quantidadeUsuarios > 0) {
      return;
    }

    String senhaHash = passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD);
    MapSqlParameterSource usuarioParams =
        new MapSqlParameterSource()
            .addValue("nome", "Administrador")
            .addValue("login", DEFAULT_ADMIN_LOGIN)
            .addValue("senhaHash", senhaHash);

    jdbcTemplate.update(
        "INSERT INTO usuario (nome, login, senha_hash, ativo) VALUES (:nome, :login, :senhaHash, 1)",
        usuarioParams);

    Long usuarioId =
        jdbcTemplate.queryForObject(
            "SELECT id FROM usuario WHERE login = :login",
            Map.of("login", DEFAULT_ADMIN_LOGIN),
            Long.class);
    Long papelId =
        jdbcTemplate.queryForObject(
            "SELECT id FROM papel WHERE codigo = :codigo",
            Map.of("codigo", Papel.ADMIN.name()),
            Long.class);

    if (usuarioId != null && papelId != null) {
      jdbcTemplate.update(
          "INSERT INTO usuario_papel (usuario_id, papel_id) VALUES (:usuarioId, :papelId)",
          new MapSqlParameterSource()
              .addValue("usuarioId", usuarioId)
              .addValue("papelId", papelId));
      LOGGER.warn(
          "Usuario admin bootstrap criado: login='{}', senha='{}'. Altere imediatamente apos primeiro acesso.",
          DEFAULT_ADMIN_LOGIN,
          DEFAULT_ADMIN_PASSWORD);
    }
  }
}
