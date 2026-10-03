package br.com.easynr6.gestaoepi.modules.employee.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeAssignment;
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
    DepartmentOption department = new DepartmentOption(10L, "Operacao", 1L, "Itupeva", true);
    JobRoleOption jobRole = new JobRoleOption(11L, "Tecnico", 99L, true);
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> policy.validateDepartmentAndRole(department, jobRole));
    assertEquals("CAD-003 Inconsistencia entre funcao e setor.", ex.getMessage());
  }

  @Test
  void shouldAcceptMissingManager() {
    assertDoesNotThrow(() -> policy.validateManager(2L, null, 1L, null));
  }

  @Test
  void shouldRejectManagerOutsideTheUnitOrInactive() {
    EmployeeAssignment otherUnit = new EmployeeAssignment(9L, 2L, true);
    EmployeeAssignment inactive = new EmployeeAssignment(9L, 1L, false);
    EmployeeAssignment self = new EmployeeAssignment(9L, 1L, true);
    assertThrows(
        IllegalArgumentException.class, () -> policy.validateManager(2L, 9L, 1L, otherUnit));
    assertThrows(
        IllegalArgumentException.class, () -> policy.validateManager(2L, 9L, 1L, inactive));
    assertThrows(IllegalArgumentException.class, () -> policy.validateManager(9L, 9L, 1L, self));
    assertDoesNotThrow(
        () -> policy.validateManager(2L, 9L, 1L, new EmployeeAssignment(9L, 1L, true)));
  }

  @Test
  void shouldAcceptValidDepartmentAndRolePair() {
    DepartmentOption department = new DepartmentOption(10L, "Operacao", 1L, "Itupeva", true);
    JobRoleOption jobRole = new JobRoleOption(11L, "Almoxarife", 10L, true);
    assertDoesNotThrow(() -> policy.validateDepartmentAndRole(department, jobRole));
  }
}
