package br.com.easynr6.gestaoepi.ui.operacao;

/** Voltar no diálogo não chama o serviço. A ficha só nasce depois do Confirmar. */
public final class ConfirmacaoFornecimento {

  private ConfirmacaoFornecimento() {}

  public static void seguir(boolean confirmou, Runnable gravar) {
    if (confirmou && gravar != null) {
      gravar.run();
    }
  }
}
