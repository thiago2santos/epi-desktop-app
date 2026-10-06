package br.com.easynr6.gestaoepi.modules.issuance.domain;

/** Quem entra na lista de pendência. Posto e trabalhador ativo ficam de fora. */
public class PendenciaPolicy {

  public boolean entra(boolean trabalhadorAtivo, String modo) {
    if (trabalhadorAtivo) {
      return false;
    }
    return modo == null || "INDIVIDUAL".equals(modo);
  }
}
