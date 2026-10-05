package br.com.easynr6.gestaoepi.modules.matrix.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository;
import br.com.easynr6.gestaoepi.modules.matrix.domain.ModoMatriz;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcMatrizRepository implements MatrizRepository {

  private static final String CA_ATIVO =
      """
      AND b.active = 1
      AND b.ca_status = 'ACTIVE'
      AND (b.valid_from IS NULL OR b.valid_from <= DATE('now'))
      AND (b.valid_until IS NULL OR b.valid_until >= DATE('now'))
      """;

  private static final String LIST_PERFIS_SQL =
      """
      SELECT 'FUNCAO' AS tipo, jr.id AS id, d.name || ' — ' || jr.name AS rotulo
      FROM job_role jr
      JOIN department d ON d.id = jr.department_id
      WHERE jr.active = 1
        AND NOT EXISTS (
          SELECT 1
          FROM ghe_job_role m
          JOIN ghe g ON g.id = m.ghe_id
          WHERE m.job_role_id = jr.id AND g.active = 1)
      UNION ALL
      SELECT 'GHE', g.id, g.name
      FROM ghe g
      WHERE g.active = 1
      ORDER BY rotulo
      """;

  private static final String FIND_FUNCAO_SQL =
      """
      SELECT jr.active AS ativo,
             CASE WHEN g.id IS NOT NULL AND g.active = 1 THEN 1 ELSE 0 END AS em_ghe
      FROM job_role jr
      LEFT JOIN ghe_job_role m ON m.job_role_id = jr.id
      LEFT JOIN ghe g ON g.id = m.ghe_id
      WHERE jr.id = :id
      """;

  private static final String FIND_GHE_SQL = "SELECT active FROM ghe WHERE id = :id";

  private static final String FIND_EPI_SQL = "SELECT active FROM epi_catalog WHERE id = :epiId";

  private static final String CA_ATUAL_SQL =
      """
      SELECT b.id, b.ca_number
      FROM epi_ca_binding b
      WHERE b.epi_id = :epiId
      """
          + CA_ATIVO
          + """
      ORDER BY b.id DESC
      LIMIT 1
      """;

  private static final String LINHA_ATIVA_SQL =
      """
      SELECT COUNT(1) FROM matriz_linha
      WHERE perfil_tipo = :tipo AND perfil_id = :perfilId AND epi_id = :epiId AND active = 1
      """;

  private static final String INSERT_SQL =
      """
      INSERT INTO matriz_linha (
          perfil_tipo, perfil_id, epi_id, ca_binding_id, modo, exige_treinamento, active)
      VALUES (:tipo, :perfilId, :epiId, :caId, :modo, :treinamento, 1)
      """;

  private static final String SELECT_ID_SQL =
      """
      SELECT id FROM matriz_linha
      WHERE perfil_tipo = :tipo AND perfil_id = :perfilId AND epi_id = :epiId AND active = 1
      ORDER BY id DESC LIMIT 1
      """;

  private static final String FIND_LINHA_SQL =
      """
      SELECT id, epi_id, ca_binding_id, modo, exige_treinamento, active
      FROM matriz_linha
      WHERE id = :id
      """;

  private static final String UPDATE_SQL =
      """
      UPDATE matriz_linha
      SET ca_binding_id = :caId, modo = :modo, exige_treinamento = :treinamento
      WHERE id = :id AND active = 1
      """;

  private static final String INATIVAR_SQL =
      "UPDATE matriz_linha SET active = 0 WHERE id = :id AND active = 1";

  private static final String LISTAR_SQL =
      """
      SELECT m.id, m.epi_id, e.description, e.annex_group, b.ca_number, m.modo, m.exige_treinamento
      FROM matriz_linha m
      JOIN epi_catalog e ON e.id = m.epi_id
      JOIN epi_ca_binding b ON b.id = m.ca_binding_id
      WHERE m.perfil_tipo = :tipo AND m.perfil_id = :perfilId AND m.active = 1
      ORDER BY e.description
      """;

  private static final String CANDIDATOS_SQL =
      """
      SELECT e.id, e.description, e.annex_group
      FROM epi_catalog e
      WHERE e.active = 1
        AND NOT EXISTS (
          SELECT 1 FROM matriz_linha m
          WHERE m.epi_id = e.id
            AND m.perfil_tipo = :tipo
            AND m.perfil_id = :perfilId
            AND m.active = 1)
      ORDER BY e.description
      """;

  private final NamedParameterJdbcTemplate jdbc;

  public JdbcMatrizRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<PerfilOpcao> listPerfis() {
    return jdbc.query(
        LIST_PERFIS_SQL,
        (rs, rowNum) ->
            new PerfilOpcao(
                PerfilVigente.Tipo.valueOf(rs.getString("tipo")),
                rs.getLong("id"),
                rs.getString("rotulo")));
  }

  @Override
  public Optional<PerfilAlvo> findPerfil(PerfilVigente.Tipo tipo, Long perfilId) {
    if (tipo == null || perfilId == null) {
      return Optional.empty();
    }
    if (tipo == PerfilVigente.Tipo.GHE) {
      List<PerfilAlvo> rows =
          jdbc.query(
              FIND_GHE_SQL,
              new MapSqlParameterSource("id", perfilId),
              (rs, rowNum) -> new PerfilAlvo(true, rs.getInt("active") == 1, false));
      return rows.stream().findFirst();
    }
    List<PerfilAlvo> rows =
        jdbc.query(
            FIND_FUNCAO_SQL,
            new MapSqlParameterSource("id", perfilId),
            (rs, rowNum) ->
                new PerfilAlvo(true, rs.getInt("ativo") == 1, rs.getInt("em_ghe") == 1));
    return rows.stream().findFirst();
  }

  @Override
  public Optional<EpiAlvo> findEpi(Long epiId) {
    if (epiId == null) {
      return Optional.empty();
    }
    List<EpiAlvo> rows =
        jdbc.query(
            FIND_EPI_SQL,
            new MapSqlParameterSource("epiId", epiId),
            (rs, rowNum) -> new EpiAlvo(true, rs.getInt("active") == 1));
    return rows.stream().findFirst();
  }

  @Override
  public Optional<CaSugerido> caAtivoAtual(Long epiId) {
    if (epiId == null) {
      return Optional.empty();
    }
    List<CaSugerido> rows =
        jdbc.query(
            CA_ATUAL_SQL,
            new MapSqlParameterSource("epiId", epiId),
            (rs, rowNum) -> new CaSugerido(rs.getLong("id"), rs.getString("ca_number")));
    return rows.stream().findFirst();
  }

  @Override
  public boolean existeLinhaAtiva(PerfilVigente.Tipo tipo, Long perfilId, Long epiId) {
    Integer count =
        jdbc.queryForObject(
            LINHA_ATIVA_SQL, perfil(tipo, perfilId).addValue("epiId", epiId), Integer.class);
    return count != null && count > 0;
  }

  @Override
  public Long incluir(
      PerfilVigente.Tipo tipo,
      Long perfilId,
      Long epiId,
      Long caBindingId,
      ModoMatriz modo,
      boolean exigeTreinamento) {
    MapSqlParameterSource params =
        perfil(tipo, perfilId)
            .addValue("epiId", epiId)
            .addValue("caId", caBindingId)
            .addValue("modo", modo.name())
            .addValue("treinamento", exigeTreinamento ? 1 : 0);
    jdbc.update(INSERT_SQL, params);
    return jdbc.queryForObject(SELECT_ID_SQL, params, Long.class);
  }

  @Override
  public Optional<LinhaGravada> findLinha(Long linhaId) {
    if (linhaId == null) {
      return Optional.empty();
    }
    List<LinhaGravada> rows =
        jdbc.query(
            FIND_LINHA_SQL,
            new MapSqlParameterSource("id", linhaId),
            (rs, rowNum) ->
                new LinhaGravada(
                    rs.getLong("id"),
                    rs.getLong("epi_id"),
                    rs.getLong("ca_binding_id"),
                    ModoMatriz.valueOf(rs.getString("modo")),
                    rs.getInt("exige_treinamento") == 1,
                    rs.getInt("active") == 1));
    return rows.stream().findFirst();
  }

  @Override
  public void alterar(Long linhaId, Long caBindingId, ModoMatriz modo, boolean exigeTreinamento) {
    jdbc.update(
        UPDATE_SQL,
        new MapSqlParameterSource()
            .addValue("id", linhaId)
            .addValue("caId", caBindingId)
            .addValue("modo", modo.name())
            .addValue("treinamento", exigeTreinamento ? 1 : 0));
  }

  @Override
  public void inativar(Long linhaId) {
    jdbc.update(INATIVAR_SQL, new MapSqlParameterSource("id", linhaId));
  }

  @Override
  public List<LinhaMatriz> listarAtivas(PerfilVigente.Tipo tipo, Long perfilId) {
    return jdbc.query(
        LISTAR_SQL,
        perfil(tipo, perfilId),
        (rs, rowNum) ->
            new LinhaMatriz(
                rs.getLong("id"),
                rs.getLong("epi_id"),
                rs.getString("description"),
                AnnexGroup.valueOf(rs.getString("annex_group")),
                rs.getString("ca_number"),
                ModoMatriz.valueOf(rs.getString("modo")),
                rs.getInt("exige_treinamento") == 1));
  }

  @Override
  public List<EpiOpcao> listarCandidatos(PerfilVigente.Tipo tipo, Long perfilId) {
    return jdbc.query(
        CANDIDATOS_SQL,
        perfil(tipo, perfilId),
        (rs, rowNum) ->
            new EpiOpcao(
                rs.getLong("id"),
                rs.getString("description"),
                AnnexGroup.valueOf(rs.getString("annex_group"))));
  }

  private static MapSqlParameterSource perfil(PerfilVigente.Tipo tipo, Long perfilId) {
    return new MapSqlParameterSource()
        .addValue("tipo", tipo == null ? null : tipo.name())
        .addValue("perfilId", perfilId);
  }
}
