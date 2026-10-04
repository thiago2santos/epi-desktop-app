package br.com.easynr6.gestaoepi.modules.stock.domain;

/** A peça vencida só entra depois que a tela confirma. Sem o aceite, nada é gravado. */
public class PecaVencidaNaoConfirmadaException extends IllegalArgumentException {

  public PecaVencidaNaoConfirmadaException() {
    super("Esta peca ja venceu. O lote entra sem quantidade disponivel.");
  }
}
