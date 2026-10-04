package br.com.easynr6.gestaoepi.ui.admin;

/** Texto de tela para usuários e papéis. O código da regra fica no log e na auditoria. */
public final class MensagensAdministracaoUsuario {

  private MensagensAdministracaoUsuario() {}

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para esta ação.";
    }
    if (mensagem.startsWith("AUTH-005")) {
      return "Já existe um usuário com esse login.";
    }
    if (mensagem.startsWith("AUTH-006")) {
      return "A senha precisa de 12 caracteres, com pelo menos três entre maiúscula, minúscula, número e símbolo, e não pode conter o login.";
    }
    if (mensagem.startsWith("AUTH-008")) {
      return "Escolha um papel.";
    }
    if (mensagem.startsWith("AUTH-009")) {
      return "Selecione um usuário da lista.";
    }
    if (mensagem.startsWith("AUTH-010")) {
      return "Informe o nome e o login.";
    }
    if (mensagem.startsWith("AUTH-017")) {
      return "Este usuário não tem o papel selecionado.";
    }
    if (erro instanceof IllegalArgumentException && !mensagem.isBlank()) {
      return mensagem.trim().replaceFirst("^[A-Z]+-\\d+\\s+", "");
    }
    return "Não foi possível salvar.";
  }
}
