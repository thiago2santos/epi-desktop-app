package br.com.easynr6.gestaoepi.modules.matrix.domain;

/** Peça de guarda pessoal, ou item de posto. */
public enum ModoMatriz {
  INDIVIDUAL("Individual"),
  POSTO("Posto");

  private final String rotulo;

  ModoMatriz(String rotulo) {
    this.rotulo = rotulo;
  }

  public String rotulo() {
    return rotulo;
  }
}
