package br.com.easynr6.gestaoepi.shared.audit;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditQueryService {

  private static final String LIST_AUDIT_EVENTS_SQL =
      """
      SELECT a.id,
             a.instante,
             a.usuario_id,
             u.login AS usuario_login,
             a.acao,
             a.entidade,
             a.entidade_id,
             a.detalhes
      FROM auditoria a
      LEFT JOIN usuario u ON u.id = a.usuario_id
      WHERE (? = ''
          OR a.acao LIKE ?
          OR a.entidade LIKE ?
          OR a.entidade_id LIKE ?
          OR IFNULL(a.detalhes, '') LIKE ?
          OR IFNULL(u.login, '') LIKE ?)
      ORDER BY a.id DESC
      LIMIT ?
      """;

  private final JdbcTemplate jdbcTemplate;

  public AuditQueryService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<AuditEventSummary> listEvents(String term, int limitRows) {
    String cleanTerm = term == null ? "" : term.trim();
    int safeLimit = limitRows <= 0 ? 200 : Math.min(limitRows, 1000);
    return jdbcTemplate.query(
        LIST_AUDIT_EVENTS_SQL,
        ps -> {
          ps.setString(1, cleanTerm);
          ps.setString(2, "%" + cleanTerm + "%");
          ps.setString(3, "%" + cleanTerm + "%");
          ps.setString(4, "%" + cleanTerm + "%");
          ps.setString(5, "%" + cleanTerm + "%");
          ps.setString(6, "%" + cleanTerm + "%");
          ps.setInt(7, safeLimit);
        },
        (rs, rowNum) ->
            new AuditEventSummary(
                rs.getLong("id"),
                rs.getString("instante"),
                rs.getLong("usuario_id"),
                rs.getString("usuario_login"),
                rs.getString("acao"),
                rs.getString("entidade"),
                rs.getString("entidade_id"),
                rs.getString("detalhes")));
  }

  public record AuditEventSummary(
      Long id,
      String timestamp,
      Long userId,
      String userLogin,
      String action,
      String entity,
      String entityId,
      String details) {}
}
