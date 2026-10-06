package br.com.easynr6.gestaoepi.modules.issuance.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DevolucaoPolicyTest {

  private final DevolucaoPolicy policy = new DevolucaoPolicy();

  @Test
  void shouldRefuseADateBeforeTheIssuanceOrAfterToday() {
    LocalDate fornecimento = LocalDate.of(2026, 3, 10);
    LocalDate hoje = LocalDate.of(2026, 10, 5);

    IllegalArgumentException antes =
        assertThrows(
            IllegalArgumentException.class,
            () -> policy.data(fornecimento, LocalDate.of(2026, 3, 9), hoje));
    IllegalArgumentException futura =
        assertThrows(
            IllegalArgumentException.class,
            () -> policy.data(fornecimento, hoje.plusDays(1), hoje));

    assertTrue(antes.getMessage().startsWith("POS-002"));
    assertTrue(futura.getMessage().startsWith("POS-002"));
    policy.data(fornecimento, fornecimento, hoje);
  }

  @Test
  void shouldRequireTextWhenTheReasonIsOther() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> policy.motivo(MotivoDevolucao.OUTRO, " "));

    assertTrue(ex.getMessage().startsWith("POS-003"));
    policy.motivo(MotivoDevolucao.DESLIGAMENTO, null);
  }
}
