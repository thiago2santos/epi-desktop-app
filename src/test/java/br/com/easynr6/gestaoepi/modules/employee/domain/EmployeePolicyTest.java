package br.com.easynr6.gestaoepi.modules.employee.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.JobRoleOption;
import org.junit.jupiter.api.Test;

class EmployeePolicyTest {

  private final EmployeePolicy policy = new EmployeePolicy();

  @Test
  void shouldNormalizeCodeAndName() {
    assertEquals("MTR-100", policy.normalizeCode("  mtr-100 "));
    assertEquals("Nome Completo", policy.normalizeName(" Nome Completo "));
  }

  @Test
  void shouldRejectMissingRequiredFields() {
    assertThrows(
        IllegalArgumentException.class, () -> policy.validateRequiredFields("", "Fulano", 1L, 1L));
    assertThrows(
        IllegalArgumentException.class, () -> policy.validateRequiredFields("MTR-1", "", 1L, 1L));
    assertThrows(
        IllegalArgumentException.class,
        () -> policy.validateRequiredFields("MTR-1", "Fulano", null, 1L));
    assertThrows(
        IllegalArgumentException.class,
        () -> policy.validateRequiredFields("MTR-1", "Fulano", 1L, null));
  }

  @Test
  void shouldRejectMismatchedDepartmentAndJobRole() {
    DepartmentOption department = new DepartmentOption(10L, "Operacao", true);
    JobRoleOption jobRole = new JobRoleOption(11L, "Tecnico", 99L, true);
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> policy.validateDepartmentAndRole(department, jobRole));
    assertEquals("EMP-003 Job role does not belong to selected department.", ex.getMessage());
  }

  @Test
  void shouldAcceptValidDepartmentAndRolePair() {
    DepartmentOption department = new DepartmentOption(10L, "Operacao", true);
    JobRoleOption jobRole = new JobRoleOption(11L, "Almoxarife", 10L, true);
    assertDoesNotThrow(() -> policy.validateDepartmentAndRole(department, jobRole));
  }
}
