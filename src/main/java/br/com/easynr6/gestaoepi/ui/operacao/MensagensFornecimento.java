package br.com.easynr6.gestaoepi.ui.operacao;

/** Texto de tela para o UC-ENT-01. O código da regra fica no log e na auditoria. */
public final class MensagensFornecimento {

  public static final String CONFIRMAR =
      "Confirmar fornecimento? Esta ficha não poderá ser editada.";
  public static final String LEGAL =
      "Ao confirmar, a ficha fica registrada. Correção posterior é um estorno, não uma edição.";
  public static final String TRABALHADOR_AUSENTE = "Trabalhador não encontrado ou inativo.";
  public static final String TERMO_CAIXA =
      "Trabalhador aceitou o termo de responsabilidade (assinatura no balcão).";

  private MensagensFornecimento() {}

  public static String sucesso(long fichaId) {
    return "Fornecimento registrado. Ficha " + fichaId + ".";
  }

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("ENT-001")) {
      return TRABALHADOR_AUSENTE;
    }
    if (mensagem.startsWith("ENT-002")) {
      return "Inclua ao menos um EPI na ficha.";
    }
    if (mensagem.startsWith("ENT-003")) {
      return "A quantidade precisa ser um número inteiro maior que zero.";
    }
    if (mensagem.startsWith("ENT-004")) {
      return "Escolha um lote vigente deste EPI. Peça vencida não pode ser fornecida.";
    }
    if (mensagem.startsWith("ENT-005")) {
      return "Não há quantidade disponível neste lote.";
    }
    if (mensagem.startsWith("ENT-006")) {
      return "Este EPI não está na matriz vigente. Só o SESMT pode registrar a exceção.";
    }
    if (mensagem.startsWith("ENT-007")) {
      return "Descreva a exceção com pelo menos 10 caracteres.";
    }
    if (mensagem.startsWith("ENT-008")) {
      return "Registre a orientação de uso. Se a matriz exige treinamento, informe a data.";
    }
    if (mensagem.startsWith("ENT-009")) {
      return "O fornecimento só conclui com o aceite do termo.";
    }
    if (mensagem.startsWith("ENT-010")) {
      return "Escolha o motivo. Se for outro, descreva.";
    }
    if (mensagem.startsWith("ENT-011")) {
      return "A reserva não é deste lote ou não cobre a quantidade.";
    }
    if (mensagem.startsWith("ENT-012")) {
      return "Este lote já está nesta ficha. Ajuste a quantidade da linha.";
    }
    if (mensagem.startsWith("ENT-013")) {
      return "O pedido não cobre este trabalhador, EPI ou quantidade.";
    }
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para registrar fornecimento.";
    }
    return "Não foi possível registrar o fornecimento.";
  }
}
