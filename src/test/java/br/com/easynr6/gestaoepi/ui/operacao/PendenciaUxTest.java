package br.com.easynr6.gestaoepi.ui.operacao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PendenciaUxTest {

  @Test
  void shouldShowTheEmptyPhraseWithoutARuleCode() {
    assertTrue(MensagensPendencia.VAZIO.contains("pendência de devolução"));
    assertFalse(
        MensagensPendencia.erro(new IllegalArgumentException("AUTH-004 x")).contains("AUTH-"));
  }
}
