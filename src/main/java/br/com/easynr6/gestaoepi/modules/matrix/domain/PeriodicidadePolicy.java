package br.com.easynr6.gestaoepi.modules.matrix.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Regras de UC-MAT-02. Sem número salvo, a cobertura diz Sem prazo. */
public class PeriodicidadePolicy {

  public int requireDias(Integer dias) {
    if (dias == null || dias <= 0) {
      throw new IllegalArgumentException(
          "MAT-005 A periodicidade precisa ser um numero inteiro de dias, maior que zero.");
    }
    return dias;
  }

  public int requireAviso(int dias, Integer aviso) {
    if (aviso == null || aviso < 0 || aviso >= dias) {
      throw new IllegalArgumentException(
          "MAT-006 O aviso antecipado precisa ser zero ou mais, e menor que a periodicidade.");
    }
    return aviso;
  }

  /**
   * Texto da cobertura. Posto não vira pendência pessoal. A data do fornecimento não é gravada
   * aqui: quem consulta passa a data que ainda conta.
   */
  public String situacao(
      ModoMatriz modo, Integer dias, Integer aviso, LocalDate fornecimento, LocalDate hoje) {
    if (modo == ModoMatriz.POSTO) {
      return "Posto";
    }
    if (dias == null || aviso == null) {
      return "Sem prazo";
    }
    if (fornecimento == null || hoje == null) {
      return "Pendente";
    }
    long restantes = dias - ChronoUnit.DAYS.between(fornecimento, hoje);
    if (restantes > aviso) {
      return "Vigente";
    }
    if (restantes > 0) {
      return "Troca em " + restantes + " dias";
    }
    return "Prazo vencido";
  }
}
