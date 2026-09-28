package br.com.easynr6.gestaoepi.shared.audit;

public interface AuditTrail {

  void registrarEventoCritico(
      Long usuarioId, String acao, String entidade, String entidadeId, String detalhes);
}
