package br.com.easynr6.gestaoepi.ui.operacao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class DevolucaoUxTest {

  @Test
  void shouldNotPersistWhenTheDialogReturns() {
    AtomicBoolean gravou = new AtomicBoolean();

    ConfirmacaoDevolucao.seguir(false, () -> gravou.set(true));

    assertFalse(gravou.get());
    ConfirmacaoDevolucao.seguir(true, () -> gravou.set(true));
    assertTrue(gravou.get());
  }

  @Test
  void shouldHideRuleCodes() {
    String texto = MensagensDevolucao.erro(new IllegalArgumentException("POS-001 item pendente"));

    assertFalse(texto.contains("POS-"));
    assertTrue(MensagensDevolucao.CONFIRMAR.contains("não muda"));
    assertTrue(MensagensDevolucao.VAZIO.contains("pendente de devolução"));
  }
}
