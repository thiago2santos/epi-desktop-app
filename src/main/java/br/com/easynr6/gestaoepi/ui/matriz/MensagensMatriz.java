package br.com.easynr6.gestaoepi.ui.matriz;

/** Texto de tela para o UC-MAT-01. O código da regra fica no log e na auditoria. */
public final class MensagensMatriz {

  private MensagensMatriz() {}

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("MAT-001")) {
      return "Escolha o perfil e o EPI.";
    }
    if (mensagem.startsWith("MAT-002")) {
      return "Este EPI já está na matriz deste perfil.";
    }
    if (mensagem.startsWith("MAT-003")) {
      return "Este EPI não tem CA ativo para entrar na matriz.";
    }
    if (mensagem.startsWith("MAT-004")) {
      return "Escolha um perfil ativo e um EPI ativo.";
    }
    if (mensagem.startsWith("MAT-007")) {
      return "Esta linha da matriz não está ativa.";
    }
    if (mensagem.startsWith("MAT-008")) {
      return "Esta função usa a lista do GHE. Edite o grupo.";
    }
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para alterar a matriz.";
    }
    return "Não foi possível salvar a matriz.";
  }
}
