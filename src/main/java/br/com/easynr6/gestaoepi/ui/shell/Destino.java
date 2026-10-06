package br.com.easynr6.gestaoepi.ui.shell;

import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.util.EnumSet;
import java.util.List;

/** Telas do shell, agrupadas pelo fluxo operacional. */
public enum Destino {
  DASHBOARD(Grupo.INICIO, "Dashboard", null),
  ENTREGA(
      Grupo.OPERACAO, "Registrar fornecimento", papeis(Papel.ADMIN, Papel.SESMT, Papel.ALMOXARIFE)),
  DEVOLUCAO(
      Grupo.OPERACAO, "Devolução / descarte", papeis(Papel.ADMIN, Papel.SESMT, Papel.ALMOXARIFE)),
  ESTORNO(Grupo.OPERACAO, "Estorno", papeis(Papel.ADMIN, Papel.SESMT)),
  HISTORICO(
      Grupo.OPERACAO,
      "Histórico por trabalhador",
      papeis(Papel.ADMIN, Papel.SESMT, Papel.ALMOXARIFE, Papel.CONSULTA)),
  SOLICITAR(Grupo.DEMANDA, "Solicitar EPI (Gestor)", papeis(Papel.ADMIN)),
  FILA(Grupo.DEMANDA, "Fila de solicitações", papeis(Papel.ADMIN, Papel.SESMT, Papel.ALMOXARIFE)),
  TRABALHADORES(Grupo.CADASTROS, "Trabalhadores", papeis(Papel.ADMIN, Papel.SESMT)),
  UNIDADES(Grupo.CADASTROS, "Unidades", papeis(Papel.ADMIN, Papel.SESMT)),
  SETORES(Grupo.CADASTROS, "Setores & funções", papeis(Papel.ADMIN, Papel.SESMT)),
  GHE(Grupo.CADASTROS, "GHE", papeis(Papel.ADMIN, Papel.SESMT)),
  EPI(Grupo.CADASTROS, "Catálogo EPI", papeis(Papel.ADMIN, Papel.SESMT)),
  CA(Grupo.CADASTROS, "CA por EPI", papeis(Papel.ADMIN, Papel.SESMT)),
  LOTES(
      Grupo.ESTOQUE,
      "Lotes & saldos",
      papeis(Papel.ADMIN, Papel.SESMT, Papel.ALMOXARIFE, Papel.CONSULTA)),
  MATRIZ(Grupo.REGRAS, "Matriz função × EPI", papeis(Papel.ADMIN, Papel.SESMT)),
  PERIODICIDADE(Grupo.REGRAS, "Periodicidade", papeis(Papel.ADMIN, Papel.SESMT)),
  RELATORIOS(Grupo.RELATORIOS, "Hub de relatórios", null),
  COBERTURA(
      Grupo.RELATORIOS,
      "Cobertura",
      papeis(Papel.ADMIN, Papel.SESMT, Papel.ALMOXARIFE, Papel.CONSULTA)),
  PENDENCIAS(Grupo.RELATORIOS, "Pendências", papeis(Papel.ADMIN, Papel.SESMT, Papel.ALMOXARIFE)),
  AUDITORIA(Grupo.GOVERNANCA, "Auditoria", papeis(Papel.ADMIN, Papel.SESMT, Papel.CONSULTA)),
  USUARIOS(Grupo.GOVERNANCA, "Usuários & papéis", papeis(Papel.ADMIN)),
  PARAMETROS(Grupo.GOVERNANCA, "Parâmetros", papeis(Papel.ADMIN)),
  CAEPI(Grupo.GOVERNANCA, "Importação CAEPI", papeis(Papel.ADMIN, Papel.SESMT));

  private final Grupo grupo;
  private final String label;
  private final EnumSet<Papel> papeis;

  Destino(Grupo grupo, String label, EnumSet<Papel> papeis) {
    this.grupo = grupo;
    this.label = label;
    this.papeis = papeis;
  }

  public Grupo grupo() {
    return grupo;
  }

  public String label() {
    return label;
  }

  public boolean visivelPara(java.util.function.Predicate<Papel> temPapel) {
    if (papeis == null) {
      return true;
    }
    return papeis.stream().anyMatch(temPapel);
  }

  private static EnumSet<Papel> papeis(Papel primeiro, Papel... resto) {
    return EnumSet.of(primeiro, resto);
  }

  public enum Grupo {
    INICIO("Início"),
    OPERACAO("Operação"),
    DEMANDA("Demanda"),
    CADASTROS("Cadastros"),
    ESTOQUE("Estoque"),
    REGRAS("Regras"),
    RELATORIOS("Relatórios"),
    GOVERNANCA("Governança");

    private final String titulo;

    Grupo(String titulo) {
      this.titulo = titulo;
    }

    public String titulo() {
      return titulo;
    }

    public List<Destino> destinos() {
      return List.of(Destino.values()).stream().filter(destino -> destino.grupo == this).toList();
    }
  }
}
