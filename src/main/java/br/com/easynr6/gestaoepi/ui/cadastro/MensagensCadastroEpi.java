package br.com.easynr6.gestaoepi.ui.cadastro;

/** Texto de tela para UC-CAD-04/05. O código da regra fica no log e na auditoria. */
public final class MensagensCadastroEpi {

  private MensagensCadastroEpi() {}

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("CAD-031")) {
      return "Informe a descrição, o grupo do Anexo I e o fabricante.";
    }
    if (mensagem.startsWith("CAD-032")) {
      return "Escolha um grupo do Anexo I.";
    }
    if (mensagem.startsWith("CAD-033")) {
      return "Já existe um EPI com essa descrição, grupo e fabricante.";
    }
    if (mensagem.startsWith("CAD-034")) {
      return "Informe o número e a situação do CA.";
    }
    if (mensagem.startsWith("CAD-035")) {
      return "A vigência deste CA conflita com outro vínculo do mesmo EPI.";
    }
    if (mensagem.startsWith("CAD-036")) {
      return "Esta situação do CA não permite deixar o vínculo ativo.";
    }
    if (mensagem.startsWith("CAD-037")) {
      return "Informe a data e a evidência da consulta oficial.";
    }
    if (mensagem.startsWith("CAD-038")) {
      return "Não é possível alterar o status: confira o CA ativo e os vínculos deste EPI.";
    }
    if (mensagem.startsWith("CAD-039")) {
      return "Selecione um item da lista.";
    }
    if (mensagem.startsWith("AUTH-004")) {
      return "Você não tem permissão para esta ação.";
    }
    if (erro instanceof IllegalArgumentException && !mensagem.isBlank()) {
      return mensagem.trim().replaceFirst("^[A-Z]+-\\d+\\s+", "");
    }
    return "Não foi possível salvar.";
  }
}
