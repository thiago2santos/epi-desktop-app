package br.com.easynr6.gestaoepi.ui.operacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EntregaWizardStateTest {

  @Test
  void deveBloquearAvancoQuandoTrabalhadorNaoInformado() {
    EntregaWizardState state = new EntregaWizardState();
    assertFalse(state.canGoNext());
  }

  @Test
  void devePermitirAvancarAteComprovanteComDadosValidos() {
    EntregaWizardState state = new EntregaWizardState();
    state.setTrabalhador("Carlos Souza");
    assertTrue(state.canGoNext());
    state.goNext();
    assertEquals(2, state.currentStepNumber());

    state.setEpi("Capacete CA 12345");
    state.setLote("LT-2026-09");
    state.setValidadeLote(LocalDate.now().plusDays(10));
    state.setSaldoAtual(20);
    state.setQuantidadeSolicitada(2);
    assertTrue(state.canGoNext());
    state.goNext();

    state.setValidacaoResponsavel(true);
    assertTrue(state.canGoNext());
    state.goNext();

    state.setConfirmacaoOperacao(true);
    assertTrue(state.canGoNext());
    state.goNext();

    assertEquals(5, state.currentStepNumber());
    assertTrue(state.canFinalize());
  }

  @Test
  void deveBloquearPassoDoLoteQuandoRegraDeEntregaFalhar() {
    EntregaWizardState state = new EntregaWizardState();
    state.setTrabalhador("Carlos Souza");
    state.goNext();

    state.setEpi("Bota");
    state.setLote("LT-2025-01");
    state.setValidadeLote(LocalDate.now().minusDays(1));
    state.setSaldoAtual(10);
    state.setQuantidadeSolicitada(1);
    assertFalse(state.canGoNext());
  }

  @Test
  void deveLancarErroAoFinalizarForaDoUltimoPasso() {
    EntregaWizardState state = new EntregaWizardState();
    assertThrows(IllegalStateException.class, state::finalizar);
  }
}
