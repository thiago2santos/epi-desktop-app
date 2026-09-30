package br.com.easynr6.gestaoepi.modules.epi.domain;

public enum CaStatus {
  ACTIVE("Ativo"),
  SUSPENDED("Suspenso"),
  CANCELED("Cancelado"),
  EXPIRED("Expirado");

  private final String displayLabelPtBr;

  CaStatus(String displayLabelPtBr) {
    this.displayLabelPtBr = displayLabelPtBr;
  }

  public String displayLabelPtBr() {
    return displayLabelPtBr;
  }
}
