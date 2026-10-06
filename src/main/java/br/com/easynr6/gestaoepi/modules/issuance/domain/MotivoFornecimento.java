package br.com.easynr6.gestaoepi.modules.issuance.domain;

/** Motivo da saída do EPI na ficha. */
public enum MotivoFornecimento {
  PRIMEIRA_ENTREGA("Primeira entrega"),
  TROCA_PERIODICA("Troca periódica"),
  DANO("Dano"),
  EXTRAVIO("Extravio"),
  MUDANCA_FUNCAO("Mudança de função"),
  OUTRO("Outro");

  private final String rotulo;

  MotivoFornecimento(String rotulo) {
    this.rotulo = rotulo;
  }

  public String rotulo() {
    return rotulo;
  }
}
