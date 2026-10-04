package br.com.easynr6.gestaoepi.shared.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditService implements AuditTrail {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);
  private static final int DETALHES_MAXIMO = 400;

  private static final String INSERT_AUDITORIA_SQL =
      """
      INSERT INTO auditoria (
        instante, usuario_id, acao, entidade, entidade_id, detalhes, resultado, codigo, correlacao
      )
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      """;

  private final JdbcTemplate jdbcTemplate;

  public AuditService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void registrarEventoCritico(
      Long usuarioId, String acao, String entidade, String entidadeId, String detalhes) {
    registrarResultado(
        usuarioId, acao, entidade, entidadeId, ResultadoAuditoria.SUCESSO, null, detalhes);
  }

  @Override
  public void registrarResultado(
      Long usuarioId,
      String acao,
      String entidade,
      String entidadeId,
      ResultadoAuditoria resultado,
      String codigo,
      String detalhes) {
    ResultadoAuditoria desfecho = resultado == null ? ResultadoAuditoria.SUCESSO : resultado;
    String correlacao = Correlacao.atual();
    if (correlacao == null) {
      correlacao = Correlacao.novoId();
    }
    String texto = abreviar(detalhes);
    jdbcTemplate.update(
        INSERT_AUDITORIA_SQL,
        InstanteAuditoria.agora(),
        usuarioId,
        acao,
        entidade,
        entidadeId == null || entidadeId.isBlank() ? "-" : entidadeId,
        texto,
        desfecho.name(),
        codigo,
        correlacao);
    if (desfecho == ResultadoAuditoria.FALHA) {
      LOGGER.warn(
          "auditoria resultado={} codigo={} acao={} entidade={} entidadeId={} usuarioId={} correlacao={}",
          desfecho,
          codigo,
          acao,
          entidade,
          entidadeId,
          usuarioId,
          correlacao);
      return;
    }
    LOGGER.info(
        "auditoria resultado={} codigo={} acao={} entidade={} entidadeId={} usuarioId={} correlacao={}",
        desfecho,
        codigo,
        acao,
        entidade,
        entidadeId,
        usuarioId,
        correlacao);
  }

  private static String abreviar(String detalhes) {
    if (detalhes == null || detalhes.isBlank()) {
      return null;
    }
    String texto = detalhes.trim();
    if (texto.length() <= DETALHES_MAXIMO) {
      return texto;
    }
    return texto.substring(0, DETALHES_MAXIMO);
  }
}
