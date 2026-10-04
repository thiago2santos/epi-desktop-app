package br.com.easynr6.gestaoepi.modules.employee.application.port;

import java.util.List;
import java.util.Optional;

public interface OrgStructureRepository {

  Optional<DepartmentOption> findDepartmentById(Long departmentId);

  Optional<JobRoleOption> findJobRoleById(Long jobRoleId);

  boolean departmentExists(Long departmentId);

  boolean jobRoleExists(Long jobRoleId);

  boolean isActiveUnit(Long unitId);

  boolean existsDepartmentByNameInUnit(String name, Long unitId);

  boolean existsDepartmentByNameInUnitExcludingId(String name, Long unitId, Long departmentId);

  Long createDepartment(String name, Long unitId, boolean active);

  void updateDepartment(Long departmentId, String name, boolean active);

  void setDepartmentActive(Long departmentId, boolean active);

  boolean hasActiveJobRoles(Long departmentId);

  boolean existsJobRoleByNameInDepartment(String name, Long departmentId);

  boolean existsJobRoleByNameInDepartmentExcludingId(
      String name, Long departmentId, Long jobRoleId);

  Long createJobRole(String name, Long departmentId, boolean active);

  void updateJobRole(Long jobRoleId, String name, Long departmentId, boolean active);

  void setJobRoleActive(Long jobRoleId, boolean active);

  boolean hasActiveEmployeesByJobRole(Long jobRoleId);

  List<DepartmentSummary> listDepartmentsByTerm(String term);

  List<JobRoleSummary> listJobRolesByTerm(String term);

  List<UnitOption> listActiveUnits();

  Optional<UnitSummary> findUnitById(Long unitId);

  boolean existsUnitByCnpj(String cnpj);

  boolean hasActiveDepartments(Long unitId);

  Long createUnit(String name, String cnpj, boolean active);

  void updateUnitName(Long unitId, String name);

  void setUnitActive(Long unitId, boolean active);

  List<UnitSummary> listUnitsByTerm(String term);

  List<DepartmentOption> listActiveDepartments();

  List<JobRoleOption> listActiveJobRolesByDepartment(Long departmentId);

  record UnitOption(Long id, String name, String cnpj) {}

  record UnitSummary(Long id, String name, String cnpj, boolean active) {}

  record DepartmentOption(Long id, String name, Long unitId, String unitName, boolean active) {}

  record JobRoleOption(Long id, String name, Long departmentId, boolean active) {}

  record DepartmentSummary(Long id, String name, Long unitId, String unitName, boolean active) {}

  record JobRoleSummary(
      Long id,
      String name,
      Long departmentId,
      String departmentName,
      String unitName,
      boolean active) {}
}
