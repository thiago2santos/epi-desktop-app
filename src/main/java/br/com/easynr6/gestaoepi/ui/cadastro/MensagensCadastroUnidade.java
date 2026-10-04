package br.com.easynr6.gestaoepi.ui.cadastro;

/** Texto de tela para o UC-CAD-01. O código da regra fica no log e na auditoria. */
public final class MensagensCadastroUnidade {

  private MensagensCadastroUnidade() {}

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("CAD-041")) {
      return "Informe o nome e o CNPJ.";
    }
    if (mensagem.startsWith("CAD-042")) {
      return "CNPJ inválido. Use os 14 dígitos, com ou sem máscara.";
    }
    if (mensagem.startsWith("CAD-043")) {
      return "Já existe uma unidade com esse CNPJ.";
    }
    if (mensagem.startsWith("CAD-044")) {
      return "Selecione uma unidade da lista para editar.";
    }
    if (mensagem.startsWith("CAD-045")) {
      return "Não é possível inativar: esta unidade ainda tem setor ativo.";
    }
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para esta ação.";
    }
    if (erro instanceof IllegalArgumentException && !mensagem.isBlank()) {
      return mensagem.trim().replaceFirst("^[A-Z]+-\\d+\\s+", "");
    }
    return "Não foi possível salvar a unidade.";
  }
}
