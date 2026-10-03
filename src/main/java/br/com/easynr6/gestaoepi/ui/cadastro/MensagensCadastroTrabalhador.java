package br.com.easynr6.gestaoepi.ui.cadastro;

/** Texto de tela para os códigos do UC-CAD-03. O código permanece visível. */
public final class MensagensCadastroTrabalhador {

  private MensagensCadastroTrabalhador() {}

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("CAD-001")) {
      return "CAD-001 Matrícula já cadastrada.";
    }
    if (mensagem.startsWith("CAD-002")) {
      return "CAD-002 Função ou setor inválido ou inativo. Escolha um setor ativo e uma função desse setor.";
    }
    if (mensagem.startsWith("CAD-003")) {
      return "CAD-003 A função não pertence ao setor escolhido.";
    }
    if (mensagem.startsWith("CAD-004")) {
      return "CAD-004 Informe matrícula, nome, setor e função.";
    }
    if (mensagem.startsWith("CAD-005")) {
      return "CAD-005 Não é possível inativar: o histórico deste trabalhador impede a operação.";
    }
    if (mensagem.startsWith("CAD-007")) {
      return "CAD-007 Escolha um gestor ativo da mesma unidade, ou deixe sem gestor.";
    }
    if (mensagem.startsWith("CAD-006")) {
      return "CAD-006 Selecione um trabalhador da lista para editar.";
    }
    if (mensagem.startsWith("AUTH-004")) {
      return "AUTH-004 Você não tem permissão para esta ação.";
    }
    if (erro instanceof IllegalArgumentException && !mensagem.isBlank()) {
      return mensagem;
    }
    return "Não foi possível salvar o trabalhador.";
  }
}
