package br.com.easynr6.gestaoepi.modules.epi.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcEpiRepository implements EpiRepository {

  private static final String COUNT_EPI_BY_ID_SQL =
      "SELECT COUNT(1) FROM epi_catalog WHERE id = :epiId";
  private static final String COUNT_DUPLICATE_EPI_SQL =
      """
      SELECT COUNT(1)
      FROM epi_catalog
      WHERE UPPER(description) = UPPER(:description)
        AND annex_group = :annexGroup
        AND UPPER(manufacturer_name) = UPPER(:manufacturerName)
        AND (:excludeEpiId IS NULL OR id <> :excludeEpiId)
      """;
  private static final String INSERT_EPI_SQL =
      """
      INSERT INTO epi_catalog (epi_code, description, annex_group, manufacturer_name, active)
      VALUES (:epiCode, :description, :annexGroup, :manufacturerName, :active)
      """;
  private static final String SELECT_EPI_ID_AFTER_INSERT_SQL =
      """
      SELECT id
      FROM epi_catalog
      WHERE description = :description
        AND annex_group = :annexGroup
        AND manufacturer_name = :manufacturerName
      ORDER BY id DESC
      LIMIT 1
      """;
  private static final String UPDATE_EPI_SQL =
      """
      UPDATE epi_catalog
      SET epi_code = :epiCode,
          description = :description,
          annex_group = :annexGroup,
          manufacturer_name = :manufacturerName,
          active = :active,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = :epiId
      """;
  private static final String UPDATE_EPI_STATUS_SQL =
      """
      UPDATE epi_catalog
      SET active = :active,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = :epiId
      """;
  private static final String EXISTS_ACTIVE_COHERENT_CA_SQL =
      """
      SELECT COUNT(1)
      FROM epi_ca_binding
      WHERE epi_id = :epiId
        AND active = 1
        AND ca_status = 'ACTIVE'
        AND (valid_from IS NULL OR valid_from <= DATE('now'))
        AND (valid_until IS NULL OR valid_until >= DATE('now'))
      """;
  private static final String LIST_EPI_SQL =
      """
      SELECT id, epi_code, description, annex_group, manufacturer_name, active, updated_at
      FROM epi_catalog
      WHERE (:term = ''
         OR description LIKE :termLike
         OR manufacturer_name LIKE :termLike
         OR IFNULL(epi_code, '') LIKE :termLike)
      ORDER BY description
      """;
  private static final String INSERT_CA_BINDING_SQL =
      """
      INSERT INTO epi_ca_binding (
          epi_id, ca_number, ca_status, valid_from, valid_until, official_check_at, official_check_note, active)
      VALUES (
          :epiId, :caNumber, :caStatus, :validFrom, :validUntil, :officialCheckAt, :officialCheckNote, :active)
      """;
  private static final String SELECT_CA_BINDING_ID_AFTER_INSERT_SQL =
      """
      SELECT id
      FROM epi_ca_binding
      WHERE epi_id = :epiId
        AND ca_number = :caNumber
        AND official_check_at = :officialCheckAt
      ORDER BY id DESC
      LIMIT 1
      """;
  private static final String COUNT_CA_BINDING_BY_ID_SQL =
      "SELECT COUNT(1) FROM epi_ca_binding WHERE id = :bindingId";
  private static final String SELECT_CA_BINDING_DETAILS_SQL =
      """
      SELECT id, epi_id, ca_number, ca_status, valid_from, valid_until, active
      FROM epi_ca_binding
      WHERE id = :bindingId
      """;
  private static final String UPDATE_CA_BINDING_SQL =
      """
      UPDATE epi_ca_binding
      SET ca_number = :caNumber,
          ca_status = :caStatus,
          valid_from = :validFrom,
          valid_until = :validUntil,
          official_check_at = :officialCheckAt,
          official_check_note = :officialCheckNote,
          active = :active,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = :bindingId
      """;
  private static final String UPDATE_CA_BINDING_STATUS_SQL =
      """
      UPDATE epi_ca_binding
      SET active = :active,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = :bindingId
      """;
  private static final String COUNT_CA_VALIDITY_CONFLICT_SQL =
      """
      SELECT COUNT(1)
      FROM epi_ca_binding
      WHERE epi_id = :epiId
        AND ca_number = :caNumber
        AND active = 1
        AND (:excludeBindingId IS NULL OR id <> :excludeBindingId)
        AND (
          (valid_from IS NULL OR :validUntil IS NULL OR valid_from <= :validUntil)
          AND (valid_until IS NULL OR :validFrom IS NULL OR valid_until >= :validFrom)
        )
      """;
  private static final String LIST_CA_BINDINGS_BY_EPI_SQL =
      """
      SELECT id,
             epi_id,
             ca_number,
             ca_status,
             valid_from,
             valid_until,
             official_check_at,
             official_check_note,
             active,
             updated_at
      FROM epi_ca_binding
      WHERE epi_id = :epiId
      ORDER BY id DESC
      """;

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public JdbcEpiRepository(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public boolean existsEpiById(Long epiId) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_EPI_BY_ID_SQL,
            new MapSqlParameterSource().addValue("epiId", epiId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public boolean existsEpiDuplicate(
      Long excludeEpiId, String description, AnnexGroup annexGroup, String manufacturerName) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_DUPLICATE_EPI_SQL,
            new MapSqlParameterSource()
                .addValue("excludeEpiId", excludeEpiId)
                .addValue("description", description)
                .addValue("annexGroup", annexGroup.name())
                .addValue("manufacturerName", manufacturerName),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public Long createEpi(
      String epiCode,
      String description,
      AnnexGroup annexGroup,
      String manufacturerName,
      boolean active) {
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("epiCode", epiCode)
            .addValue("description", description)
            .addValue("annexGroup", annexGroup.name())
            .addValue("manufacturerName", manufacturerName)
            .addValue("active", active ? 1 : 0);
    jdbcTemplate.update(INSERT_EPI_SQL, params);
    return jdbcTemplate.queryForObject(SELECT_EPI_ID_AFTER_INSERT_SQL, params, Long.class);
  }

  @Override
  public void updateEpi(
      Long epiId,
      String epiCode,
      String description,
      AnnexGroup annexGroup,
      String manufacturerName,
      boolean active) {
    jdbcTemplate.update(
        UPDATE_EPI_SQL,
        new MapSqlParameterSource()
            .addValue("epiId", epiId)
            .addValue("epiCode", epiCode)
            .addValue("description", description)
            .addValue("annexGroup", annexGroup.name())
            .addValue("manufacturerName", manufacturerName)
            .addValue("active", active ? 1 : 0));
  }

  @Override
  public void setEpiActive(Long epiId, boolean active) {
    jdbcTemplate.update(
        UPDATE_EPI_STATUS_SQL,
        new MapSqlParameterSource().addValue("epiId", epiId).addValue("active", active ? 1 : 0));
  }

  @Override
  public boolean hasHistoricalDependencies(Long epiId) {
    return false;
  }

  @Override
  public boolean hasActiveCoherentCaForEpi(Long epiId) {
    Integer count =
        jdbcTemplate.queryForObject(
            EXISTS_ACTIVE_COHERENT_CA_SQL,
            new MapSqlParameterSource().addValue("epiId", epiId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public List<EpiSummary> listByTerm(String term) {
    String cleanTerm = term == null ? "" : term.trim();
    return jdbcTemplate.query(
        LIST_EPI_SQL,
        new MapSqlParameterSource()
            .addValue("term", cleanTerm)
            .addValue("termLike", "%" + cleanTerm + "%"),
        (rs, rowNum) ->
            new EpiSummary(
                rs.getLong("id"),
                rs.getString("epi_code"),
                rs.getString("description"),
                AnnexGroup.valueOf(rs.getString("annex_group")),
                rs.getString("manufacturer_name"),
                rs.getInt("active") == 1,
                rs.getString("updated_at")));
  }

  @Override
  public Long createCaBinding(
      Long epiId,
      String caNumber,
      CaStatus caStatus,
      LocalDate validFrom,
      LocalDate validUntil,
      LocalDateTime officialCheckAt,
      String officialCheckNote,
      boolean active) {
    String validFromValue = validFrom == null ? null : validFrom.toString();
    String validUntilValue = validUntil == null ? null : validUntil.toString();
    String officialCheckAtValue = officialCheckAt == null ? null : officialCheckAt.toString();
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("epiId", epiId)
            .addValue("caNumber", caNumber)
            .addValue("caStatus", caStatus.name())
            .addValue("validFrom", validFromValue)
            .addValue("validUntil", validUntilValue)
            .addValue("officialCheckAt", officialCheckAtValue)
            .addValue("officialCheckNote", officialCheckNote)
            .addValue("active", active ? 1 : 0);
    jdbcTemplate.update(INSERT_CA_BINDING_SQL, params);
    return jdbcTemplate.queryForObject(SELECT_CA_BINDING_ID_AFTER_INSERT_SQL, params, Long.class);
  }

  @Override
  public boolean existsCaBindingById(Long bindingId) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_CA_BINDING_BY_ID_SQL,
            new MapSqlParameterSource().addValue("bindingId", bindingId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public Optional<CaBindingDetails> findCaBindingById(Long bindingId) {
    List<CaBindingDetails> rows =
        jdbcTemplate.query(
            SELECT_CA_BINDING_DETAILS_SQL,
            new MapSqlParameterSource().addValue("bindingId", bindingId),
            (rs, rowNum) ->
                new CaBindingDetails(
                    rs.getLong("id"),
                    rs.getLong("epi_id"),
                    rs.getString("ca_number"),
                    CaStatus.valueOf(rs.getString("ca_status")),
                    rs.getString("valid_from") == null
                        ? null
                        : LocalDate.parse(rs.getString("valid_from")),
                    rs.getString("valid_until") == null
                        ? null
                        : LocalDate.parse(rs.getString("valid_until")),
                    rs.getInt("active") == 1));
    return rows.stream().findFirst();
  }

  @Override
  public void updateCaBinding(
      Long bindingId,
      String caNumber,
      CaStatus caStatus,
      LocalDate validFrom,
      LocalDate validUntil,
      LocalDateTime officialCheckAt,
      String officialCheckNote,
      boolean active) {
    jdbcTemplate.update(
        UPDATE_CA_BINDING_SQL,
        new MapSqlParameterSource()
            .addValue("bindingId", bindingId)
            .addValue("caNumber", caNumber)
            .addValue("caStatus", caStatus.name())
            .addValue("validFrom", validFrom == null ? null : validFrom.toString())
            .addValue("validUntil", validUntil == null ? null : validUntil.toString())
            .addValue(
                "officialCheckAt", officialCheckAt == null ? null : officialCheckAt.toString())
            .addValue("officialCheckNote", officialCheckNote)
            .addValue("active", active ? 1 : 0));
  }

  @Override
  public void setCaBindingActive(Long bindingId, boolean active) {
    jdbcTemplate.update(
        UPDATE_CA_BINDING_STATUS_SQL,
        new MapSqlParameterSource()
            .addValue("bindingId", bindingId)
            .addValue("active", active ? 1 : 0));
  }

  @Override
  public boolean hasHistoricalDependenciesForCaBinding(Long bindingId) {
    return false;
  }

  @Override
  public boolean existsCaValidityConflict(
      Long epiId,
      String caNumber,
      LocalDate validFrom,
      LocalDate validUntil,
      Long excludeBindingId) {
    Integer count =
        jdbcTemplate.queryForObject(
            COUNT_CA_VALIDITY_CONFLICT_SQL,
            new MapSqlParameterSource()
                .addValue("epiId", epiId)
                .addValue("caNumber", caNumber)
                .addValue("validFrom", validFrom == null ? null : validFrom.toString())
                .addValue("validUntil", validUntil == null ? null : validUntil.toString())
                .addValue("excludeBindingId", excludeBindingId),
            Integer.class);
    return count != null && count > 0;
  }

  @Override
  public List<CaBindingSummary> listCaByEpi(Long epiId) {
    return jdbcTemplate.query(
        LIST_CA_BINDINGS_BY_EPI_SQL,
        new MapSqlParameterSource().addValue("epiId", epiId),
        (rs, rowNum) ->
            new CaBindingSummary(
                rs.getLong("id"),
                rs.getLong("epi_id"),
                rs.getString("ca_number"),
                CaStatus.valueOf(rs.getString("ca_status")),
                rs.getString("valid_from"),
                rs.getString("valid_until"),
                rs.getString("official_check_at"),
                rs.getString("official_check_note"),
                rs.getInt("active") == 1,
                rs.getString("updated_at")));
  }

  @Override
  public void saveCaEvidence(Long bindingId, String fileName, byte[] content) {
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("bindingId", bindingId)
            .addValue("fileName", fileName)
            .addValue("content", content);
    jdbcTemplate.update("DELETE FROM epi_ca_evidence WHERE binding_id = :bindingId", params);
    jdbcTemplate.update(
        """
        INSERT INTO epi_ca_evidence (binding_id, file_name, content)
        VALUES (:bindingId, :fileName, :content)
        """,
        params);
  }

  @Override
  public boolean hasCaEvidence(Long bindingId) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM epi_ca_evidence WHERE binding_id = :bindingId",
            new MapSqlParameterSource("bindingId", bindingId),
            Integer.class);
    return count != null && count > 0;
  }
}
