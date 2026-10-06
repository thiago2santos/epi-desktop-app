package br.com.easynr6.gestaoepi.modules.matrix.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcPeriodicidadeRepository implements PeriodicidadeRepository {

  private static final String LISTAR_SQL =
      """
      SELECT e.id, e.description, p.dias, p.aviso_dias,
        (
          SELECT COUNT(1)
          FROM job_role jr
          WHERE jr.active = 1
            AND (
              (
                NOT EXISTS (
                  SELECT 1 FROM ghe_job_role gj
                  JOIN ghe g ON g.id = gj.ghe_id AND g.active = 1
                  WHERE gj.job_role_id = jr.id)
                AND EXISTS (
                  SELECT 1 FROM matriz_linha ml
                  WHERE ml.active = 1 AND ml.epi_id = e.id
                    AND ml.perfil_tipo = 'FUNCAO' AND ml.perfil_id = jr.id)
              )
              OR EXISTS (
                SELECT 1 FROM ghe_job_role gj
                JOIN ghe g ON g.id = gj.ghe_id AND g.active = 1
                JOIN matriz_linha ml
                  ON ml.perfil_tipo = 'GHE' AND ml.perfil_id = g.id
                 AND ml.active = 1 AND ml.epi_id = e.id
                WHERE gj.job_role_id = jr.id)
            )
        ) AS funcoes
      FROM epi_catalog e
      LEFT JOIN periodicidade_epi p ON p.epi_id = e.id
      WHERE EXISTS (
        SELECT 1 FROM matriz_linha m WHERE m.epi_id = e.id AND m.active = 1)
      ORDER BY e.description
      """;

  private static final String TEM_LINHA_SQL =
      "SELECT COUNT(1) FROM matriz_linha WHERE epi_id = :epiId AND active = 1";

  private static final String FIND_SQL =
      "SELECT dias, aviso_dias FROM periodicidade_epi WHERE epi_id = :epiId";

  private static final String UPSERT_SQL =
      """
      INSERT INTO periodicidade_epi (epi_id, dias, aviso_dias)
      VALUES (:epiId, :dias, :aviso)
      ON CONFLICT(epi_id) DO UPDATE SET dias = excluded.dias, aviso_dias = excluded.aviso_dias
      """;

  private static final String TRABALHADOR_SQL =
      "SELECT job_role_id, active FROM employee WHERE id = :id";

  private final NamedParameterJdbcTemplate jdbc;

  public JdbcPeriodicidadeRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<LinhaPeriodicidade> listar() {
    return jdbc.query(
        LISTAR_SQL,
        (rs, rowNum) -> {
          int dias = rs.getInt("dias");
          Integer prazo = rs.wasNull() ? null : dias;
          int aviso = rs.getInt("aviso_dias");
          Integer antecedencia = rs.wasNull() ? null : aviso;
          return new LinhaPeriodicidade(
              rs.getLong("id"),
              rs.getString("description"),
              rs.getInt("funcoes"),
              prazo,
              antecedencia);
        });
  }

  @Override
  public boolean temLinhaAtiva(Long epiId) {
    if (epiId == null) {
      return false;
    }
    Integer count =
        jdbc.queryForObject(
            TEM_LINHA_SQL, new MapSqlParameterSource("epiId", epiId), Integer.class);
    return count != null && count > 0;
  }

  @Override
  public Optional<Prazo> find(Long epiId) {
    if (epiId == null) {
      return Optional.empty();
    }
    List<Prazo> rows =
        jdbc.query(
            FIND_SQL,
            new MapSqlParameterSource("epiId", epiId),
            (rs, rowNum) -> new Prazo(rs.getInt("dias"), rs.getInt("aviso_dias")));
    return rows.stream().findFirst();
  }

  @Override
  public void salvar(Long epiId, int dias, int aviso) {
    jdbc.update(
        UPSERT_SQL,
        new MapSqlParameterSource()
            .addValue("epiId", epiId)
            .addValue("dias", dias)
            .addValue("aviso", aviso));
  }

  @Override
  public Optional<Trabalhador> findTrabalhador(Long employeeId) {
    if (employeeId == null) {
      return Optional.empty();
    }
    List<Trabalhador> rows =
        jdbc.query(
            TRABALHADOR_SQL,
            new MapSqlParameterSource("id", employeeId),
            (rs, rowNum) -> new Trabalhador(rs.getLong("job_role_id"), rs.getInt("active") == 1));
    return rows.stream().findFirst();
  }

  @Override
  public Optional<LocalDate> fornecimentoQueConta(Long employeeId, Long epiId) {
    if (employeeId == null || epiId == null) {
      return Optional.empty();
    }
    List<String> datas =
        jdbc.query(
            """
            SELECT substr(f.confirmed_at, 1, 10) AS dia
            FROM fornecimento_item i
            JOIN fornecimento_ficha f ON f.id = i.ficha_id
            WHERE f.employee_id = :employeeId AND i.epi_id = :epiId
              AND NOT EXISTS (
                  SELECT 1 FROM fornecimento_devolucao d WHERE d.item_id = i.id)
            ORDER BY f.confirmed_at DESC, f.id DESC
            LIMIT 1
            """,
            new MapSqlParameterSource().addValue("employeeId", employeeId).addValue("epiId", epiId),
            (rs, rowNum) -> rs.getString("dia"));
    return datas.stream().findFirst().map(LocalDate::parse);
  }
}
