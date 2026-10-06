package br.com.easynr6.gestaoepi.modules.issuance.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.issuance.application.port.PendenciaRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPendenciaRepository implements PendenciaRepository {

  private static final String SQL =
      """
      SELECT i.id AS item_id, emp.id AS employee_id, emp.employee_code, emp.full_name,
             e.description AS epi, substr(f.confirmed_at, 1, 10) AS dia, i.quantity, u.id AS unit_id
      FROM fornecimento_item i
      JOIN fornecimento_ficha f ON f.id = i.ficha_id
      JOIN employee emp ON emp.id = f.employee_id
      JOIN department dep ON dep.id = emp.department_id
      JOIN unit u ON u.id = dep.unit_id
      JOIN epi_catalog e ON e.id = i.epi_id
      LEFT JOIN ghe_job_role gj ON gj.job_role_id = emp.job_role_id
      LEFT JOIN ghe g ON g.id = gj.ghe_id AND g.active = 1
      LEFT JOIN matriz_linha m ON m.active = 1
        AND m.epi_id = i.epi_id
        AND m.perfil_tipo = CASE WHEN g.id IS NOT NULL THEN 'GHE' ELSE 'FUNCAO' END
        AND m.perfil_id = CASE WHEN g.id IS NOT NULL THEN g.id ELSE emp.job_role_id END
      WHERE emp.active = 0
        AND (m.id IS NULL OR m.modo = 'INDIVIDUAL')
        AND NOT EXISTS (SELECT 1 FROM fornecimento_devolucao d WHERE d.item_id = i.id)
        AND NOT EXISTS (SELECT 1 FROM fornecimento_estorno s WHERE s.item_id = i.id)
      """;

  private final NamedParameterJdbcTemplate jdbc;

  public JdbcPendenciaRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<Pendencia> listar(Long unitId, LocalDate inicio, LocalDate fim, Long employeeId) {
    StringBuilder sql = new StringBuilder(SQL);
    MapSqlParameterSource params = new MapSqlParameterSource();
    if (unitId != null) {
      sql.append(" AND u.id = :unitId");
      params.addValue("unitId", unitId);
    }
    if (inicio != null) {
      sql.append(" AND substr(f.confirmed_at, 1, 10) >= :inicio");
      params.addValue("inicio", inicio.toString());
    }
    if (fim != null) {
      sql.append(" AND substr(f.confirmed_at, 1, 10) <= :fim");
      params.addValue("fim", fim.toString());
    }
    if (employeeId != null) {
      sql.append(" AND emp.id = :employeeId");
      params.addValue("employeeId", employeeId);
    }
    sql.append(" ORDER BY emp.full_name, f.confirmed_at DESC, i.id DESC");
    return jdbc.query(
        sql.toString(),
        params,
        (rs, rowNum) ->
            new Pendencia(
                rs.getLong("item_id"),
                rs.getLong("employee_id"),
                rs.getString("employee_code"),
                rs.getString("full_name"),
                rs.getString("epi"),
                LocalDate.parse(rs.getString("dia")),
                rs.getInt("quantity"),
                rs.getLong("unit_id")));
  }
}
