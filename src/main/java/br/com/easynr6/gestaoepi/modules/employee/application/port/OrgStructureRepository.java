package br.com.easynr6.gestaoepi.modules.employee.application.port;

import java.util.List;
import java.util.Optional;

public interface OrgStructureRepository {

  Optional<DepartmentOption> findDepartmentById(Long departmentId);

  Optional<JobRoleOption> findJobRoleById(Long jobRoleId);

  List<DepartmentOption> listActiveDepartments();

  List<JobRoleOption> listActiveJobRolesByDepartment(Long departmentId);

  record DepartmentOption(Long id, String name, boolean active) {}

  record JobRoleOption(Long id, String name, Long departmentId, boolean active) {}
}
