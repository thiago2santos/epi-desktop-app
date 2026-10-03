package br.com.easynr6.gestaoepi.ui.cadastro;

import br.com.easynr6.gestaoepi.shared.audit.CodigoRegra;

/** Texto de tela para o UC-CAD-03. O código da regra fica no log e na auditoria. */
public final class MensagensCadastroTrabalhador {

  private MensagensCadastroTrabalhador() {}

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("CAD-001")) {
      return "Matrícula já cadastrada.";
    }
    if (mensagem.startsWith("CAD-002")) {
      return "Função ou setor inválido ou inativo. Escolha um setor ativo e uma função desse setor.";
    }
    if (mensagem.startsWith("CAD-003")) {
      return "A função não pertence ao setor escolhido.";
    }
    if (mensagem.startsWith("CAD-004")) {
      return "Informe matrícula, nome, setor e função.";
    }
    if (mensagem.startsWith("CAD-005")) {
      return "Não é possível inativar: o histórico deste trabalhador impede a operação.";
    }
    if (mensagem.startsWith("CAD-007")) {
      return "Escolha um gestor ativo da mesma unidade, ou deixe sem gestor.";
    }
    if (mensagem.startsWith("CAD-006")) {
      return "Selecione um trabalhador da lista para editar.";
    }
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para esta ação.";
    }
    if (erro instanceof IllegalArgumentException && !mensagem.isBlank()) {
      return CodigoRegra.semCodigo(mensagem);
    }
    return "Não foi possível salvar o trabalhador.";
  }
}
