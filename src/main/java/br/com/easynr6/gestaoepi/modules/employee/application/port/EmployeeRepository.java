package br.com.easynr6.gestaoepi.modules.employee.application.port;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {

  boolean existsByEmployeeCode(String employeeCode);

  boolean existsById(Long employeeId);

  Long create(
      String employeeCode,
      String fullName,
      Long departmentId,
      Long jobRoleId,
      Long managerId,
      boolean active);

  void update(
      Long employeeId,
      String fullName,
      Long departmentId,
      Long jobRoleId,
      Long managerId,
      boolean active);

  Optional<EmployeeAssignment> findAssignmentById(Long employeeId);

  List<EmployeeOption> listActiveByUnit(Long unitId);

  void setActive(Long employeeId, boolean active);

  boolean hasHistoricalDependencies(Long employeeId);

  List<EmployeeSummary> listByTerm(String term);

  record EmployeeAssignment(Long id, Long unitId, boolean active) {}

  record EmployeeOption(Long id, String employeeCode, String fullName, boolean active) {}

  record EmployeeSummary(
      Long id,
      String employeeCode,
      String fullName,
      Long unitId,
      String unitName,
      Long departmentId,
      String departmentName,
      Long jobRoleId,
      String jobRoleName,
      Long managerId,
      String managerName,
      boolean active,
      String updatedAt) {}
}
