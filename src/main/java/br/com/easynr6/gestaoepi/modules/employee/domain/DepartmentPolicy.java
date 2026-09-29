package br.com.easynr6.gestaoepi.modules.employee.domain;

public class DepartmentPolicy {

  public void validateRequiredName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("CAD-022 Campos obrigatorios ausentes.");
    }
  }

  public String normalizeName(String name) {
    return name == null ? "" : name.trim();
  }
}
