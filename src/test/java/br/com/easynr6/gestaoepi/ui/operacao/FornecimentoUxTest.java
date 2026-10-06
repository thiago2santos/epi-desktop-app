package br.com.easynr6.gestaoepi.ui.operacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class FornecimentoUxTest {

  @Test
  void shouldNotPersistWhenTheDialogReturns() {
    AtomicBoolean gravou = new AtomicBoolean();

    ConfirmacaoFornecimento.seguir(false, () -> gravou.set(true));

    assertFalse(gravou.get());
    ConfirmacaoFornecimento.seguir(true, () -> gravou.set(true));
    assertTrue(gravou.get());
  }

  @Test
  void shouldHideRuleCodesAndParseQuantity() {
    String texto =
        MensagensFornecimento.erro(
            new IllegalArgumentException("ENT-004 Escolha um lote vigente deste EPI."));

    assertFalse(texto.contains("ENT-"));
    assertTrue(texto.contains("Peça vencida"));
    assertTrue(MensagensFornecimento.CONFIRMAR.contains("não poderá ser editada"));
    assertNull(FornecimentoWizardView.quantidade("0"));
    assertEquals(2, FornecimentoWizardView.quantidade("2"));
    assertTrue(MensagensFornecimento.sucesso(15).startsWith("Fornecimento registrado."));
  }
}
