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
      SELECT id, name, active
      FROM department
      WHERE id = :departmentId
      """;
  private static final String SELECT_JOB_ROLE_BY_ID_SQL =
      """
      SELECT id, name, department_id, active
      FROM job_role
      WHERE id = :jobRoleId
      """;
  private static final String SELECT_ACTIVE_DEPARTMENTS_SQL =
      """
      SELECT id, name, active
      FROM department
      WHERE active = 1
      ORDER BY name
      """;
  private static final String SELECT_ACTIVE_JOB_ROLES_BY_DEPARTMENT_SQL =
      """
      SELECT id, name, department_id, active
      FROM job_role
      WHERE department_id = :departmentId
        AND active = 1
      ORDER BY name
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
                    rs.getLong("id"), rs.getString("name"), rs.getInt("active") == 1));
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
  public List<DepartmentOption> listActiveDepartments() {
    return jdbcTemplate.query(
        SELECT_ACTIVE_DEPARTMENTS_SQL,
        (rs, rowNum) ->
            new DepartmentOption(rs.getLong("id"), rs.getString("name"), rs.getInt("active") == 1));
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
