package br.com.easynr6.gestaoepi.ui.operacao;

/** Texto de tela para o UC-POS-03. A consulta não mostra código de regra. */
public final class MensagensPendencia {

  public static final String VAZIO = "Nenhuma pendência de devolução.";

  private MensagensPendencia() {}

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para consultar pendências de devolução.";
    }
    return "Não foi possível consultar as pendências.";
  }
}
