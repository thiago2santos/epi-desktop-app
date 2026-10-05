package br.com.easynr6.gestaoepi.ui.cadastro;

/** Texto de tela para o UC-CAD-07. O código da regra fica no log e na auditoria. */
public final class MensagensCadastroGhe {

  private MensagensCadastroGhe() {}

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("CAD-051")) {
      return "Informe a unidade e o nome do GHE.";
    }
    if (mensagem.startsWith("CAD-052")) {
      return "Já existe um GHE com esse nome nesta unidade.";
    }
    if (mensagem.startsWith("CAD-053")) {
      return "Este GHE não foi encontrado.";
    }
    if (mensagem.startsWith("CAD-054")) {
      return "Escolha uma função ativa desta unidade.";
    }
    if (mensagem.startsWith("CAD-055")) {
      return "Esta função já está em outro GHE. Tire-a de lá antes.";
    }
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para esta ação.";
    }
    return "Não foi possível salvar o GHE.";
  }
}
