package br.com.easynr6.gestaoepi.modules.issuance.domain;

/** Texto do histórico. A consulta não grava auditoria e não mostra o motivo livre do estorno. */
public class HistoricoPolicy {

  public static final String FORNECIDO = "Fornecido";
  public static final String DEVOLVIDO = "Devolvido";
  public static final String ESTORNADO = "Estornado";

  public String situacaoFornecimento(boolean estornado, boolean devolvido) {
    if (estornado) {
      return ESTORNADO;
    }
    if (devolvido) {
      return DEVOLVIDO;
    }
    return FORNECIDO;
  }

  public String motivoFornecimento(String codigo) {
    String achado = codigo == null ? "" : codigo;
    for (MotivoFornecimento motivo : MotivoFornecimento.values()) {
      if (motivo.name().equals(codigo)) {
        achado = motivo.rotulo();
      }
    }
    return achado;
  }

  public String motivoDevolucao(String codigo) {
    String achado = codigo == null ? "" : codigo;
    for (MotivoDevolucao motivo : MotivoDevolucao.values()) {
      if (motivo.name().equals(codigo)) {
        achado = motivo.rotulo();
      }
    }
    return achado;
  }

  public String motivoEstorno() {
    return "";
  }
}
