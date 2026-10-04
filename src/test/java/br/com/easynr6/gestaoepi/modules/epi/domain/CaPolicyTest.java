package br.com.easynr6.gestaoepi.modules.epi.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CaPolicyTest {

  private final CaPolicy policy = new CaPolicy();

  @Test
  void shouldRejectMissingOfficialEvidence() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                policy.validateRequiredFields(
                    "CA-1234", CaStatus.ACTIVE, null, "Consulta no CAEPI"));
    assertEquals("CAD-037 Evidencia de consulta oficial do CA ausente.", ex.getMessage());
  }

  @Test
  void shouldRejectActivationWhenStatusIsNotActive() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> policy.validateStatusForActivation(CaStatus.SUSPENDED, true));
    assertEquals("CAD-036 Situacao do CA impede ativacao do vinculo.", ex.getMessage());
  }

  @Test
  void shouldRejectInvalidValidityWindow() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                policy.validateValidityWindow(
                    LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 9)));
    assertEquals("CAD-035 CA com conflito de vigencia para o mesmo EPI.", ex.getMessage());
  }

  @Test
  void shouldNormalizeNumberAndNote() {
    assertEquals("1234", policy.normalizeCaNumber("  ca 1234  "));
    assertThrows(IllegalArgumentException.class, () -> policy.normalizeCaNumber("ABC"));
    assertThrows(IllegalArgumentException.class, () -> policy.rejeitarNotaVaga("ok"));
    assertEquals(
        "Conferencia realizada", policy.normalizeOfficialNote("  Conferencia realizada  "));
  }

  @Test
  void shouldAcceptValidFields() {
    policy.validateRequiredFields(
        "CA1234", CaStatus.ACTIVE, LocalDateTime.of(2026, 9, 29, 10, 0), "Consulta oficial");
  }
}
