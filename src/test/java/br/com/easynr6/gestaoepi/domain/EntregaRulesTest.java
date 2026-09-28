package br.com.easynr6.gestaoepi.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EntregaRulesTest {

  @Test
  void devePermitirEntregaQuandoLoteValidoESaldoSuficiente() {
    boolean permitido = EntregaRules.podeEntregarLote(LocalDate.now().plusDays(5), 10, 2);
    assertTrue(permitido);
  }

  @Test
  void deveBloquearEntregaQuandoLoteVencido() {
    boolean permitido = EntregaRules.podeEntregarLote(LocalDate.now().minusDays(1), 10, 1);
    assertFalse(permitido);
  }

  @Test
  void deveBloquearEntregaQuandoSaldoInsuficiente() {
    boolean permitido = EntregaRules.podeEntregarLote(LocalDate.now().plusDays(2), 1, 2);
    assertFalse(permitido);
  }

  @Test
  void deveBloquearDevolucaoAnteriorAEntrega() {
    boolean valida = EntregaRules.devolucaoEmDataValida(LocalDate.now(), LocalDate.now().minusDays(1));
    assertFalse(valida);
  }

  @Test
  void deveAceitarDevolucaoNoMesmoDiaOuDepois() {
    LocalDate entrega = LocalDate.now();
    assertTrue(EntregaRules.devolucaoEmDataValida(entrega, entrega));
    assertTrue(EntregaRules.devolucaoEmDataValida(entrega, entrega.plusDays(3)));
  }
}
