package br.com.easynr6.gestaoepi.shared.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rastro técnico de uma exceção já tratada. A frase ao operador fica na tela; a causa fica aqui.
 */
public final class LogTroubleshooting {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogTroubleshooting.class);

  private LogTroubleshooting() {}

  public static void registrar(String acao, Long usuarioId, String alvo, Throwable erro) {
    boolean abertaAqui = Correlacao.abrir();
    try {
      String codigo = CodigoRegra.extrair(erro == null ? null : erro.getMessage());
      String alvoLog = alvo == null || alvo.isBlank() ? "-" : alvo;
      if (codigo == null) {
        LOGGER.error(
            "troubleshooting acao={} usuarioId={} alvo={} correlacao={}",
            acao,
            usuarioId,
            alvoLog,
            Correlacao.atual(),
            erro);
        return;
      }
      LOGGER.warn(
          "troubleshooting acao={} codigo={} usuarioId={} alvo={} detalhe={} correlacao={}",
          acao,
          codigo,
          usuarioId,
          alvoLog,
          erro.getMessage(),
          Correlacao.atual());
    } finally {
      Correlacao.fechar(abertaAqui);
    }
  }
}
