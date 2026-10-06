package br.com.easynr6.gestaoepi.ui.operacao;

/** Voltar no diálogo não chama o serviço. */
public final class ConfirmacaoDevolucao {

  private ConfirmacaoDevolucao() {}

  public static void seguir(boolean confirmou, Runnable gravar) {
    if (confirmou && gravar != null) {
      gravar.run();
    }
  }
}
