package br.com.easynr6.gestaoepi.modules.employee.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcOrgStructureRepository implements OrgStructureRepository {

  private static final String SELECT_DEPARTMENT_BY_ID_SQL =
      """
      SELECT d.id, d.name, d.unit_id, u.name AS unit_name, d.active
      FROM department d
      JOIN unit u ON u.id = d.unit_id
      WHERE d.id = :departmentId
      """;
  private static final String SELECT_JOB_ROLE_BY_ID_SQL =
      """
      SELECT id, name, department_id, active
      FROM job_role
      WHERE id = :jobRoleId
      """;
  private static final String SELECT_ACTIVE_DEPARTMENTS_SQL =
      """
      SELECT d.id, d.name, d.unit_id, u.name AS unit_name, d.active
      FROM department d
      JOIN unit u ON u.id = d.unit_id
      WHERE d.active = 1
      ORDER BY u.name, d.name
      """;
  private static final String SELECT_ACTIVE_JOB_ROLES_BY_DEPARTMENT_SQL =
      """
      SELECT id, name, department_id, active
      FROM job_role
      WHERE department_id = :departmentId
        AND active = 1
      ORDER BY name
      """;
  private static final String COUNT_DEPARTMENT_BY_ID_SQL =
      "SELECT COUNT(1) FROM department WHERE id = :departmentId";
  private static final String COUNT_JOB_ROLE_BY_ID_SQL =
      "SELECT COUNT(1) FROM job_role WHERE id = :jobRoleId";
  private static final String COUNT_ACTIVE_UNIT_SQL =
      "SELECT COUNT(1) FROM unit WHERE id = :unitId AND active = 1";
  private static final String COUNT_DEPARTMENT_BY_NAME_SQL =
      """
      SELECT COUNT(1) FROM department
      WHERE UPPER(name) = UPPER(:name) AND unit_id = :unitId
      """;
  private static final String COUNT_DEPARTMENT_BY_NAME_EXCLUDING_ID_SQL =
      """
      SELECT COUNT(1) FROM department
      WHERE UPPER(name) = UPPER(:name) AND unit_id = :unitId AND id <> :departmentId
      """;
  private static final String INSERT_DEPARTMENT_SQL =
      "INSERT INTO department (name, unit_id, active) VALUES (:name, :unitId, :active)";
  private static final String SELECT_DEPARTMENT_ID_BY_NAME_SQL =
      """
      SELECT id FROM department
      WHERE UPPER(name) = UPPER(:name) AND unit_id = :unitId
      """;
  private static final String UPDATE_DEPARTMENT_SQL =
      """
      UPDATE department
      SET name = :name,
          active = :active
      WHERE id = :departmentId
      """;
  private static final String SET_DEPARTMENT_STATUS_SQL =
      """
      UPDATE department
      SET active = :active
      WHERE id = :departmentId
      """;
  private static final String COUNT_ACTIVE_JOB_ROLES_BY_DEPARTMENT_SQL =
      """
      SELECT COUNT(1)
      FROM job_role
      WHERE department_id = :departmentId
        AND active = 1
      """;
  private static final String COUNT_JOB_ROLE_BY_NAME_AND_DEPARTMENT_SQL =
      """
      SELECT COUNT(1)
      FROM job_role
      WHERE UPPER(name) = UPPER(:name)
        AND department_id = :departmentId
      """;
  private static final String COUNT_JOB_ROLE_BY_NAME_AND_DEPARTMENT_EXCLUDING_ID_SQL =
      """
      SELECT COUNT(1)
      FROM job_role
      WHERE UPPER(name) = UPPER(:name)
        AND department_id = :departmentId
        AND id <> :jobRoleId
      """;
  private static final String INSERT_JOB_ROLE_SQL =
      """
      INSERT INTO job_role (name, department_id, active)
      VALUES (:name, :departmentId, :active)
      """;
  private static final String SELECT_JOB_ROLE_ID_BY_NAME_AND_DEPARTMENT_SQL =
      """
      SELECT id
      FROM job_role
      WHERE UPPER(name) = UPPER(:name)
        AND department_id = :departmentId
      """;
  private static final String UPDATE_JOB_ROLE_SQL =
      """
      UPDATE job_role
      SET name = :name,
          department_id = :departmentId,
          active = :active
      WHERE id = :jobRoleId
      """;
  private static final String SET_JOB_ROLE_STATUS_SQL =
      """
      UPDATE job_role
      SET active = :active
      WHERE id = :jobRoleId
      """;
  private static final String COUNT_ACTIVE_EMPLOYEES_BY_JOB_ROLE_SQL =
      """
      SELECT COUNT(1)
      FROM employee
      WHERE job_role_id = :jobRoleId
        AND active = 1
      """;
  private static final String LIST_DEPARTMENTS_BY_TERM_SQL =
      """
      SELECT d.id, d.name, d.unit_id, u.name AS unit_name, d.active
      FROM department d
      JOIN unit u ON u.id = d.unit_id
      WHERE (:term = '' OR d.name LIKE :termLike)
      ORDER BY u.name, d.name
      """;
  private static final String LIST_JOB_ROLES_BY_TERM_SQL =
      """
      SELECT jr.id,
             jr.name,
             jr.department_id,
             d.name AS department_name,
             jr.active
      FROM job_role jr
      JOIN department d ON d.id = jr.department_id
      WHERE (:term = '' OR jr.name LIKE :termLike)
      ORDER BY d.name, jr.name
      """;

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public JdbcOrgStructureRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Optional<DepartmentOption> findDepartmentById(Long departmentId) {
    List<DepartmentOption> rows =
        jdbcTemplate.query(
            SELECT_DEPARTMENT_BY_ID_SQL,
            new MapSqlParameterSource().addValue("departmentId", departmentId),
            (rs, rowNum) ->
                new DepartmentOption(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getLong("unit_id"),
                    rs.getString("unit_name"),
                    rs.getInt("active") == 1));
    return rows.stream().findFirst();
  }

  @Override
  public Optional<JobRoleOption> findJobRoleById(Long jobRoleId) {
    List<JobRoleOption> rows =
        jdbcTemplate.query(
            SELECT_JOB_ROLE_BY_ID_SQL,
            new MapSqlParameterSource().addValue("jobRoleId", jobRoleId),
            (rs, rowNum) ->
                new JobRoleOption(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getLong("department_id"),
                    rs.getInt("active") == 1));
    return rows.stream().findFirst();
  }

  @Override
  public boolean departmentExists(Long departmentId) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_DEPARTMENT_BY_ID_SQL,
            new MapSqlParameterSource().addValue("departmentId", departmentId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public boolean jobRoleExists(Long jobRoleId) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_JOB_ROLE_BY_ID_SQL,
            new MapSqlParameterSource().addValue("jobRoleId", jobRoleId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public boolean isActiveUnit(Long unitId) {
    if (unitId == null) {
      return false;
    }
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_ACTIVE_UNIT_SQL,
            new MapSqlParameterSource().addValue("unitId", unitId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public boolean existsDepartmentByNameInUnit(String name, Long unitId) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_DEPARTMENT_BY_NAME_SQL,
            new MapSqlParameterSource().addValue("name", name).addValue("unitId", unitId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public boolean existsDepartmentByNameInUnitExcludingId(
      String name, Long unitId, Long departmentId) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_DEPARTMENT_BY_NAME_EXCLUDING_ID_SQL,
            new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("unitId", unitId)
                .addValue("departmentId", departmentId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public Long createDepartment(String name, Long unitId, boolean active) {
    jdbcTemplate.update(
        INSERT_DEPARTMENT_SQL,
        new MapSqlParameterSource()
            .addValue("name", name)
            .addValue("unitId", unitId)
            .addValue("active", active ? 1 : 0));
    return jdbcTemplate.queryForObject(
        SELECT_DEPARTMENT_ID_BY_NAME_SQL,
        new MapSqlParameterSource().addValue("name", name).addValue("unitId", unitId),
        Long.class);
  }

  @Override
  public void updateDepartment(Long departmentId, String name, boolean active) {
    jdbcTemplate.update(
        UPDATE_DEPARTMENT_SQL,
        new MapSqlParameterSource()
            .addValue("departmentId", departmentId)
            .addValue("name", name)
            .addValue("active", active ? 1 : 0));
  }

  @Override
  public void setDepartmentActive(Long departmentId, boolean active) {
    jdbcTemplate.update(
        SET_DEPARTMENT_STATUS_SQL,
        new MapSqlParameterSource()
            .addValue("departmentId", departmentId)
            .addValue("active", active ? 1 : 0));
  }

  @Override
  public boolean hasActiveJobRoles(Long departmentId) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_ACTIVE_JOB_ROLES_BY_DEPARTMENT_SQL,
            new MapSqlParameterSource().addValue("departmentId", departmentId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public boolean existsJobRoleByNameInDepartment(String name, Long departmentId) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_JOB_ROLE_BY_NAME_AND_DEPARTMENT_SQL,
            new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("departmentId", departmentId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public boolean existsJobRoleByNameInDepartmentExcludingId(
      String name, Long departmentId, Long jobRoleId) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_JOB_ROLE_BY_NAME_AND_DEPARTMENT_EXCLUDING_ID_SQL,
            new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("departmentId", departmentId)
                .addValue("jobRoleId", jobRoleId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public Long createJobRole(String name, Long departmentId, boolean active) {
    jdbcTemplate.update(
        INSERT_JOB_ROLE_SQL,
        new MapSqlParameterSource()
            .addValue("name", name)
            .addValue("departmentId", departmentId)
            .addValue("active", active ? 1 : 0));
    return jdbcTemplate.queryForObject(
        SELECT_JOB_ROLE_ID_BY_NAME_AND_DEPARTMENT_SQL,
        new MapSqlParameterSource().addValue("name", name).addValue("departmentId", departmentId),
        Long.class);
  }

  @Override
  public void updateJobRole(Long jobRoleId, String name, Long departmentId, boolean active) {
    jdbcTemplate.update(
        UPDATE_JOB_ROLE_SQL,
        new MapSqlParameterSource()
            .addValue("jobRoleId", jobRoleId)
            .addValue("name", name)
            .addValue("departmentId", departmentId)
            .addValue("active", active ? 1 : 0));
  }

  @Override
  public void setJobRoleActive(Long jobRoleId, boolean active) {
    jdbcTemplate.update(
        SET_JOB_ROLE_STATUS_SQL,
        new MapSqlParameterSource()
            .addValue("jobRoleId", jobRoleId)
            .addValue("active", active ? 1 : 0));
  }

  @Override
  public boolean hasActiveEmployeesByJobRole(Long jobRoleId) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_ACTIVE_EMPLOYEES_BY_JOB_ROLE_SQL,
            new MapSqlParameterSource().addValue("jobRoleId", jobRoleId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public List<DepartmentSummary> listDepartmentsByTerm(String term) {
    String cleanTerm = term == null ? "" : term.trim();
    return jdbcTemplate.query(
        LIST_DEPARTMENTS_BY_TERM_SQL,
        new MapSqlParameterSource()
            .addValue("term", cleanTerm)
            .addValue("termLike", "%" + cleanTerm + "%"),
        (rs, rowNum) ->
            new DepartmentSummary(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("unit_id"),
                rs.getString("unit_name"),
                rs.getInt("active") == 1));
  }

  @Override
  public List<JobRoleSummary> listJobRolesByTerm(String term) {
    String cleanTerm = term == null ? "" : term.trim();
    return jdbcTemplate.query(
        LIST_JOB_ROLES_BY_TERM_SQL,
        new MapSqlParameterSource()
            .addValue("term", cleanTerm)
            .addValue("termLike", "%" + cleanTerm + "%"),
        (rs, rowNum) ->
            new JobRoleSummary(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("department_id"),
                rs.getString("department_name"),
                rs.getInt("active") == 1));
  }

  @Override
  public List<DepartmentOption> listActiveDepartments() {
    return jdbcTemplate.query(
        SELECT_ACTIVE_DEPARTMENTS_SQL,
        (rs, rowNum) ->
            new DepartmentOption(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("unit_id"),
                rs.getString("unit_name"),
                rs.getInt("active") == 1));
  }

  @Override
  public List<JobRoleOption> listActiveJobRolesByDepartment(Long departmentId) {
    if (departmentId == null) {
      return List.of();
    }
    return jdbcTemplate.query(
        SELECT_ACTIVE_JOB_ROLES_BY_DEPARTMENT_SQL,
        new MapSqlParameterSource().addValue("departmentId", departmentId),
        (rs, rowNum) ->
            new JobRoleOption(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getLong("department_id"),
                rs.getInt("active") == 1));
  }
}
