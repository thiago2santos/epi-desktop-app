package br.com.easynr6.gestaoepi.identity.infra.jdbc;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.identity.domain.CredentialState;
import br.com.easynr6.gestaoepi.identity.domain.IdentityUser;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UserAdministrationService.UsuarioAdminResumo;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcIdentityRepository implements IdentityRepository {

  private static final String SELECT_USER_BY_LOGIN_SQL =
      """
      SELECT id, nome, login, senha_hash, ativo, credencial_troca_obrigatoria, tentativas_invalidas, bloqueado_ate
      FROM usuario
      WHERE login = :login
      """;
  private static final String SELECT_ROLES_BY_USER_ID_SQL =
      """
      SELECT p.codigo
      FROM papel p
      JOIN usuario_papel up ON up.papel_id = p.id
      WHERE up.usuario_id = :userId
      """;
  private static final String UPDATE_AUTH_FAILURE_SQL =
      """
      UPDATE usuario
      SET tentativas_invalidas = :invalidAttempts,
          bloqueado_ate = :blockedUntil
      WHERE id = :userId
      """;
  private static final String CLEAR_AUTH_FAILURE_SQL =
      """
      UPDATE usuario
      SET tentativas_invalidas = 0,
          bloqueado_ate = NULL
      WHERE id = :userId
      """;
  private static final String UPDATE_CREDENTIAL_SQL =
      """
      UPDATE usuario
      SET senha_hash = :credentialHash,
          credencial_troca_obrigatoria = :forcePasswordChange,
          tentativas_invalidas = :invalidAttempts,
          bloqueado_ate = :blockedUntil
      WHERE id = :userId
      """;
  private static final String UPDATE_CREDENTIAL_WITH_TIMESTAMP_SQL =
      """
      UPDATE usuario
      SET senha_hash = :credentialHash,
          credencial_troca_obrigatoria = :forcePasswordChange,
          tentativas_invalidas = :invalidAttempts,
          bloqueado_ate = :blockedUntil,
          credencial_atualizada_em = CURRENT_TIMESTAMP
      WHERE id = :userId
      """;
  private static final String CREATE_USER_SQL =
      """
      INSERT INTO usuario (nome, login, senha_hash, ativo, credencial_troca_obrigatoria)
      VALUES (:nome, :login, :credentialHash, :ativo, :forcePasswordChange)
      """;
  private static final String SELECT_USER_ID_BY_LOGIN_SQL =
      "SELECT id FROM usuario WHERE login = :login";
  private static final String COUNT_USER_SQL = "SELECT COUNT(1) FROM usuario WHERE id = :userId";
  private static final String ASSIGN_ROLE_SQL =
      """
      INSERT OR IGNORE INTO usuario_papel (usuario_id, papel_id)
      VALUES (:userId, (SELECT id FROM papel WHERE codigo = :roleCode))
      """;
  private static final String REMOVE_ROLE_SQL =
      """
      DELETE FROM usuario_papel
      WHERE usuario_id = :userId
        AND papel_id = (SELECT id FROM papel WHERE codigo = :roleCode)
      """;
  private static final String UPDATE_USER_SQL =
      """
      UPDATE usuario
      SET nome = :nome,
          login = :login,
          ativo = :ativo
      WHERE id = :userId
      """;
  private static final String SET_USER_ACTIVE_SQL =
      "UPDATE usuario SET ativo = :ativo WHERE id = :userId";
  private static final String DELETE_USER_SQL = "DELETE FROM usuario WHERE id = :userId";
  private static final String DELETE_USER_ROLES_SQL =
      "DELETE FROM usuario_papel WHERE usuario_id = :userId";
  private static final String SELECT_USERS_SQL =
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

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public JdbcIdentityRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Optional<IdentityUser> findUserByLogin(String login) {
    List<IdentityUser> rows =
        jdbcTemplate.query(
            SELECT_USER_BY_LOGIN_SQL,
            new MapSqlParameterSource().addValue("login", login),
            (rs, rowNum) ->
                new IdentityUser(
                    rs.getLong("id"),
                    rs.getString("nome"),
                    rs.getString("login"),
                    rs.getString("senha_hash"),
                    rs.getInt("ativo") == 1,
                    new CredentialState(
                        rs.getInt("credencial_troca_obrigatoria") == 1,
                        rs.getInt("tentativas_invalidas"),
                        parseDateTime(rs.getString("bloqueado_ate")))));
    return rows.stream().findFirst();
  }

  @Override
  public Set<Papel> loadRoles(Long userId) {
    List<String> rows =
        jdbcTemplate.queryForList(
            SELECT_ROLES_BY_USER_ID_SQL,
            new MapSqlParameterSource().addValue("userId", userId),
            String.class);
    EnumSet<Papel> roles = EnumSet.noneOf(Papel.class);
    for (String role : rows) {
      roles.add(Papel.valueOf(role));
    }
    return roles;
  }

  @Override
  public void updateFailedAuthentication(
      Long userId, int invalidAttempts, LocalDateTime blockedUntil) {
    jdbcTemplate.update(
        UPDATE_AUTH_FAILURE_SQL,
        new MapSqlParameterSource()
            .addValue("invalidAttempts", invalidAttempts)
            .addValue("blockedUntil", blockedUntil == null ? null : blockedUntil.toString())
            .addValue("userId", userId));
  }

  @Override
  public void clearAuthenticationFailures(Long userId) {
    jdbcTemplate.update(
        CLEAR_AUTH_FAILURE_SQL, new MapSqlParameterSource().addValue("userId", userId));
  }

  @Override
  public void updateCredential(
      Long userId,
      String credentialHash,
      boolean forcePasswordChange,
      int invalidAttempts,
      LocalDateTime blockedUntil,
      boolean updateCredentialTimestamp) {
    String sql =
        updateCredentialTimestamp ? UPDATE_CREDENTIAL_WITH_TIMESTAMP_SQL : UPDATE_CREDENTIAL_SQL;
    jdbcTemplate.update(
        sql,
        new MapSqlParameterSource()
            .addValue("credentialHash", credentialHash)
            .addValue("forcePasswordChange", forcePasswordChange ? 1 : 0)
            .addValue("invalidAttempts", invalidAttempts)
            .addValue("blockedUntil", blockedUntil == null ? null : blockedUntil.toString())
            .addValue("userId", userId));
  }

  @Override
  public Long createUser(
      String nome,
      String login,
      String credentialHash,
      boolean ativo,
      boolean forcePasswordChange) {
    jdbcTemplate.update(
        CREATE_USER_SQL,
        new MapSqlParameterSource()
            .addValue("nome", nome)
            .addValue("login", login)
            .addValue("credentialHash", credentialHash)
            .addValue("ativo", ativo ? 1 : 0)
            .addValue("forcePasswordChange", forcePasswordChange ? 1 : 0));
    return jdbcTemplate.queryForObject(
        SELECT_USER_ID_BY_LOGIN_SQL,
        new MapSqlParameterSource().addValue("login", login),
        Long.class);
  }

  @Override
  public boolean userExists(Long userId) {
    Integer amount =
        jdbcTemplate.queryForObject(
            COUNT_USER_SQL, new MapSqlParameterSource().addValue("userId", userId), Integer.class);
    return amount != null && amount > 0;
  }

  @Override
  public void assignRole(Long userId, Papel papel) {
    jdbcTemplate.update(
        ASSIGN_ROLE_SQL,
        new MapSqlParameterSource().addValue("userId", userId).addValue("roleCode", papel.name()));
  }

  @Override
  public int removeRole(Long userId, Papel papel) {
    return jdbcTemplate.update(
        REMOVE_ROLE_SQL,
        new MapSqlParameterSource().addValue("userId", userId).addValue("roleCode", papel.name()));
  }

  @Override
  public IdentityUser requireUserByLogin(String login) {
    if (login == null || login.isBlank()) {
      throw new IllegalArgumentException("AUTH-011 Login alvo obrigatorio.");
    }
    return findUserByLogin(login.trim())
        .orElseThrow(() -> new IllegalArgumentException("AUTH-009 Usuario alvo inexistente."));
  }

  @Override
  public void updateUser(Long userId, String nome, String login, boolean ativo) {
    jdbcTemplate.update(
        UPDATE_USER_SQL,
        new MapSqlParameterSource()
            .addValue("nome", nome)
            .addValue("login", login)
            .addValue("ativo", ativo ? 1 : 0)
            .addValue("userId", userId));
  }

  @Override
  public void setUserActive(Long userId, boolean ativo) {
    jdbcTemplate.update(
        SET_USER_ACTIVE_SQL,
        new MapSqlParameterSource().addValue("ativo", ativo ? 1 : 0).addValue("userId", userId));
  }

  @Override
  public int deleteUser(Long userId) {
    return jdbcTemplate.update(
        DELETE_USER_SQL, new MapSqlParameterSource().addValue("userId", userId));
  }

  @Override
  public void deleteUserRoles(Long userId) {
    jdbcTemplate.update(
        DELETE_USER_ROLES_SQL, new MapSqlParameterSource().addValue("userId", userId));
  }

  @Override
  public List<UsuarioAdminResumo> listUsers() {
    return jdbcTemplate.query(
        SELECT_USERS_SQL,
        (rs, rowNum) ->
            new UsuarioAdminResumo(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("login"),
                rs.getInt("ativo") == 1,
                rs.getInt("credencial_troca_obrigatoria") == 1,
                rs.getString("papeis")));
  }

  private static LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return LocalDateTime.parse(value);
  }
}
