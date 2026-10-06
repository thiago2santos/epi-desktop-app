package br.com.easynr6.gestaoepi.ui.operacao;

/** Texto de tela para o UC-POS-02. O código da regra fica no log e na auditoria. */
public final class MensagensEstorno {

  public static final String CONFIRMAR =
      "Estornar este fornecimento? A ficha permanece e o saldo da prateleira volta.";
  public static final String VAZIO = "Nenhum fornecimento em aberto para estornar.";
  public static final String SUCESSO = "Fornecimento estornado.";

  private MensagensEstorno() {}

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("POS-004")) {
      return "Este fornecimento não pode ser estornado.";
    }
    if (mensagem.startsWith("POS-005")) {
      return "Descreva o motivo do estorno com pelo menos 10 caracteres.";
    }
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para estornar fornecimento.";
    }
    return "Não foi possível estornar o fornecimento.";
  }
}
