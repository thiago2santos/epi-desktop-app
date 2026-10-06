package br.com.easynr6.gestaoepi.modules.issuance.domain;

import java.time.LocalDate;

/** Regras da devolução. A tela não mostra o código POS. */
public class DevolucaoPolicy {

  public void data(LocalDate fornecimento, LocalDate escolhida, LocalDate hoje) {
    if (fornecimento == null
        || escolhida == null
        || hoje == null
        || escolhida.isBefore(fornecimento)
        || escolhida.isAfter(hoje)) {
      throw new IllegalArgumentException(
          "POS-002 A data da devolucao precisa ser no dia do fornecimento ou depois, ate hoje.");
    }
  }

  public void motivo(MotivoDevolucao motivo, String texto) {
    if (motivo == null || (motivo == MotivoDevolucao.OUTRO && emBranco(texto))) {
      throw new IllegalArgumentException("POS-003 Escolha o motivo. Se for outro, descreva.");
    }
  }

  private static boolean emBranco(String texto) {
    return texto == null || texto.isBlank();
  }
}
