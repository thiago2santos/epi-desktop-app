package br.com.easynr6.gestaoepi.ui.matriz;

/** Texto de tela para o UC-MAT-02. O código da regra fica no log e na auditoria. */
public final class MensagensPeriodicidade {

  private MensagensPeriodicidade() {}

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("MAT-005")) {
      return "A periodicidade precisa ser um número inteiro de dias, maior que zero.";
    }
    if (mensagem.startsWith("MAT-006")) {
      return "O aviso antecipado precisa ser zero ou mais, e menor que a periodicidade.";
    }
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para alterar a periodicidade.";
    }
    return "Não foi possível salvar a periodicidade.";
  }
}
