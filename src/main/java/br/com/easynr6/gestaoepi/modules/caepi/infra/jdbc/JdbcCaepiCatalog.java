package br.com.easynr6.gestaoepi.modules.caepi.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog;
import br.com.easynr6.gestaoepi.modules.caepi.domain.CaepiParser.Registro;
import br.com.easynr6.gestaoepi.modules.caepi.domain.CaepiParser.Resultado;
import br.com.easynr6.gestaoepi.modules.caepi.domain.NomeFornecedor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCaepiCatalog implements CaepiCatalog {

  private static final String INSERT_CARGA =
      """
      INSERT INTO caepi_carga (
          finished_at, mode, result, actor_user_id, source_name, byte_size, sha256,
          record_count, failure_reason)
      VALUES (
          :finishedAt, :mode, :result, :actorId, :sourceName, :byteSize, :sha256,
          :recordCount, :failureReason)
      """;
  private static final String SELECT_ID =
      "SELECT id FROM caepi_carga WHERE sha256 = :sha256 AND result = :result ORDER BY id DESC LIMIT 1";

  private final CaepiDatabase banco;
  private final NamedParameterJdbcTemplate jdbc;

  public JdbcCaepiCatalog(CaepiDatabase banco) {
    this.banco = banco;
    this.jdbc = banco.jdbc();
  }

  @Override
  public long publicar(
      long actorId, String sourceName, int byteSize, String sha256, Resultado resultado) {
    Long id = banco.escrever(() -> gravar(actorId, sourceName, byteSize, sha256, resultado));
    if (id == null) {
      throw new IllegalStateException("CAE-004 A carga CAEPI nao foi concluida.");
    }
    return id;
  }

  private long gravar(
      long actorId, String sourceName, int byteSize, String sha256, Resultado resultado) {
    LocalDateTime agora = LocalDateTime.now().withNano(0);
    long cargaId =
        inserirCarga(
            actorId,
            sourceName,
            byteSize,
            sha256,
            "SUCESSO",
            resultado.indice().size(),
            null,
            agora);
    jdbc.update("DELETE FROM caepi_variante", new MapSqlParameterSource());
    jdbc.update("DELETE FROM caepi_ca", new MapSqlParameterSource());
    jdbc.batchUpdate(
        """
        INSERT INTO caepi_ca (ca_number, ca_status, valid_until, equipment, manufacturer, carga_id)
        VALUES (:caNumber, :status, :validUntil, :equipment, :manufacturer, :cargaId)
        """,
        parametros(resultado.indice(), cargaId));
    jdbc.batchUpdate(
        """
        INSERT INTO caepi_variante (
            ca_number, ca_status, valid_until, equipment, manufacturer, manufacturer_norm, carga_id)
        VALUES (
            :caNumber, :status, :validUntil, :equipment, :manufacturer, :manufacturerNorm, :cargaId)
        """,
        parametros(resultado.variantes(), cargaId));
    return cargaId;
  }

  @Override
  public long registrarFalha(
      long actorId, String sourceName, int byteSize, String sha256, String motivo) {
    Long id =
        banco.escrever(
            () ->
                inserirCarga(
                    actorId,
                    sourceName,
                    byteSize,
                    sha256,
                    "FALHA",
                    0,
                    motivo,
                    LocalDateTime.now().withNano(0)));
    if (id == null) {
      throw new IllegalStateException("CAE-004 A carga CAEPI nao foi concluida.");
    }
    return id;
  }

  @Override
  public Optional<CargaSucesso> ultimaSucesso() {
    List<CargaSucesso> rows =
        jdbc.query(
            """
            SELECT id, finished_at, record_count
            FROM caepi_carga
            WHERE result = 'SUCESSO'
            ORDER BY id DESC
            LIMIT 1
            """,
            (rs, rowNum) ->
                new CargaSucesso(
                    rs.getLong("id"),
                    LocalDateTime.parse(rs.getString("finished_at")),
                    rs.getInt("record_count")));
    return rows.stream().findFirst();
  }

  @Override
  public Optional<CaPublicado> findByNumber(String caNumber) {
    List<CaPublicado> rows =
        jdbc.query(
            """
            SELECT ca_number, ca_status, valid_until, equipment, manufacturer
            FROM caepi_ca
            WHERE ca_number = :caNumber
            """,
            new MapSqlParameterSource("caNumber", caNumber),
            (rs, rowNum) -> publicado(rs));
    return rows.stream().findFirst();
  }

  @Override
  public List<Linha> buscar(String termo, String fabricanteEpi) {
    Optional<CargaSucesso> carga = ultimaSucesso();
    if (carga.isEmpty()) {
      return List.of();
    }
    String chaveFabricante = NomeFornecedor.chave(fabricanteEpi);
    String chaveTermo = NomeFornecedor.chave(termo);
    boolean filtraFabricante = chaveTermo.isBlank() && chaveFabricante.length() >= 3;
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("cargaId", carga.get().id())
            .addValue("termo", chaveTermo.isBlank() ? null : "%" + chaveTermo + "%")
            .addValue("fabricante", filtraFabricante ? "%" + chaveFabricante + "%" : null);
    return jdbc.query(
        """
        SELECT i.ca_number, i.ca_status, i.valid_until, v.equipment, v.manufacturer
        FROM caepi_variante v
        JOIN caepi_ca i ON i.ca_number = v.ca_number
        WHERE v.carga_id = :cargaId
          AND (:fabricante IS NULL OR v.manufacturer_norm LIKE :fabricante)
          AND (
            :termo IS NULL
            OR v.ca_number LIKE :termo
            OR upper(v.equipment) LIKE :termo
            OR v.manufacturer_norm LIKE :termo
          )
        ORDER BY v.ca_number
        LIMIT 200
        """,
        params,
        (rs, rowNum) ->
            new Linha(
                rs.getString("ca_number"),
                rs.getString("ca_status"),
                data(rs.getString("valid_until")),
                rs.getString("equipment"),
                rs.getString("manufacturer")));
  }

  @Override
  public List<Tentativa> ultimas() {
    return jdbc.query(
        """
        SELECT id, finished_at, result, source_name, record_count, failure_reason
        FROM caepi_carga
        ORDER BY id DESC
        LIMIT 30
        """,
        (rs, rowNum) ->
            new Tentativa(
                rs.getLong("id"),
                LocalDateTime.parse(rs.getString("finished_at")),
                rs.getString("result"),
                rs.getString("source_name"),
                rs.getInt("record_count"),
                rs.getString("failure_reason")));
  }

  private long inserirCarga(
      long actorId,
      String sourceName,
      int byteSize,
      String sha256,
      String result,
      int recordCount,
      String motivo,
      LocalDateTime quando) {
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("finishedAt", quando.toString())
            .addValue("mode", "MANUAL")
            .addValue("result", result)
            .addValue("actorId", actorId)
            .addValue("sourceName", sourceName)
            .addValue("byteSize", byteSize)
            .addValue("sha256", sha256)
            .addValue("recordCount", recordCount)
            .addValue("failureReason", motivo);
    jdbc.update(INSERT_CARGA, params);
    Long id =
        jdbc.queryForObject(
            SELECT_ID,
            new MapSqlParameterSource().addValue("sha256", sha256).addValue("result", result),
            Long.class);
    if (id == null) {
      throw new IllegalStateException("CAE-004 A carga CAEPI nao foi concluida.");
    }
    return id;
  }

  private static SqlParameterSource[] parametros(List<Registro> registros, long cargaId) {
    return registros.stream()
        .map(
            registro ->
                new MapSqlParameterSource()
                    .addValue("caNumber", registro.caNumber())
                    .addValue("status", registro.status())
                    .addValue(
                        "validUntil",
                        registro.validUntil() == null ? null : registro.validUntil().toString())
                    .addValue("equipment", registro.equipment())
                    .addValue("manufacturer", registro.manufacturer())
                    .addValue("manufacturerNorm", NomeFornecedor.chave(registro.manufacturer()))
                    .addValue("cargaId", cargaId))
        .toArray(SqlParameterSource[]::new);
  }

  private static CaPublicado publicado(ResultSet rs) throws SQLException {
    return new CaPublicado(
        rs.getString("ca_number"),
        rs.getString("ca_status"),
        data(rs.getString("valid_until")),
        rs.getString("equipment"),
        rs.getString("manufacturer"));
  }

  private static LocalDate data(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    return LocalDate.parse(raw);
  }
}
