package br.com.easynr6.gestaoepi.modules.employee.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcEmployeeRepository implements EmployeeRepository {

  private static final String COUNT_EMPLOYEE_BY_CODE_SQL =
      "SELECT COUNT(1) FROM employee WHERE employee_code = :employeeCode";
  private static final String COUNT_EMPLOYEE_BY_ID_SQL =
      "SELECT COUNT(1) FROM employee WHERE id = :employeeId";
  private static final String INSERT_EMPLOYEE_SQL =
      """
      INSERT INTO employee (
          employee_code, full_name, department_id, job_role_id, manager_id, active)
      VALUES (
          :employeeCode, :fullName, :departmentId, :jobRoleId, :managerId, :active)
      """;
  private static final String SELECT_EMPLOYEE_ID_BY_CODE_SQL =
      "SELECT id FROM employee WHERE employee_code = :employeeCode";
  private static final String UPDATE_EMPLOYEE_SQL =
      """
      UPDATE employee
      SET full_name = :fullName,
          department_id = :departmentId,
          job_role_id = :jobRoleId,
          manager_id = :managerId,
          active = :active,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = :employeeId
      """;
  private static final String SET_EMPLOYEE_STATUS_SQL =
      """
      UPDATE employee
      SET active = :active,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = :employeeId
      """;
  private static final String SELECT_ASSIGNMENT_SQL =
      """
      SELECT e.id, d.unit_id, e.active
      FROM employee e
      JOIN department d ON d.id = e.department_id
      WHERE e.id = :employeeId
      """;
  private static final String LIST_ACTIVE_BY_UNIT_SQL =
      """
      SELECT e.id, e.employee_code, e.full_name, e.active
      FROM employee e
      JOIN department d ON d.id = e.department_id
      WHERE d.unit_id = :unitId
        AND e.active = 1
      ORDER BY e.full_name
      """;
  private static final String LIST_EMPLOYEE_SQL =
      """
      SELECT e.id,
             e.employee_code,
             e.full_name,
             u.id AS unit_id,
             u.name AS unit_name,
             e.department_id,
             d.name AS department_name,
             e.job_role_id,
             jr.name AS job_role_name,
             e.manager_id,
             m.full_name AS manager_name,
             e.active,
             e.updated_at
      FROM employee e
      JOIN department d ON d.id = e.department_id
      JOIN unit u ON u.id = d.unit_id
      JOIN job_role jr ON jr.id = e.job_role_id
      LEFT JOIN employee m ON m.id = e.manager_id
      WHERE (:term = '' OR e.employee_code LIKE :termLike OR e.full_name LIKE :termLike)
      ORDER BY e.full_name
      """;

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public JdbcEmployeeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public boolean existsByEmployeeCode(String employeeCode) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_EMPLOYEE_BY_CODE_SQL,
            new MapSqlParameterSource().addValue("employeeCode", employeeCode),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public boolean existsById(Long employeeId) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_EMPLOYEE_BY_ID_SQL,
            new MapSqlParameterSource().addValue("employeeId", employeeId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public Long create(
      String employeeCode,
      String fullName,
      Long departmentId,
      Long jobRoleId,
      Long managerId,
      boolean active) {
    jdbcTemplate.update(
        INSERT_EMPLOYEE_SQL,
        new MapSqlParameterSource()
            .addValue("employeeCode", employeeCode)
            .addValue("fullName", fullName)
            .addValue("departmentId", departmentId)
            .addValue("jobRoleId", jobRoleId)
            .addValue("managerId", managerId, Types.INTEGER)
            .addValue("active", active ? 1 : 0));
    return jdbcTemplate.queryForObject(
        SELECT_EMPLOYEE_ID_BY_CODE_SQL,
        new MapSqlParameterSource().addValue("employeeCode", employeeCode),
        Long.class);
  }

  @Override
  public void update(
      Long employeeId,
      String fullName,
      Long departmentId,
      Long jobRoleId,
      Long managerId,
      boolean active) {
    jdbcTemplate.update(
        UPDATE_EMPLOYEE_SQL,
        new MapSqlParameterSource()
            .addValue("employeeId", employeeId)
            .addValue("fullName", fullName)
            .addValue("departmentId", departmentId)
            .addValue("jobRoleId", jobRoleId)
            .addValue("managerId", managerId, Types.INTEGER)
            .addValue("active", active ? 1 : 0));
  }

  @Override
  public Optional<EmployeeAssignment> findAssignmentById(Long employeeId) {
    if (employeeId == null) {
      return Optional.empty();
    }
    List<EmployeeAssignment> rows =
        jdbcTemplate.query(
            SELECT_ASSIGNMENT_SQL,
            new MapSqlParameterSource().addValue("employeeId", employeeId),
            (rs, rowNum) ->
                new EmployeeAssignment(
                    rs.getLong("id"), rs.getLong("unit_id"), rs.getInt("active") == 1));
    return rows.stream().findFirst();
  }

  @Override
  public List<EmployeeOption> listActiveByUnit(Long unitId) {
    if (unitId == null) {
      return List.of();
    }
    return jdbcTemplate.query(
        LIST_ACTIVE_BY_UNIT_SQL,
        new MapSqlParameterSource().addValue("unitId", unitId),
        (rs, rowNum) ->
            new EmployeeOption(
                rs.getLong("id"),
                rs.getString("employee_code"),
                rs.getString("full_name"),
                rs.getInt("active") == 1));
  }

  @Override
  public void setActive(Long employeeId, boolean active) {
    jdbcTemplate.update(
        SET_EMPLOYEE_STATUS_SQL,
        new MapSqlParameterSource()
            .addValue("employeeId", employeeId)
            .addValue("active", active ? 1 : 0));
  }

  @Override
  public boolean hasHistoricalDependencies(Long employeeId) {
    return false;
  }

  @Override
  public List<EmployeeSummary> listByTerm(String term) {
    String cleanTerm = term == null ? "" : term.trim();
    return jdbcTemplate.query(
        LIST_EMPLOYEE_SQL,
        new MapSqlParameterSource()
            .addValue("term", cleanTerm)
            .addValue("termLike", "%" + cleanTerm + "%"),
        (rs, rowNum) ->
            new EmployeeSummary(
                rs.getLong("id"),
                rs.getString("employee_code"),
                rs.getString("full_name"),
                rs.getLong("unit_id"),
                rs.getString("unit_name"),
                rs.getLong("department_id"),
                rs.getString("department_name"),
                rs.getLong("job_role_id"),
                rs.getString("job_role_name"),
                nullableLong(rs, "manager_id"),
                rs.getString("manager_name"),
                rs.getInt("active") == 1,
                rs.getString("updated_at")));
  }

  private static Long nullableLong(ResultSet rs, String column) throws SQLException {
    long value = rs.getLong(column);
    return rs.wasNull() ? null : value;
  }
}
