package br.com.easynr6.gestaoepi.ui.cadastro;

/** Texto de tela para o UC-CAD-02. O código da regra fica no log e na auditoria. */
public final class MensagensCadastroOrganizacional {

  private MensagensCadastroOrganizacional() {}

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("CAD-021")) {
      return "Já existe um setor com esse nome nesta unidade.";
    }
    if (mensagem.startsWith("CAD-022")) {
      return "Informe os campos obrigatórios.";
    }
    if (mensagem.startsWith("CAD-023")) {
      return "Selecione um setor da lista para editar.";
    }
    if (mensagem.startsWith("CAD-024")) {
      return "Não é possível inativar: este setor ainda tem função ativa.";
    }
    if (mensagem.startsWith("CAD-025")) {
      return "Já existe uma função com esse nome neste setor.";
    }
    if (mensagem.startsWith("CAD-026")) {
      return "Escolha um setor ativo.";
    }
    if (mensagem.startsWith("CAD-027") && mensagem.contains("Unidade")) {
      return "Escolha uma unidade ativa.";
    }
    if (mensagem.startsWith("CAD-027")) {
      return "Selecione uma função da lista para editar.";
    }
    if (mensagem.startsWith("CAD-028")) {
      return "Não é possível inativar: esta função ainda tem trabalhador ativo.";
    }
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para esta ação.";
    }
    if (erro instanceof IllegalArgumentException && !mensagem.isBlank()) {
      return mensagem.trim().replaceFirst("^[A-Z]+-\\d+\\s+", "");
    }
    return "Não foi possível salvar.";
  }
}
