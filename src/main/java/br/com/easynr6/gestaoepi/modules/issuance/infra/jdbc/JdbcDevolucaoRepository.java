package br.com.easynr6.gestaoepi.modules.issuance.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.issuance.application.port.DevolucaoRepository;
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoDevolucao;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcDevolucaoRepository implements DevolucaoRepository {

  private static final String ITEM_SQL =
      """
      SELECT i.id AS item_id, f.employee_id, i.epi_id, e.description AS epi, b.ca_number,
             l.lot_code, i.quantity, substr(f.confirmed_at, 1, 10) AS dia,
             emp.employee_code, emp.full_name
      FROM fornecimento_item i
      JOIN fornecimento_ficha f ON f.id = i.ficha_id
      JOIN employee emp ON emp.id = f.employee_id
      JOIN epi_catalog e ON e.id = i.epi_id
      JOIN lote_epi l ON l.id = i.lote_id
      JOIN epi_ca_binding b ON b.id = l.epi_ca_binding_id
      WHERE NOT EXISTS (
          SELECT 1 FROM fornecimento_devolucao d WHERE d.item_id = i.id)
      """;

  private final NamedParameterJdbcTemplate jdbc;

  public JdbcDevolucaoRepository(NamedParameterJdbcTemplate jdbc) {
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
  public List<ItemPendente> listarPendentes(Long employeeId) {
    if (employeeId == null) {
      return List.of();
    }
    return jdbc.query(
        ITEM_SQL + " AND f.employee_id = :employeeId ORDER BY f.confirmed_at DESC, i.id DESC",
        new MapSqlParameterSource("employeeId", employeeId),
        (rs, rowNum) -> item(rs));
  }

  @Override
  public Optional<ItemPendente> findPendente(Long itemId) {
    if (itemId == null) {
      return Optional.empty();
    }
    List<ItemPendente> rows =
        jdbc.query(
            ITEM_SQL + " AND i.id = :itemId",
            new MapSqlParameterSource("itemId", itemId),
            (rs, rowNum) -> item(rs));
    return rows.stream().findFirst();
  }

  @Override
  public long inserir(
      long itemId,
      MotivoDevolucao motivo,
      String motivoTexto,
      LocalDate devolvidoEm,
      Long operatorUserId,
      LocalDateTime createdAt) {
    jdbc.update(
        """
        INSERT INTO fornecimento_devolucao (
            item_id, motivo, motivo_texto, devolvido_em, operator_user_id, created_at)
        VALUES (:itemId, :motivo, :texto, :dia, :operatorId, :quando)
        """,
        new MapSqlParameterSource()
            .addValue("itemId", itemId)
            .addValue("motivo", motivo.name())
            .addValue(
                "texto", motivoTexto == null || motivoTexto.isBlank() ? null : motivoTexto.trim())
            .addValue("dia", devolvidoEm.toString())
            .addValue("operatorId", operatorUserId)
            .addValue("quando", createdAt.toString()));
    Long id =
        jdbc.queryForObject("SELECT last_insert_rowid()", new MapSqlParameterSource(), Long.class);
    if (id == null) {
      throw new IllegalStateException("Devolucao sem identificador.");
    }
    return id;
  }

  private static ItemPendente item(java.sql.ResultSet rs) throws java.sql.SQLException {
    return new ItemPendente(
        rs.getLong("item_id"),
        rs.getLong("employee_id"),
        rs.getLong("epi_id"),
        rs.getString("epi"),
        rs.getString("ca_number"),
        rs.getString("lot_code"),
        rs.getInt("quantity"),
        LocalDate.parse(rs.getString("dia")),
        rs.getString("employee_code"),
        rs.getString("full_name"));
  }
}
