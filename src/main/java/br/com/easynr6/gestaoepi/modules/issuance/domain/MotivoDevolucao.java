package br.com.easynr6.gestaoepi.modules.issuance.domain;

/** Motivo da devolução, do descarte ou do extravio. */
public enum MotivoDevolucao {
  DESGASTE("Desgaste"),
  DANO("Dano"),
  DESCARTE("Descarte"),
  DESLIGAMENTO("Desligamento"),
  EXTRAVIO("Extravio"),
  OUTRO("Outro");

  private final String rotulo;

  MotivoDevolucao(String rotulo) {
    this.rotulo = rotulo;
  }

  public String rotulo() {
    return rotulo;
  }
}
