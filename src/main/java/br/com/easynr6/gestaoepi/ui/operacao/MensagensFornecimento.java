package br.com.easynr6.gestaoepi.ui.operacao;

import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoFornecimento;

/** Texto de tela para o UC-ENT-01. O código da regra fica no log e na auditoria. */
public final class MensagensFornecimento {

  public static final String CONFIRMAR =
      "Confirmar fornecimento? Esta ficha não poderá ser editada.";
  public static final String TRABALHADOR_AUSENTE = "Trabalhador não encontrado ou inativo.";
  public static final String DICA_BUSCA =
      "A lista mostra todos. A partir de 3 caracteres, restringe por matrícula ou nome.";
  public static final String DICA_SETOR = "Filtra a lista por setor.";
  public static final String DICA_ATIVOS = "Esconde quem está inativo. Inativo não recebe EPI.";
  public static final String DICA_FORA =
      "Mostra os demais EPIs cadastrados, que não estão na matriz desta função.";
  public static final String DICA_EPIS =
      "Marque o que vai sair agora. Sim é o previsto para esta função.";
  public static final String FUNCAO_SEM_EPI = "Esta função não tem EPI previsto.";
  public static final String EXCECAO_CAMPO = "Por que entregar este EPI fora da função?";
  public static final String FORA_DA_MATRIZ =
      "Este EPI não está na matriz vigente. Só o SESMT pode registrar a exceção.";
  public static final String DICA_LOTE = "Lotes deste EPI na unidade do trabalhador.";
  public static final String DICA_MOTIVO = "Por que esta peça está saindo agora.";
  public static final String ESCOLHA_LOTE = "Escolha o lote";
  public static final String LOTE_VAZIO = "Nenhum lote deste EPI nesta unidade.";
  public static final String LOTE_VENCIDO =
      "Escolha um lote vigente deste EPI. Peça vencida não pode ser fornecida.";
  public static final String SEM_DISPONIVEL = "Não há quantidade disponível neste lote.";
  public static final String PRIMEIRA_BLOQUEADA =
      "Este trabalhador já tem este EPI. Primeira entrega não cabe.";
  public static final String TROCA_BLOQUEADA = "Não há entrega anterior deste EPI para trocar.";
  public static final String SEM_PECA = "Não há peça deste EPI com o trabalhador.";
  public static final String SEM_FICHA_ANTERIOR = "Não há ficha anterior.";
  public static final String FUNCAO_IGUAL = "A função desta ficha é a mesma da última entrega.";
  public static final String BUSCA_VAZIA = "Nenhum trabalhador encontrado.";
  public static final String BUSCA_VAZIA_ATIVOS = "Nenhum trabalhador ativo encontrado.";
  public static final String TRABALHADOR_INATIVO =
      "Este trabalhador está inativo e não pode receber EPI.";
  public static final String SENHA_ROTULO = "Senha de recebimento";
  public static final String DICA_SENHA =
      "O trabalhador digita a senha definida pelo SESMT. Ela não abre o sistema.";
  public static final String SEM_SENHA =
      "Este trabalhador ainda não tem senha de recebimento. O SESMT define a senha no cadastro.";
  public static final String SENHA_NAO_CONFERE = "A senha de recebimento não confere.";
  public static final String CIENCIA_CAIXA = "Eu recebi a orientação de uso";
  public static final String ORIENTACAO =
      "Registre a orientação de uso. Se a matriz exige treinamento, informe a data.";
  public static final String DICA_CIENCIA =
      "Marque depois de orientar o uso e o ajuste desta peça.";
  public static final String DICA_TREINAMENTO =
      "A matriz exige a data do treinamento. Ela não pode ser posterior a hoje.";
  public static final String REVISAO = "Revisão";
  public static final String FICHA = "Ficha de entrega de EPI";
  public static final String TERMO_TITULO = "Termo de responsabilidade";
  public static final String ORIENTACAO_REGISTRADA = "Orientação de uso registrada";
  public static final String TERMO_ACEITO = "Termo aceito.";
  public static final String CONSEQUENCIA =
      "Esta ficha não poderá ser editada. A quantidade sai do disponível.";
  public static final String FORA_DA_MATRIZ_LINHA = "Fora da matriz: ";

  private MensagensFornecimento() {}

  public static String motivoBloqueado(MotivoFornecimento motivo, boolean tinhaFicha) {
    return switch (motivo) {
      case PRIMEIRA_ENTREGA -> PRIMEIRA_BLOQUEADA;
      case TROCA_PERIODICA -> TROCA_BLOQUEADA;
      case DANO, EXTRAVIO -> SEM_PECA;
      case MUDANCA_FUNCAO -> tinhaFicha ? FUNCAO_IGUAL : SEM_FICHA_ANTERIOR;
      case OUTRO -> "";
    };
  }

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
      return LOTE_VENCIDO;
    }
    if (mensagem.startsWith("ENT-005")) {
      return SEM_DISPONIVEL;
    }
    if (mensagem.startsWith("ENT-006")) {
      return FORA_DA_MATRIZ;
    }
    if (mensagem.startsWith("ENT-007")) {
      return "Descreva a exceção com pelo menos 10 caracteres.";
    }
    if (mensagem.startsWith("ENT-008")) {
      return ORIENTACAO;
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
    if (mensagem.startsWith("ENT-014")) {
      return SEM_SENHA;
    }
    if (mensagem.startsWith("ENT-015")) {
      return SENHA_NAO_CONFERE;
    }
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para registrar fornecimento.";
    }
    return "Não foi possível registrar o fornecimento.";
  }
}
