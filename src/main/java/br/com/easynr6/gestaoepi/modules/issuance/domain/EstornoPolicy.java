package br.com.easynr6.gestaoepi.modules.issuance.domain;

/** Regras do estorno. A tela não mostra o código POS. */
public class EstornoPolicy {

  public String motivo(String texto) {
    String limpo = texto == null ? "" : texto.trim();
    if (limpo.length() < 10) {
      throw new IllegalArgumentException(
          "POS-005 Descreva o motivo do estorno com pelo menos 10 caracteres.");
    }
    return limpo;
  }
}
