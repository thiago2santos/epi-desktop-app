package br.com.easynr6.gestaoepi.shared.audit;

public interface AuditTrail {

  void registrarEventoCritico(
      Long usuarioId, String acao, String entidade, String entidadeId, String detalhes);

  default void registrarResultado(
      Long usuarioId,
      String acao,
      String entidade,
      String entidadeId,
      ResultadoAuditoria resultado,
      String codigo,
      String detalhes) {
    registrarEventoCritico(usuarioId, acao, entidade, entidadeId, detalhes);
  }
}
