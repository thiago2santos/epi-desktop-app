package br.com.easynr6.gestaoepi.modules.employee.application.port;

import java.util.List;

public interface EmployeeRepository {

  boolean existsByEmployeeCode(String employeeCode);

  boolean existsById(Long employeeId);

  Long create(
      String employeeCode, String fullName, Long departmentId, Long jobRoleId, boolean active);

  void update(Long employeeId, String fullName, Long departmentId, Long jobRoleId, boolean active);

  void setActive(Long employeeId, boolean active);

  boolean hasHistoricalDependencies(Long employeeId);

  List<EmployeeSummary> listByTerm(String term);

  record EmployeeSummary(
      Long id,
      String employeeCode,
      String fullName,
      Long departmentId,
      String departmentName,
      Long jobRoleId,
      String jobRoleName,
      boolean active,
      String updatedAt) {}
}
