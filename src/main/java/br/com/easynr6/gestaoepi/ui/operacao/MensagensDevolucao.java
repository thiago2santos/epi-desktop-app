package br.com.easynr6.gestaoepi.ui.operacao;

/** Texto de tela para o UC-POS-01. O código da regra fica no log e na auditoria. */
public final class MensagensDevolucao {

  public static final String CONFIRMAR =
      "Registrar esta devolução? O saldo da prateleira não muda.";
  public static final String VAZIO = "Nenhum item pendente de devolução para este trabalhador.";
  public static final String SUCESSO = "Devolução registrada.";

  private MensagensDevolucao() {}

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("POS-001")) {
      return "Este item não está pendente de devolução.";
    }
    if (mensagem.startsWith("POS-002")) {
      return "A data da devolução precisa ser no dia do fornecimento ou depois, até hoje.";
    }
    if (mensagem.startsWith("POS-003")) {
      return "Escolha o motivo. Se for outro, descreva.";
    }
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para registrar devolução.";
    }
    return "Não foi possível registrar a devolução.";
  }
}
