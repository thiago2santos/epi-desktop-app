package br.com.easynr6.gestaoepi.modules.matrix.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PeriodicidadePolicyTest {

  private final PeriodicidadePolicy policy = new PeriodicidadePolicy();
  private final LocalDate hoje = LocalDate.of(2026, 10, 5);

  @Test
  void shouldRejectZeroDaysAndWarningThatReachesThePeriod() {
    IllegalArgumentException dias =
        assertThrows(IllegalArgumentException.class, () -> policy.requireDias(0));
    IllegalArgumentException aviso =
        assertThrows(IllegalArgumentException.class, () -> policy.requireAviso(180, 180));

    assertTrue(dias.getMessage().startsWith("MAT-005"));
    assertTrue(aviso.getMessage().startsWith("MAT-006"));
  }

  @Test
  void shouldDescribeCoverageFromTheCountingIssuanceDate() {
    LocalDate ha10 = hoje.minusDays(10);
    LocalDate ha170 = hoje.minusDays(170);
    LocalDate ha180 = hoje.minusDays(180);

    assertEquals("Vigente", policy.situacao(ModoMatriz.INDIVIDUAL, 180, 15, ha10, hoje));
    assertEquals("Troca em 10 dias", policy.situacao(ModoMatriz.INDIVIDUAL, 180, 15, ha170, hoje));
    assertEquals("Prazo vencido", policy.situacao(ModoMatriz.INDIVIDUAL, 180, 15, ha180, hoje));
    assertEquals("Pendente", policy.situacao(ModoMatriz.INDIVIDUAL, 180, 15, null, hoje));
    assertEquals("Sem prazo", policy.situacao(ModoMatriz.INDIVIDUAL, null, null, ha10, hoje));
    assertEquals("Posto", policy.situacao(ModoMatriz.POSTO, null, null, null, hoje));
  }

  @Test
  void shouldUseTheNewPeriodWithoutChangingTheIssuanceDate() {
    LocalDate ficha = hoje.minusDays(100);

    assertEquals("Vigente", policy.situacao(ModoMatriz.INDIVIDUAL, 180, 15, ficha, hoje));
    assertEquals("Prazo vencido", policy.situacao(ModoMatriz.INDIVIDUAL, 90, 15, ficha, hoje));
    assertEquals(hoje.minusDays(100), ficha);
  }
}
