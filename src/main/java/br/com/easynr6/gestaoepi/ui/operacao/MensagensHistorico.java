package br.com.easynr6.gestaoepi.ui.operacao;

/** Texto de tela para o UC-ENT-03. A consulta não mostra código de regra. */
public final class MensagensHistorico {

  public static final String VAZIO = "Nenhum fornecimento no período.";
  public static final String BUSCA = "Nenhum trabalhador com essa matrícula ou nome.";
  public static final String PERIODO = "A data final precisa ser no dia inicial ou depois.";

  private MensagensHistorico() {}

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para consultar o histórico.";
    }
    return "Não foi possível consultar o histórico.";
  }
}
