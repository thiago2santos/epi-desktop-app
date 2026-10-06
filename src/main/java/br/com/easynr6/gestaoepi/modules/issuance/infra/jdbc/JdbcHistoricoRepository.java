package br.com.easynr6.gestaoepi.modules.issuance.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.issuance.application.port.HistoricoRepository;
import br.com.easynr6.gestaoepi.modules.issuance.domain.HistoricoPolicy;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcHistoricoRepository implements HistoricoRepository {

  private static final String CA =
      """
      COALESCE(
          (SELECT c.ca_number FROM fornecimento_item_ca c
           WHERE c.item_id = i.id ORDER BY c.ca_number LIMIT 1),
          b.ca_number)
      """;

  private static final String BASE =
      """
      FROM fornecimento_item i
      JOIN fornecimento_ficha f ON f.id = i.ficha_id
      JOIN epi_catalog e ON e.id = i.epi_id
      JOIN lote_epi l ON l.id = i.lote_id
      JOIN epi_ca_binding b ON b.id = l.epi_ca_binding_id
      """;

  private final NamedParameterJdbcTemplate jdbc;
  private final HistoricoPolicy policy = new HistoricoPolicy();

  public JdbcHistoricoRepository(NamedParameterJdbcTemplate jdbc) {
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
        (rs, rowNum) -> trabalhador(rs));
  }

  @Override
  public List<Linha> listar(Long employeeId, LocalDate inicio, LocalDate fim) {
    if (employeeId == null || inicio == null || fim == null) {
      return List.of();
    }
    MapSqlParameterSource params =
        new MapSqlParameterSource()
            .addValue("employeeId", employeeId)
            .addValue("inicio", inicio.toString())
            .addValue("fim", fim.toString());
    return jdbc.query(sql(), params, (rs, rowNum) -> linha(rs));
  }

  private String sql() {
    return """
        SELECT substr(f.confirmed_at, 1, 10) AS dia, f.confirmed_at AS ordem,
               e.description AS epi, %s AS ca, l.lot_code, i.quantity, i.motivo AS motivo_codigo,
               CASE WHEN EXISTS (
                   SELECT 1 FROM fornecimento_estorno s WHERE s.item_id = i.id) THEN 1 ELSE 0 END
                   AS estornado,
               CASE WHEN EXISTS (
                   SELECT 1 FROM fornecimento_devolucao d WHERE d.item_id = i.id) THEN 1 ELSE 0 END
                   AS devolvido,
               'FORNECIMENTO' AS fato, i.id AS item_id
        %s
        WHERE f.employee_id = :employeeId
          AND substr(f.confirmed_at, 1, 10) BETWEEN :inicio AND :fim
        UNION ALL
        SELECT d.devolvido_em, d.created_at,
               e.description, %s, l.lot_code, i.quantity, d.motivo,
               0, 1, 'DEVOLUCAO', i.id
        %s
        JOIN fornecimento_devolucao d ON d.item_id = i.id
        WHERE f.employee_id = :employeeId
          AND d.devolvido_em BETWEEN :inicio AND :fim
        UNION ALL
        SELECT substr(s.created_at, 1, 10), s.created_at,
               e.description, %s, l.lot_code, i.quantity, '' ,
               1, 0, 'ESTORNO', i.id
        %s
        JOIN fornecimento_estorno s ON s.item_id = i.id
        WHERE f.employee_id = :employeeId
          AND substr(s.created_at, 1, 10) BETWEEN :inicio AND :fim
        ORDER BY dia DESC, ordem DESC, item_id DESC
        """
        .formatted(CA, BASE, CA, BASE, CA, BASE);
  }

  private Linha linha(java.sql.ResultSet rs) throws java.sql.SQLException {
    String fato = rs.getString("fato");
    String motivo = motivo(fato, rs.getString("motivo_codigo"));
    String situacao = situacao(fato, rs.getInt("estornado") == 1, rs.getInt("devolvido") == 1);
    return new Linha(
        LocalDate.parse(rs.getString("dia")),
        rs.getString("epi"),
        rs.getString("ca"),
        rs.getString("lot_code"),
        rs.getInt("quantity"),
        motivo,
        situacao,
        fato,
        rs.getLong("item_id"));
  }

  private String motivo(String fato, String codigo) {
    if ("DEVOLUCAO".equals(fato)) {
      return policy.motivoDevolucao(codigo);
    }
    if ("ESTORNO".equals(fato)) {
      return policy.motivoEstorno();
    }
    return policy.motivoFornecimento(codigo);
  }

  private String situacao(String fato, boolean estornado, boolean devolvido) {
    if ("DEVOLUCAO".equals(fato)) {
      return HistoricoPolicy.DEVOLVIDO;
    }
    if ("ESTORNO".equals(fato)) {
      return HistoricoPolicy.ESTORNADO;
    }
    return policy.situacaoFornecimento(estornado, devolvido);
  }

  private static TrabalhadorBusca trabalhador(java.sql.ResultSet rs) throws java.sql.SQLException {
    return new TrabalhadorBusca(
        rs.getLong("id"),
        rs.getString("employee_code"),
        rs.getString("full_name"),
        rs.getInt("active") == 1,
        rs.getString("setor"),
        rs.getString("funcao"));
  }
}
