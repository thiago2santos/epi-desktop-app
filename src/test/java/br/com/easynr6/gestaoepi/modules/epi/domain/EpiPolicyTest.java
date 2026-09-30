package br.com.easynr6.gestaoepi.modules.epi.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EpiPolicyTest {

  private final EpiPolicy policy = new EpiPolicy();

  @Test
  void shouldRejectMissingRequiredFields() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> policy.validateRequiredFields("", AnnexGroup.A, "Fabricante"));
    assertEquals("CAD-031 Campos obrigatorios de EPI ausentes.", ex.getMessage());
  }

  @Test
  void shouldNormalizeFields() {
    assertEquals("CAP-100", policy.normalizeEpiCode(" cap-100 "));
    assertEquals("Capacete Classe B", policy.normalizeDescription("  Capacete Classe B  "));
    assertEquals("Fabricante XPTO", policy.normalizeManufacturerName("  Fabricante XPTO  "));
  }
}
