package br.com.easynr6.gestaoepi.shared.audit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

  private static final String INSERT_AUDITORIA_SQL =
      """
      INSERT INTO auditoria (usuario_id, acao, entidade, entidade_id, detalhes)
      VALUES (?, ?, ?, ?, ?)
      """;

  private final JdbcTemplate jdbcTemplate;

  public AuditService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void registrarEventoCritico(
      Long usuarioId, String acao, String entidade, String entidadeId, String detalhes) {
    jdbcTemplate.update(
        INSERT_AUDITORIA_SQL,
        usuarioId,
        acao,
        entidade,
        entidadeId == null ? "-" : entidadeId,
        detalhes);
  }
}
