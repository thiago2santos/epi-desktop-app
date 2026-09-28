package br.com.easynr6.gestaoepi.domain;

import java.time.LocalDate;
import java.util.Objects;

public final class EntregaRules {

  private EntregaRules() {}

  public static boolean podeEntregarLote(
      LocalDate validadePeca, int saldoAtual, int quantidadeSolicitada) {
    Objects.requireNonNull(validadePeca, "validadePeca obrigatoria");
    if (quantidadeSolicitada <= 0) {
      return false;
    }
    return !validadePeca.isBefore(LocalDate.now()) && saldoAtual >= quantidadeSolicitada;
  }

  public static boolean devolucaoEmDataValida(LocalDate dataEntrega, LocalDate dataDevolucao) {
    Objects.requireNonNull(dataEntrega, "dataEntrega obrigatoria");
    Objects.requireNonNull(dataDevolucao, "dataDevolucao obrigatoria");
    return !dataDevolucao.isBefore(dataEntrega);
  }
}
