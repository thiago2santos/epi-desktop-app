package br.com.easynr6.gestaoepi.modules.employee.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
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
  private static final String LIST_ACTIVE_UNITS_SQL =
      """
      SELECT id, name, cnpj
      FROM unit
      WHERE active = 1
      ORDER BY name, cnpj
      """;
  private static final String SELECT_UNIT_BY_ID_SQL =
      """
      SELECT id, name, cnpj, active
      FROM unit
      WHERE id = :unitId
      """;
  private static final String COUNT_UNIT_BY_CNPJ_SQL =
      "SELECT COUNT(1) FROM unit WHERE cnpj = :cnpj";
  private static final String COUNT_ACTIVE_DEPARTMENTS_BY_UNIT_SQL =
      "SELECT COUNT(1) FROM department WHERE unit_id = :unitId AND active = 1";
  private static final String SELECT_COMPANY_ID_SQL = "SELECT id FROM company ORDER BY id LIMIT 1";
  private static final String INSERT_UNIT_SQL =
      """
      INSERT INTO unit (company_id, name, cnpj, active)
      VALUES (:companyId, :name, :cnpj, :active)
      """;
  private static final String SELECT_UNIT_ID_BY_CNPJ_SQL = "SELECT id FROM unit WHERE cnpj = :cnpj";
  private static final String UPDATE_UNIT_NAME_SQL =
      "UPDATE unit SET name = :name WHERE id = :unitId";
  private static final String SET_UNIT_STATUS_SQL =
      "UPDATE unit SET active = :active WHERE id = :unitId";
  private static final String LIST_UNITS_BY_TERM_SQL =
      """
      SELECT id, name, cnpj, active
      FROM unit
      WHERE (:term = ''
         OR name LIKE :termLike
         OR (:digits <> '' AND cnpj LIKE :digitsLike))
      ORDER BY name, cnpj
      """;
  private static final String LIST_JOB_ROLES_BY_TERM_SQL =
      """
      SELECT jr.id,
             jr.name,
             jr.department_id,
             d.name AS department_name,
             u.name AS unit_name,
             jr.active
      FROM job_role jr
      JOIN department d ON d.id = jr.department_id
      JOIN unit u ON u.id = d.unit_id
      WHERE (:term = '' OR jr.name LIKE :termLike OR d.name LIKE :termLike)
      ORDER BY u.name, d.name, jr.name
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
                rs.getString("unit_name"),
                rs.getInt("active") == 1));
  }

  @Override
  public List<UnitOption> listActiveUnits() {
    return jdbcTemplate.query(
        LIST_ACTIVE_UNITS_SQL,
        (rs, rowNum) ->
            new UnitOption(rs.getLong("id"), rs.getString("name"), rs.getString("cnpj")));
  }

  @Override
  public Optional<UnitSummary> findUnitById(Long unitId) {
    if (unitId == null) {
      return Optional.empty();
    }
    List<UnitSummary> rows =
        jdbcTemplate.query(
            SELECT_UNIT_BY_ID_SQL,
            new MapSqlParameterSource().addValue("unitId", unitId),
            (rs, rowNum) -> unitSummary(rs));
    return rows.stream().findFirst();
  }

  @Override
  public boolean existsUnitByCnpj(String cnpj) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_UNIT_BY_CNPJ_SQL,
            new MapSqlParameterSource().addValue("cnpj", cnpj),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public boolean hasActiveDepartments(Long unitId) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_ACTIVE_DEPARTMENTS_BY_UNIT_SQL,
            new MapSqlParameterSource().addValue("unitId", unitId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public Long createUnit(String name, String cnpj, boolean active) {
    Long companyId =
        jdbcTemplate.queryForObject(SELECT_COMPANY_ID_SQL, new MapSqlParameterSource(), Long.class);
    if (companyId == null) {
      throw new IllegalStateException("Empresa nao encontrada.");
    }
    try {
      jdbcTemplate.update(
          INSERT_UNIT_SQL,
          new MapSqlParameterSource()
              .addValue("companyId", companyId)
              .addValue("name", name)
              .addValue("cnpj", cnpj)
              .addValue("active", active ? 1 : 0));
    } catch (DuplicateKeyException ex) {
      throw new IllegalArgumentException("CAD-043 CNPJ ja cadastrado.");
    }
    return jdbcTemplate.queryForObject(
        SELECT_UNIT_ID_BY_CNPJ_SQL, new MapSqlParameterSource().addValue("cnpj", cnpj), Long.class);
  }

  @Override
  public void updateUnitName(Long unitId, String name) {
    jdbcTemplate.update(
        UPDATE_UNIT_NAME_SQL,
        new MapSqlParameterSource().addValue("unitId", unitId).addValue("name", name));
  }

  @Override
  public void setUnitActive(Long unitId, boolean active) {
    jdbcTemplate.update(
        SET_UNIT_STATUS_SQL,
        new MapSqlParameterSource().addValue("unitId", unitId).addValue("active", active ? 1 : 0));
  }

  @Override
  public List<UnitSummary> listUnitsByTerm(String term) {
    String cleanTerm = term == null ? "" : term.trim();
    String digits = cleanTerm.replaceAll("\\D", "");
    return jdbcTemplate.query(
        LIST_UNITS_BY_TERM_SQL,
        new MapSqlParameterSource()
            .addValue("term", cleanTerm)
            .addValue("termLike", "%" + cleanTerm + "%")
            .addValue("digits", digits)
            .addValue("digitsLike", "%" + digits + "%"),
        (rs, rowNum) -> unitSummary(rs));
  }

  private static UnitSummary unitSummary(ResultSet rs) throws SQLException {
    return new UnitSummary(
        rs.getLong("id"), rs.getString("name"), rs.getString("cnpj"), rs.getInt("active") == 1);
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
