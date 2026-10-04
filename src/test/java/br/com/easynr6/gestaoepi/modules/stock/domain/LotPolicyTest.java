package br.com.easynr6.gestaoepi.modules.stock.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LotPolicyTest {

  private final LotPolicy policy = new LotPolicy();

  @Test
  void obrigatoriosAusentesSaoLot001() {
    IllegalArgumentException codigo =
        assertThrows(IllegalArgumentException.class, () -> policy.codigo("  "));
    IllegalArgumentException quantidade =
        assertThrows(IllegalArgumentException.class, () -> policy.quantidade(null));

    assertTrue(codigo.getMessage().startsWith("LOT-001"));
    assertTrue(quantidade.getMessage().startsWith("LOT-001"));
  }

  @Test
  void quantidadeInvalidaELot002() {
    assertTrue(rejeitaQuantidade("0").startsWith("LOT-002"));
    assertTrue(rejeitaQuantidade("1,5").startsWith("LOT-002"));
    assertTrue(rejeitaQuantidade("1.5").startsWith("LOT-002"));
    assertEquals(10, policy.quantidade("10"));
  }

  @Test
  void custoEmBrancoFicaNuloENegativoELot007() {
    assertNull(policy.custoCentavos(" "));
    assertEquals(0, policy.custoCentavos("0"));
    assertEquals(1250, policy.custoCentavos("12,50"));
    IllegalArgumentException negativo =
        assertThrows(IllegalArgumentException.class, () -> policy.custoCentavos("-1"));
    assertTrue(negativo.getMessage().startsWith("LOT-007"));
  }

  @Test
  void pecaVencidaNasceComDisponivelZero() {
    LocalDate hoje = LocalDate.of(2026, 10, 4);
    LotPolicy.Saldo saldo = policy.saldo(10, 0, hoje.minusDays(1), hoje);

    assertEquals(10, saldo.fisica());
    assertEquals(0, saldo.reservada());
    assertEquals(0, saldo.disponivel());
    assertEquals("Vencido", saldo.situacao());
  }

  @Test
  void pecaNoPrazoDisponibilizaAFisica() {
    LocalDate hoje = LocalDate.of(2026, 10, 4);
    LotPolicy.Saldo saldo = policy.saldo(10, 0, hoje, hoje);

    assertEquals(10, saldo.disponivel());
    assertEquals("Vigente", saldo.situacao());
  }

  private String rejeitaQuantidade(String valor) {
    return assertThrows(IllegalArgumentException.class, () -> policy.quantidade(valor))
        .getMessage();
  }
}
