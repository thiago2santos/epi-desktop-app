package br.com.easynr6.gestaoepi.modules.employee.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import org.junit.jupiter.api.Test;

class JobRolePolicyTest {

  private final JobRolePolicy policy = new JobRolePolicy();

  @Test
  void shouldNormalizeName() {
    assertEquals("Almoxarife", policy.normalizeName(" Almoxarife "));
  }

  @Test
  void shouldRejectMissingRequiredFields() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> policy.validateRequiredFields(" ", null));
    assertEquals("CAD-022 Campos obrigatorios ausentes.", ex.getMessage());
  }

  @Test
  void shouldRejectInactiveDepartment() {
    DepartmentOption department = new DepartmentOption(1L, "Operacao", 1L, "Itupeva", false);
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> policy.validateActiveDepartment(department));
    assertEquals("CAD-026 Setor invalido ou inativo.", ex.getMessage());
  }

  @Test
  void shouldAcceptActiveDepartment() {
    DepartmentOption department = new DepartmentOption(1L, "Operacao", 1L, "Itupeva", true);
    assertDoesNotThrow(() -> policy.validateActiveDepartment(department));
  }
}
