package br.com.easynr6.gestaoepi.modules.issuance.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HistoricoPolicyTest {

  private final HistoricoPolicy policy = new HistoricoPolicy();

  @Test
  void shouldKeepTheReversedIssuanceVisibleAndHideTheReversalText() {
    assertEquals(HistoricoPolicy.ESTORNADO, policy.situacaoFornecimento(true, false));
    assertEquals(HistoricoPolicy.DEVOLVIDO, policy.situacaoFornecimento(false, true));
    assertEquals(HistoricoPolicy.FORNECIDO, policy.situacaoFornecimento(false, false));
    assertEquals("", policy.motivoEstorno());
    assertEquals("Primeira entrega", policy.motivoFornecimento("PRIMEIRA_ENTREGA"));
    assertEquals("Desligamento", policy.motivoDevolucao("DESLIGAMENTO"));
  }
}
