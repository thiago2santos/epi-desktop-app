package br.com.easynr6.gestaoepi.modules.employee.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcGheRepository implements GheRepository {

  private static final String UNIT_EXISTS_SQL = "SELECT COUNT(1) FROM unit WHERE id = :unitId";
  private static final String FIND_GHE_SQL =
      "SELECT id, unit_id, name, active FROM ghe WHERE id = :gheId";
  private static final String NAME_EXISTS_SQL =
      """
      SELECT COUNT(1) FROM ghe
      WHERE unit_id = :unitId AND UPPER(name) = UPPER(:name)
        AND (:excludeId IS NULL OR id <> :excludeId)
      """;
  private static final String INSERT_SQL =
      "INSERT INTO ghe (unit_id, name, active) VALUES (:unitId, :name, :active)";
  private static final String SELECT_ID_SQL =
      """
      SELECT id FROM ghe
      WHERE unit_id = :unitId AND name = :name
      ORDER BY id DESC LIMIT 1
      """;
  private static final String UPDATE_NAME_SQL = "UPDATE ghe SET name = :name WHERE id = :gheId";
  private static final String UPDATE_ACTIVE_SQL =
      "UPDATE ghe SET active = :active WHERE id = :gheId";
  private static final String FIND_JOB_ROLE_SQL =
      """
      SELECT jr.id, jr.active, d.unit_id, m.ghe_id
      FROM job_role jr
      JOIN department d ON d.id = jr.department_id
      LEFT JOIN ghe_job_role m ON m.job_role_id = jr.id
      WHERE jr.id = :jobRoleId
      """;
  private static final String LINK_EXISTS_SQL =
      "SELECT COUNT(1) FROM ghe_job_role WHERE ghe_id = :gheId AND job_role_id = :jobRoleId";
  private static final String INSERT_LINK_SQL =
      "INSERT INTO ghe_job_role (ghe_id, job_role_id) VALUES (:gheId, :jobRoleId)";
  private static final String DELETE_LINK_SQL =
      "DELETE FROM ghe_job_role WHERE ghe_id = :gheId AND job_role_id = :jobRoleId";
  private static final String MEMBERSHIP_SQL =
      """
      SELECT g.id, g.active
      FROM ghe g
      JOIN ghe_job_role m ON m.ghe_id = g.id
      WHERE m.job_role_id = :jobRoleId
      """;
  private static final String LIST_SQL =
      """
      SELECT g.id, g.unit_id, g.name, g.active,
             (SELECT COUNT(1) FROM ghe_job_role m WHERE m.ghe_id = g.id) AS funcoes
      FROM ghe g
      WHERE g.unit_id = :unitId
        AND (:term IS NULL OR UPPER(g.name) LIKE UPPER(:term))
      ORDER BY g.name
      """;
  private static final String MEMBERS_SQL =
      """
      SELECT jr.id, jr.name, d.name AS department_name, jr.active
      FROM ghe_job_role m
      JOIN job_role jr ON jr.id = m.job_role_id
      JOIN department d ON d.id = jr.department_id
      WHERE m.ghe_id = :gheId
      ORDER BY d.name, jr.name
      """;
  private static final String CANDIDATES_SQL =
      """
      SELECT jr.id, jr.name, d.name AS department_name, jr.active
      FROM job_role jr
      JOIN department d ON d.id = jr.department_id
      WHERE d.unit_id = :unitId
        AND jr.active = 1
        AND NOT EXISTS (SELECT 1 FROM ghe_job_role m WHERE m.job_role_id = jr.id)
      ORDER BY d.name, jr.name
      """;

  private final NamedParameterJdbcTemplate jdbc;

  public JdbcGheRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public boolean unitExists(Long unitId) {
    if (unitId == null) {
      return false;
    }
    Integer count =
        jdbc.queryForObject(
            UNIT_EXISTS_SQL, new MapSqlParameterSource("unitId", unitId), Integer.class);
    return count != null && count > 0;
  }

  @Override
  public Optional<GheStored> findById(Long gheId) {
    if (gheId == null) {
      return Optional.empty();
    }
    List<GheStored> rows =
        jdbc.query(
            FIND_GHE_SQL,
            new MapSqlParameterSource("gheId", gheId),
            (rs, rowNum) ->
                new GheStored(
                    rs.getLong("id"),
                    rs.getLong("unit_id"),
                    rs.getString("name"),
                    rs.getInt("active") == 1));
    return rows.stream().findFirst();
  }

  @Override
  public boolean existsNameInUnit(String name, Long unitId, Long excludeId) {
    Integer count =
        jdbc.queryForObject(
            NAME_EXISTS_SQL,
            new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("unitId", unitId)
                .addValue("excludeId", excludeId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public Long create(Long unitId, String name, boolean active) {
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("unitId", unitId)
            .addValue("name", name)
            .addValue("active", active ? 1 : 0);
    jdbc.update(INSERT_SQL, params);
    return jdbc.queryForObject(SELECT_ID_SQL, params, Long.class);
  }

  @Override
  public void updateName(Long gheId, String name) {
    jdbc.update(
        UPDATE_NAME_SQL,
        new MapSqlParameterSource().addValue("gheId", gheId).addValue("name", name));
  }

  @Override
  public void setActive(Long gheId, boolean active) {
    jdbc.update(
        UPDATE_ACTIVE_SQL,
        new MapSqlParameterSource().addValue("gheId", gheId).addValue("active", active ? 1 : 0));
  }

  @Override
  public Optional<FuncaoParaGhe> findJobRole(Long jobRoleId) {
    if (jobRoleId == null) {
      return Optional.empty();
    }
    List<FuncaoParaGhe> rows =
        jdbc.query(
            FIND_JOB_ROLE_SQL,
            new MapSqlParameterSource("jobRoleId", jobRoleId),
            (rs, rowNum) -> {
              long ghe = rs.getLong("ghe_id");
              Long gheId = rs.wasNull() ? null : ghe;
              return new FuncaoParaGhe(
                  rs.getLong("id"), rs.getInt("active") == 1, rs.getLong("unit_id"), gheId);
            });
    return rows.stream().findFirst();
  }

  @Override
  public boolean link(Long gheId, Long jobRoleId) {
    MapSqlParameterSource params =
        new MapSqlParameterSource().addValue("gheId", gheId).addValue("jobRoleId", jobRoleId);
    Integer count = jdbc.queryForObject(LINK_EXISTS_SQL, params, Integer.class);
    if (count != null && count > 0) {
      return false;
    }
    jdbc.update(INSERT_LINK_SQL, params);
    return true;
  }

  @Override
  public boolean unlink(Long gheId, Long jobRoleId) {
    int removed =
        jdbc.update(
            DELETE_LINK_SQL,
            new MapSqlParameterSource().addValue("gheId", gheId).addValue("jobRoleId", jobRoleId));
    return removed > 0;
  }

  @Override
  public Optional<GheMembership> findMembership(Long jobRoleId) {
    if (jobRoleId == null) {
      return Optional.empty();
    }
    List<GheMembership> rows =
        jdbc.query(
            MEMBERSHIP_SQL,
            new MapSqlParameterSource("jobRoleId", jobRoleId),
            (rs, rowNum) -> new GheMembership(rs.getLong("id"), rs.getInt("active") == 1));
    return rows.stream().findFirst();
  }

  @Override
  public List<GheSummary> listByUnit(Long unitId, String term) {
    String filtro = term == null || term.isBlank() ? null : "%" + term.trim() + "%";
    return jdbc.query(
        LIST_SQL,
        new MapSqlParameterSource().addValue("unitId", unitId).addValue("term", filtro),
        (rs, rowNum) ->
            new GheSummary(
                rs.getLong("id"),
                rs.getLong("unit_id"),
                rs.getString("name"),
                rs.getInt("active") == 1,
                rs.getInt("funcoes")));
  }

  @Override
  public List<FuncaoDoGhe> listMembers(Long gheId) {
    return funcoes(MEMBERS_SQL, new MapSqlParameterSource("gheId", gheId));
  }

  @Override
  public List<FuncaoDoGhe> listCandidates(Long unitId) {
    return funcoes(CANDIDATES_SQL, new MapSqlParameterSource("unitId", unitId));
  }

  private List<FuncaoDoGhe> funcoes(String sql, MapSqlParameterSource params) {
    return jdbc.query(
        sql,
        params,
        (rs, rowNum) ->
            new FuncaoDoGhe(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("department_name"),
                rs.getInt("active") == 1));
  }
}
