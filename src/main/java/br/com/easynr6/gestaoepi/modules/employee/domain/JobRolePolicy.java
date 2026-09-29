package br.com.easynr6.gestaoepi.modules.employee.domain;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;

public class JobRolePolicy {

  public void validateRequiredFields(String name, Long departmentId) {
    if (name == null || name.trim().isEmpty() || departmentId == null) {
      throw new IllegalArgumentException("CAD-022 Campos obrigatorios ausentes.");
    }
  }

  public void validateActiveDepartment(DepartmentOption department) {
    if (department == null || !department.active()) {
      throw new IllegalArgumentException("CAD-026 Setor invalido ou inativo.");
    }
  }

  public String normalizeName(String name) {
    return name == null ? "" : name.trim();
  }
}
