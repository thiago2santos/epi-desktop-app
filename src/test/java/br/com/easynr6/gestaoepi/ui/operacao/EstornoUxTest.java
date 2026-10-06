package br.com.easynr6.gestaoepi.ui.operacao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class EstornoUxTest {

  @Test
  void shouldNotPersistWhenTheDialogReturns() {
    AtomicBoolean gravou = new AtomicBoolean();

    ConfirmacaoEstorno.seguir(false, () -> gravou.set(true));

    assertFalse(gravou.get());
    ConfirmacaoEstorno.seguir(true, () -> gravou.set(true));
    assertTrue(gravou.get());
  }

  @Test
  void shouldHideRuleCodes() {
    String texto = MensagensEstorno.erro(new IllegalArgumentException("POS-004 ja estornado"));

    assertFalse(texto.contains("POS-"));
    assertTrue(MensagensEstorno.CONFIRMAR.contains("saldo da prateleira volta"));
    assertTrue(MensagensEstorno.VAZIO.contains("em aberto para estornar"));
  }
}
