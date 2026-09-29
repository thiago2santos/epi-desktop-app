package br.com.easynr6.gestaoepi.modules.employee.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository;
import java.util.List;
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
      INSERT INTO employee (employee_code, full_name, department_id, job_role_id, active)
      VALUES (:employeeCode, :fullName, :departmentId, :jobRoleId, :active)
      """;
  private static final String SELECT_EMPLOYEE_ID_BY_CODE_SQL =
      "SELECT id FROM employee WHERE employee_code = :employeeCode";
  private static final String UPDATE_EMPLOYEE_SQL =
      """
      UPDATE employee
      SET full_name = :fullName,
          department_id = :departmentId,
          job_role_id = :jobRoleId,
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
  private static final String LIST_EMPLOYEE_SQL =
      """
      SELECT e.id,
             e.employee_code,
             e.full_name,
             e.department_id,
             d.name AS department_name,
             e.job_role_id,
             jr.name AS job_role_name,
             e.active,
             e.updated_at
      FROM employee e
      JOIN department d ON d.id = e.department_id
      JOIN job_role jr ON jr.id = e.job_role_id
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
      String employeeCode, String fullName, Long departmentId, Long jobRoleId, boolean active) {
    jdbcTemplate.update(
        INSERT_EMPLOYEE_SQL,
        new MapSqlParameterSource()
            .addValue("employeeCode", employeeCode)
            .addValue("fullName", fullName)
            .addValue("departmentId", departmentId)
            .addValue("jobRoleId", jobRoleId)
            .addValue("active", active ? 1 : 0));
    return jdbcTemplate.queryForObject(
        SELECT_EMPLOYEE_ID_BY_CODE_SQL,
        new MapSqlParameterSource().addValue("employeeCode", employeeCode),
        Long.class);
  }

  @Override
  public void update(
      Long employeeId, String fullName, Long departmentId, Long jobRoleId, boolean active) {
    jdbcTemplate.update(
        UPDATE_EMPLOYEE_SQL,
        new MapSqlParameterSource()
            .addValue("employeeId", employeeId)
            .addValue("fullName", fullName)
            .addValue("departmentId", departmentId)
            .addValue("jobRoleId", jobRoleId)
            .addValue("active", active ? 1 : 0));
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
                rs.getLong("department_id"),
                rs.getString("department_name"),
                rs.getLong("job_role_id"),
                rs.getString("job_role_name"),
                rs.getInt("active") == 1,
                rs.getString("updated_at")));
  }
}
