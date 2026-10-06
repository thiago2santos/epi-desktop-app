package br.com.easynr6.gestaoepi.modules.issuance.application.port;

import java.time.LocalDateTime;

/** Append de BAIXA_FORNECIMENTO. Falha aqui desfaz a ficha inteira. */
public interface BaixaFornecimento {

  void registrar(Baixa baixa);

  record Baixa(
      long loteId,
      int quantidade,
      Integer custoCentavos,
      String unidade,
      String setor,
      String funcao,
      long epiId,
      LocalDateTime quando) {}
}
