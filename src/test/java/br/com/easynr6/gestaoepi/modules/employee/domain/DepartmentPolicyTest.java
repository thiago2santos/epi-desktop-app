package br.com.easynr6.gestaoepi.modules.employee.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DepartmentPolicyTest {

  private final DepartmentPolicy policy = new DepartmentPolicy();

  @Test
  void shouldNormalizeName() {
    assertEquals("Operacao", policy.normalizeName(" Operacao "));
  }

  @Test
  void shouldRejectMissingName() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> policy.validateRequiredName(" "));
    assertEquals("CAD-022 Campos obrigatorios ausentes.", ex.getMessage());
  }

  @Test
  void shouldAcceptValidName() {
    assertDoesNotThrow(() -> policy.validateRequiredName("SESMT"));
  }
}
