package br.com.easynr6.gestaoepi.modules.employee.domain;

import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeAssignment;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.JobRoleOption;
import java.util.Locale;
import java.util.Set;

public class EmployeePolicy {

  private static final Set<String> SENHAS_OBVIAS =
      Set.of("123456", "000000", "111111", "senha", "password");
  private static final String CAD_008 =
      "CAD-008 A senha de recebimento precisa de 6 a 64 caracteres e nao pode ser a matricula nem"
          + " uma sequencia obvia.";

  public void validateRequiredFields(
      String employeeCode, String fullName, Long departmentId, Long jobRoleId) {
    if (isBlank(employeeCode) || isBlank(fullName) || departmentId == null || jobRoleId == null) {
      throw new IllegalArgumentException("CAD-004 Campos obrigatorios ausentes.");
    }
  }

  public void validateUpdateFields(String fullName, Long departmentId, Long jobRoleId) {
    if (isBlank(fullName) || departmentId == null || jobRoleId == null) {
      throw new IllegalArgumentException("CAD-004 Campos obrigatorios ausentes.");
    }
  }

  public void validateManager(
      Long employeeId, Long managerId, Long unitId, EmployeeAssignment manager) {
    if (managerId == null) {
      return;
    }
    boolean proprio = employeeId != null && employeeId.equals(managerId);
    boolean vigente =
        manager != null && manager.active() && unitId != null && unitId.equals(manager.unitId());
    if (proprio || !vigente) {
      throw new IllegalArgumentException("CAD-007 Gestor invalido para este trabalhador.");
    }
  }

  public void validateDepartmentAndRole(DepartmentOption department, JobRoleOption jobRole) {
    if (department == null || !department.active()) {
      throw new IllegalArgumentException("CAD-002 Funcao invalida ou inativa.");
    }
    if (jobRole == null || !jobRole.active()) {
      throw new IllegalArgumentException("CAD-002 Funcao invalida ou inativa.");
    }
    if (!department.id().equals(jobRole.departmentId())) {
      throw new IllegalArgumentException("CAD-003 Inconsistencia entre funcao e setor.");
    }
  }

  public String normalizeCode(String employeeCode) {
    return employeeCode == null ? "" : employeeCode.trim().toUpperCase();
  }

  public String normalizeName(String fullName) {
    return fullName == null ? "" : fullName.trim();
  }

  /** Devolve a senha pronta para o hash. Em branco não é senha: o chamador não grava. */
  public String receiptPassword(String password, String employeeCode) {
    String limpa = password == null ? "" : password.trim();
    String matricula = employeeCode == null ? "" : employeeCode.trim();
    boolean obvia = SENHAS_OBVIAS.contains(limpa.toLowerCase(Locale.ROOT));
    boolean igualMatricula = !matricula.isEmpty() && limpa.equalsIgnoreCase(matricula);
    if (limpa.length() < 6 || limpa.length() > 64 || obvia || igualMatricula) {
      throw new IllegalArgumentException(CAD_008);
    }
    return limpa;
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
