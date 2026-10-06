package br.com.easynr6.gestaoepi.ui.operacao;

import java.time.LocalDate;

/** A data final anterior à inicial para na tela e não chama a consulta. */
public final class HistoricoPeriodo {

  private HistoricoPeriodo() {}

  public static boolean valido(LocalDate inicio, LocalDate fim) {
    return inicio != null && fim != null && !fim.isBefore(inicio);
  }
}
