package br.com.easynr6.gestaoepi.modules.stock.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcStockRepository implements StockRepository {

  private static final String UNIT_ACTIVE_SQL =
      "SELECT COUNT(1) FROM unit WHERE id = :unitId AND active = 1";
  private static final String ACTIVE_CA_SQL =
      """
      SELECT b.id, b.official_check_at
      FROM epi_ca_binding b
      JOIN epi_catalog e ON e.id = b.epi_id
      WHERE b.id = :caBindingId
        AND b.epi_id = :epiId
        AND b.active = 1
        AND b.ca_status = 'ACTIVE'
        AND e.active = 1
      """;
  private static final String LOT_EXISTS_SQL =
      """
      SELECT COUNT(1) FROM lote_epi
      WHERE unit_id = :unitId
        AND epi_id = :epiId
        AND lot_code = :lotCode
        AND size_label = :sizeLabel
      """;
  private static final String INSERT_LOT_SQL =
      """
      INSERT INTO lote_epi (
          unit_id, epi_id, epi_ca_binding_id, lot_code, manufacturer, size_label,
          piece_valid_until, ca_checked_at, unit_cost_cents)
      VALUES (
          :unitId, :epiId, :caBindingId, :lotCode, :manufacturer, :sizeLabel,
          :pieceValidUntil, :caCheckedAt, :unitCostCents)
      """;
  private static final String SELECT_LOT_ID_SQL =
      """
      SELECT id FROM lote_epi
      WHERE unit_id = :unitId
        AND epi_id = :epiId
        AND lot_code = :lotCode
        AND size_label = :sizeLabel
      """;
  private static final String INSERT_MOVEMENT_SQL =
      """
      INSERT INTO estoque_movimento (lote_id, movement_type, quantity)
      VALUES (:loteId, 'RECEBIMENTO', :quantity)
      """;
  private static final String LIST_LOTS_SQL =
      """
      SELECT l.id, l.lot_code, e.description, l.size_label, l.piece_valid_until,
             l.unit_cost_cents, l.manufacturer,
             COALESCE((
                 SELECT SUM(m.quantity)
                 FROM estoque_movimento m
                 WHERE m.lote_id = l.id AND m.movement_type = 'RECEBIMENTO'
             ), 0) AS fisica
      FROM lote_epi l
      JOIN epi_catalog e ON e.id = l.epi_id
      WHERE l.unit_id = :unitId
      ORDER BY l.piece_valid_until, l.lot_code
      """;
  private static final String LIST_UNITS_SQL =
      "SELECT id, name FROM unit WHERE active = 1 ORDER BY name";
  private static final String LIST_EPIS_SQL =
      """
      SELECT id, epi_code, description
      FROM epi_catalog
      WHERE active = 1
      ORDER BY description
      """;
  private static final String LIST_CAS_SQL =
      """
      SELECT id, ca_number, official_check_at
      FROM epi_ca_binding
      WHERE epi_id = :epiId AND active = 1 AND ca_status = 'ACTIVE'
      ORDER BY ca_number
      """;

  private final NamedParameterJdbcTemplate jdbc;

  public JdbcStockRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public boolean unitIsActive(Long unitId) {
    Integer count =
        jdbc.queryForObject(
            UNIT_ACTIVE_SQL, new MapSqlParameterSource("unitId", unitId), Integer.class);
    return count != null && count > 0;
  }

  @Override
  public Optional<CaParaRecebimento> findActiveCa(Long epiId, Long caBindingId) {
    List<CaParaRecebimento> rows =
        jdbc.query(
            ACTIVE_CA_SQL,
            new MapSqlParameterSource()
                .addValue("epiId", epiId)
                .addValue("caBindingId", caBindingId),
            (rs, rowNum) ->
                new CaParaRecebimento(
                    rs.getLong("id"), dataHora(rs.getString("official_check_at"))));
    return rows.stream().findFirst();
  }

  @Override
  public boolean lotExists(Long unitId, Long epiId, String lotCode, String sizeLabel) {
    Integer count =
        jdbc.queryForObject(
            LOT_EXISTS_SQL, chave(unitId, epiId, lotCode, sizeLabel), Integer.class);
    return count != null && count > 0;
  }

  @Override
  public Long insertReceipt(
      Long unitId,
      Long epiId,
      Long caBindingId,
      String lotCode,
      String manufacturer,
      String sizeLabel,
      LocalDate pieceValidUntil,
      LocalDateTime caCheckedAt,
      Integer unitCostCents,
      int quantity) {
    MapSqlParameterSource params = chave(unitId, epiId, lotCode, sizeLabel);
    params.addValue("caBindingId", caBindingId);
    params.addValue("manufacturer", manufacturer);
    params.addValue("pieceValidUntil", pieceValidUntil.toString());
    params.addValue("caCheckedAt", caCheckedAt.toString());
    params.addValue("unitCostCents", unitCostCents);
    jdbc.update(INSERT_LOT_SQL, params);
    Long loteId = jdbc.queryForObject(SELECT_LOT_ID_SQL, params, Long.class);
    jdbc.update(
        INSERT_MOVEMENT_SQL,
        new MapSqlParameterSource().addValue("loteId", loteId).addValue("quantity", quantity));
    return loteId;
  }

  @Override
  public List<LotStored> listByUnit(Long unitId) {
    return jdbc.query(
        LIST_LOTS_SQL, new MapSqlParameterSource("unitId", unitId), (rs, rowNum) -> lote(rs));
  }

  @Override
  public List<UnitOption> listActiveUnits() {
    return jdbc.query(
        LIST_UNITS_SQL, (rs, rowNum) -> new UnitOption(rs.getLong("id"), rs.getString("name")));
  }

  @Override
  public List<EpiOption> listActiveEpis() {
    return jdbc.query(
        LIST_EPIS_SQL,
        (rs, rowNum) ->
            new EpiOption(rs.getLong("id"), rs.getString("epi_code"), rs.getString("description")));
  }

  @Override
  public List<CaOption> listActiveCas(Long epiId) {
    return jdbc.query(
        LIST_CAS_SQL,
        new MapSqlParameterSource("epiId", epiId),
        (rs, rowNum) ->
            new CaOption(
                rs.getLong("id"),
                rs.getString("ca_number"),
                dataHora(rs.getString("official_check_at"))));
  }

  private static MapSqlParameterSource chave(
      Long unitId, Long epiId, String lotCode, String sizeLabel) {
    return new MapSqlParameterSource()
        .addValue("unitId", unitId)
        .addValue("epiId", epiId)
        .addValue("lotCode", lotCode)
        .addValue("sizeLabel", sizeLabel);
  }

  private static LotStored lote(ResultSet rs) throws SQLException {
    int custo = rs.getInt("unit_cost_cents");
    Integer centavos = rs.wasNull() ? null : custo;
    return new LotStored(
        rs.getLong("id"),
        rs.getString("lot_code"),
        rs.getString("description"),
        rs.getString("size_label"),
        LocalDate.parse(rs.getString("piece_valid_until")),
        rs.getInt("fisica"),
        centavos,
        rs.getString("manufacturer"));
  }

  private static LocalDateTime dataHora(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    String valor = raw.trim().replace(' ', 'T');
    if (valor.length() > 19) {
      valor = valor.substring(0, 19);
    }
    return LocalDateTime.parse(valor);
  }
}
