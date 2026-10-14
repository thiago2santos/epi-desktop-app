package br.com.easynr6.gestaoepi.ui.cadastro;

import br.com.easynr6.gestaoepi.shared.audit.CodigoRegra;

/** Texto de tela para o UC-CAD-03. O código da regra fica no log e na auditoria. */
public final class MensagensCadastroTrabalhador {

  public static final String SENHA_ROTULO = "Senha de recebimento";
  public static final String SEM_SENHA = "Sem senha de recebimento.";
  public static final String SENHA_DEFINIDA = "Senha de recebimento definida.";
  public static final String DICA_SENHA =
      "O trabalhador digita esta senha no balcão. Ela não abre o sistema.";
  public static final String SENHA_RECUSADA =
      "A senha de recebimento precisa de 6 a 64 caracteres e não pode ser a matrícula nem uma"
          + " sequência óbvia.";
  public static final String SEM_PERMISSAO_SENHA =
      "Você não tem permissão para definir a senha de recebimento.";

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
    if (mensagem.startsWith("CAD-008")) {
      return SENHA_RECUSADA;
    }
    if (mensagem.contains("definir a senha de recebimento")) {
      return SEM_PERMISSAO_SENHA;
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
