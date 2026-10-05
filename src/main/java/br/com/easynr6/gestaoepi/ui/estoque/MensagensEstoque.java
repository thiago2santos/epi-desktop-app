package br.com.easynr6.gestaoepi.ui.estoque;

/** Texto de tela para o recebimento e a consulta de lote. O código da regra fica no log. */
public final class MensagensEstoque {

  public static final String FALHA_CONSULTA = "Não foi possível consultar os lotes.";

  private MensagensEstoque() {}

  public static String erroConsulta(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para esta ação.";
    }
    return FALHA_CONSULTA;
  }

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("LOT-001")) {
      return "Informe unidade, EPI, CA, lote, validade e quantidade.";
    }
    if (mensagem.startsWith("LOT-002")) {
      return "A quantidade precisa ser um número inteiro maior que zero.";
    }
    if (mensagem.startsWith("LOT-003")) {
      return "Já existe este lote para este EPI nesta unidade.";
    }
    if (mensagem.startsWith("LOT-004")) {
      return "Este EPI não tem CA ativo para receber.";
    }
    if (mensagem.startsWith("LOT-005")) {
      return "Escolha uma unidade ativa.";
    }
    if (mensagem.startsWith("LOT-007")) {
      return "O custo unitário não pode ser negativo.";
    }
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para esta ação.";
    }
    if (mensagem.contains("peca ja venceu")) {
      return "Esta peça já venceu. O lote entra sem quantidade disponível.";
    }
    if (erro instanceof IllegalArgumentException && !mensagem.isBlank()) {
      return mensagem.trim().replaceFirst("^[A-Z]+-\\d+\\s+", "");
    }
    return "Não foi possível receber o lote.";
  }
}
