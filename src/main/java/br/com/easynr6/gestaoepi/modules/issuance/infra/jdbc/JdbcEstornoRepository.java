package br.com.easynr6.gestaoepi.modules.issuance.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.issuance.application.port.EstornoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcEstornoRepository implements EstornoRepository {

  private static final String ITEM_SQL =
      """
      SELECT i.id AS item_id, i.ficha_id, f.employee_id, i.epi_id, i.lote_id, i.quantity,
             e.description AS epi, b.ca_number, l.lot_code, substr(f.confirmed_at, 1, 10) AS dia,
             emp.employee_code, emp.full_name, f.unit_name, f.department_name, f.job_role_name,
             (
               SELECT m.unit_cost_cents
               FROM estoque_movimento m
               WHERE m.lote_id = i.lote_id
                 AND m.movement_type = 'BAIXA_FORNECIMENTO'
                 AND m.created_at = f.confirmed_at
                 AND m.epi_id = i.epi_id
               LIMIT 1
             ) AS custo
      FROM fornecimento_item i
      JOIN fornecimento_ficha f ON f.id = i.ficha_id
      JOIN employee emp ON emp.id = f.employee_id
      JOIN epi_catalog e ON e.id = i.epi_id
      JOIN lote_epi l ON l.id = i.lote_id
      JOIN epi_ca_binding b ON b.id = l.epi_ca_binding_id
      WHERE NOT EXISTS (
          SELECT 1 FROM fornecimento_devolucao d WHERE d.item_id = i.id)
        AND NOT EXISTS (
          SELECT 1 FROM fornecimento_estorno s WHERE s.item_id = i.id)
      """;

  private final NamedParameterJdbcTemplate jdbc;

  public JdbcEstornoRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<TrabalhadorBusca> buscar(String texto) {
    String termo = texto == null ? "" : texto.trim();
    if (termo.isEmpty()) {
      return List.of();
    }
    return jdbc.query(
        """
        SELECT e.id, e.employee_code, e.full_name, e.active, d.name AS setor, j.name AS funcao
        FROM employee e
        JOIN department d ON d.id = e.department_id
        JOIN job_role j ON j.id = e.job_role_id
        WHERE e.employee_code LIKE :texto OR e.full_name LIKE :texto
        ORDER BY e.full_name
        LIMIT 30
        """,
        new MapSqlParameterSource("texto", "%" + termo + "%"),
        (rs, rowNum) ->
            new TrabalhadorBusca(
                rs.getLong("id"),
                rs.getString("employee_code"),
                rs.getString("full_name"),
                rs.getInt("active") == 1,
                rs.getString("setor"),
                rs.getString("funcao")));
  }

  @Override
  public List<ItemAberto> listarAbertos(Long employeeId) {
    if (employeeId == null) {
      return List.of();
    }
    return jdbc.query(
        ITEM_SQL + " AND f.employee_id = :employeeId ORDER BY f.confirmed_at DESC, i.id DESC",
        new MapSqlParameterSource("employeeId", employeeId),
        (rs, rowNum) -> item(rs));
  }

  @Override
  public Optional<ItemAberto> findAberto(Long itemId) {
    if (itemId == null) {
      return Optional.empty();
    }
    List<ItemAberto> rows =
        jdbc.query(
            ITEM_SQL + " AND i.id = :itemId",
            new MapSqlParameterSource("itemId", itemId),
            (rs, rowNum) -> item(rs));
    return rows.stream().findFirst();
  }

  @Override
  public long inserir(long itemId, String motivo, Long operatorUserId, LocalDateTime createdAt) {
    jdbc.update(
        """
        INSERT INTO fornecimento_estorno (item_id, motivo, operator_user_id, created_at)
        VALUES (:itemId, :motivo, :operatorId, :quando)
        """,
        new MapSqlParameterSource()
            .addValue("itemId", itemId)
            .addValue("motivo", motivo)
            .addValue("operatorId", operatorUserId)
            .addValue("quando", createdAt.toString()));
    Long id =
        jdbc.queryForObject("SELECT last_insert_rowid()", new MapSqlParameterSource(), Long.class);
    if (id == null) {
      throw new IllegalStateException("Estorno sem identificador.");
    }
    return id;
  }

  @Override
  public void registrarMovimento(ItemAberto item, LocalDateTime createdAt) {
    jdbc.update(
        """
        INSERT INTO estoque_movimento (
            lote_id, movement_type, quantity, created_at,
            unit_cost_cents, unit_name, department_name, job_role_name, epi_id)
        VALUES (
            :loteId, 'ESTORNO_FORNECIMENTO', :quantidade, :quando,
            :custo, :unidade, :setor, :funcao, :epiId)
        """,
        new MapSqlParameterSource()
            .addValue("loteId", item.loteId())
            .addValue("quantidade", item.quantidade())
            .addValue("quando", createdAt.toString())
            .addValue("custo", item.custoCentavos())
            .addValue("unidade", item.unidade())
            .addValue("setor", item.setor())
            .addValue("funcao", item.funcao())
            .addValue("epiId", item.epiId()));
  }

  private static ItemAberto item(java.sql.ResultSet rs) throws java.sql.SQLException {
    int custo = rs.getInt("custo");
    Integer custoCentavos = rs.wasNull() ? null : custo;
    return new ItemAberto(
        rs.getLong("item_id"),
        rs.getLong("ficha_id"),
        rs.getLong("employee_id"),
        rs.getLong("epi_id"),
        rs.getLong("lote_id"),
        rs.getString("epi"),
        rs.getString("ca_number"),
        rs.getString("lot_code"),
        rs.getInt("quantity"),
        LocalDate.parse(rs.getString("dia")),
        rs.getString("employee_code"),
        rs.getString("full_name"),
        custoCentavos,
        rs.getString("unit_name"),
        rs.getString("department_name"),
        rs.getString("job_role_name"));
  }
}
