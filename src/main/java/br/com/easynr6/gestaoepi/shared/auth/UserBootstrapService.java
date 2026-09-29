package br.com.easynr6.gestaoepi.shared.auth;

import java.security.SecureRandom;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "easy-nr6.bootstrap-admin.enabled",
    havingValue = "true",
    matchIfMissing = true)
@ConditionalOnBean(AuthService.class)
@Order(10)
public class UserBootstrapService implements ApplicationRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserBootstrapService.class);
  private static final String DEFAULT_ADMIN_LOGIN = "admin";
  private static final String MODE_STRICT = "strict";
  private static final String MODE_DEV = "dev";
  private static final int TEMP_PASSWORD_LENGTH = 16;
  private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
  private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
  private static final String DIGITS = "23456789";
  private static final String SYMBOLS = "!@#$%*-_";
  private static final String ALL = UPPER + LOWER + DIGITS + SYMBOLS;

  private final NamedParameterJdbcTemplate jdbcTemplate;
  private final PasswordEncoder passwordEncoder;
  private final PasswordPolicy passwordPolicy;
  private final String bootstrapAdminMode;
  private final String bootstrapAdminPassword;
  private final SecureRandom secureRandom = new SecureRandom();

  public UserBootstrapService(
      NamedParameterJdbcTemplate jdbcTemplate,
      PasswordEncoder passwordEncoder,
      PasswordPolicy passwordPolicy,
      @Value("${easy-nr6.bootstrap-admin.mode:dev}") String bootstrapAdminMode,
      @Value("${easy-nr6.bootstrap-admin.password:}") String bootstrapAdminPassword) {
    this.jdbcTemplate = jdbcTemplate;
    this.passwordEncoder = passwordEncoder;
    this.passwordPolicy = passwordPolicy;
    this.bootstrapAdminMode = bootstrapAdminMode == null ? MODE_DEV : bootstrapAdminMode.trim();
    this.bootstrapAdminPassword = bootstrapAdminPassword;
  }

  @Override
  public void run(ApplicationArguments args) {
    Integer quantidadeUsuarios =
        jdbcTemplate.queryForObject("SELECT COUNT(1) FROM usuario", Map.of(), Integer.class);
    if (quantidadeUsuarios != null && quantidadeUsuarios > 0) {
      return;
    }

    String senhaInicial = resolverSenhaInicial();

    passwordPolicy.validar(DEFAULT_ADMIN_LOGIN, senhaInicial);
    String senhaHash = passwordEncoder.encode(senhaInicial);
    MapSqlParameterSource usuarioParams =
        new MapSqlParameterSource()
            .addValue("nome", "Administrador")
            .addValue("login", DEFAULT_ADMIN_LOGIN)
            .addValue("senhaHash", senhaHash)
            .addValue("trocaObrigatoria", 1);

    jdbcTemplate.update(
        """
        INSERT INTO usuario (nome, login, senha_hash, ativo, credencial_troca_obrigatoria)
        VALUES (:nome, :login, :senhaHash, 1, :trocaObrigatoria)
        """,
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
      if (bootstrapAdminPassword == null || bootstrapAdminPassword.isBlank()) {
        LOGGER.warn(
            "Usuario admin bootstrap criado (modo dev) com login='{}'. Credencial temporaria: '{}'. Troca obrigatoria no primeiro acesso.",
            DEFAULT_ADMIN_LOGIN,
            senhaInicial);
      } else {
        LOGGER.warn(
            "Usuario admin bootstrap criado com login='{}'. Credencial inicial definida por variavel de ambiente e troca obrigatoria no primeiro acesso.",
            DEFAULT_ADMIN_LOGIN);
      }
    }
  }

  private String resolverSenhaInicial() {
    if (bootstrapAdminPassword != null && !bootstrapAdminPassword.isBlank()) {
      return bootstrapAdminPassword;
    }
    if (MODE_STRICT.equalsIgnoreCase(bootstrapAdminMode)) {
      throw new IllegalStateException(
          "Bootstrap admin em modo strict sem senha inicial. Defina EASYNR6_BOOTSTRAP_ADMIN_PASSWORD.");
    }
    if (!MODE_DEV.equalsIgnoreCase(bootstrapAdminMode)) {
      throw new IllegalStateException(
          "Modo de bootstrap-admin invalido: "
              + bootstrapAdminMode
              + ". Valores suportados: dev|strict.");
    }
    return gerarSenhaTemporaria();
  }

  private String gerarSenhaTemporaria() {
    StringBuilder senha = new StringBuilder(TEMP_PASSWORD_LENGTH);
    senha.append(randomChar(UPPER));
    senha.append(randomChar(LOWER));
    senha.append(randomChar(DIGITS));
    senha.append(randomChar(SYMBOLS));
    for (int i = 4; i < TEMP_PASSWORD_LENGTH; i++) {
      senha.append(randomChar(ALL));
    }
    return embaralhar(senha.toString());
  }

  private String embaralhar(String texto) {
    char[] chars = texto.toCharArray();
    for (int i = chars.length - 1; i > 0; i--) {
      int j = secureRandom.nextInt(i + 1);
      char temp = chars[i];
      chars[i] = chars[j];
      chars[j] = temp;
    }
    return new String(chars);
  }

  private char randomChar(String source) {
    return source.charAt(secureRandom.nextInt(source.length()));
  }
}
