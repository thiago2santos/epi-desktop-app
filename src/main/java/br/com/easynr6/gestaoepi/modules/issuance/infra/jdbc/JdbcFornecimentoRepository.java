package br.com.easynr6.gestaoepi.modules.issuance.infra.jdbc;

import br.com.easynr6.gestaoepi.modules.issuance.application.port.FornecimentoRepository;
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
public class JdbcFornecimentoRepository implements FornecimentoRepository {

  private static final String SALDO =
      """
      COALESCE((
          SELECT SUM(CASE m.movement_type
              WHEN 'RECEBIMENTO' THEN m.quantity
              WHEN 'ESTORNO_FORNECIMENTO' THEN m.quantity
              WHEN 'BAIXA_FORNECIMENTO' THEN -m.quantity
              WHEN 'BAIXA_PRATELEIRA' THEN -m.quantity
              ELSE 0
          END)
          FROM estoque_movimento m
          WHERE m.lote_id = l.id
      ), 0)
      """;
  private static final String RESERVADA =
      """
      COALESCE((
          SELECT SUM(CASE m.movement_type
              WHEN 'RESERVA' THEN m.quantity
              WHEN 'LIBERACAO_RESERVA' THEN -m.quantity
              ELSE 0
          END)
          FROM estoque_movimento m
          WHERE m.lote_id = l.id
      ), 0)
      """;
  private static final String LOTE_SELECT =
      """
      SELECT l.id, l.epi_id, l.unit_id, l.lot_code, b.ca_number, b.ca_status, l.piece_valid_until,
             l.unit_cost_cents, %s AS fisica, %s AS reservada
      FROM lote_epi l
      JOIN epi_ca_binding b ON b.id = l.epi_ca_binding_id
      """
          .formatted(SALDO, RESERVADA);
  private static final String TRABALHADOR_SELECT =
      """
      SELECT e.id, e.employee_code, e.full_name, e.active, e.job_role_id,
             u.id AS unit_id, u.name AS unit_name, d.name AS department_name,
             j.name AS job_role_name, m.full_name AS manager_name
      FROM employee e
      JOIN department d ON d.id = e.department_id
      JOIN unit u ON u.id = d.unit_id
      JOIN job_role j ON j.id = e.job_role_id
      LEFT JOIN employee m ON m.id = e.manager_id
      """;

  private final NamedParameterJdbcTemplate jdbc;

  public JdbcFornecimentoRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public Optional<Trabalhador> findTrabalhador(Long employeeId) {
    if (employeeId == null) {
      return Optional.empty();
    }
    List<Trabalhador> rows =
        jdbc.query(
            TRABALHADOR_SELECT + " WHERE e.id = :id",
            new MapSqlParameterSource("id", employeeId),
            (rs, rowNum) -> trabalhador(rs));
    return rows.stream().findFirst();
  }

  @Override
  public Optional<String> hashSenhaRecebimento(Long employeeId) {
    if (employeeId == null) {
      return Optional.empty();
    }
    List<String> rows =
        jdbc.query(
            "SELECT senha_recebimento_hash FROM employee WHERE id = :id",
            new MapSqlParameterSource("id", employeeId),
            (rs, rowNum) -> rs.getString("senha_recebimento_hash"));
    return rows.stream().filter(hash -> hash != null && !hash.isBlank()).findFirst();
  }

  @Override
  public Pagina buscar(String texto, int pagina, int tamanho, boolean somenteAtivos, Long setorId) {
    return buscar(texto, pagina, tamanho, somenteAtivos, setorId, null);
  }

  @Override
  public Pagina buscar(
      String texto, int pagina, int tamanho, boolean somenteAtivos, Long setorId, Long unidadeId) {
    String termo = texto == null ? "" : texto.trim();
    int limite = tamanho <= 0 ? 1 : tamanho;
    int indice = Math.max(pagina, 0);
    MapSqlParameterSource filtro =
        new MapSqlParameterSource("texto", termo.isEmpty() ? "" : "%" + termo + "%")
            .addValue("somenteAtivos", somenteAtivos ? 1 : 0)
            .addValue("setorId", setorId == null ? 0 : setorId)
            .addValue("unidadeId", unidadeId == null ? 0 : unidadeId);
    String onde =
        """
         WHERE (:somenteAtivos = 0 OR e.active = 1)
           AND (:setorId = 0 OR e.department_id = :setorId)
           AND (:unidadeId = 0 OR d.unit_id = :unidadeId)
           AND (
             :texto = ''
             OR e.employee_code LIKE :texto
             OR e.full_name LIKE :texto
           )
        """;
    Integer contagem =
        jdbc.queryForObject(
            """
            SELECT COUNT(1)
            FROM employee e
            JOIN department d ON d.id = e.department_id
            """
                + onde,
            filtro,
            Integer.class);
    int total = contagem == null ? 0 : contagem;
    List<Trabalhador> itens =
        jdbc.query(
            TRABALHADOR_SELECT
                + onde
                + """
                 ORDER BY e.full_name, e.id
                 LIMIT :limite OFFSET :offset
                """,
            filtro.addValue("limite", limite).addValue("offset", indice * limite),
            (rs, rowNum) -> trabalhador(rs));
    return new Pagina(itens, total, indice, limite);
  }

  @Override
  public Optional<LoteLido> findLote(Long loteId) {
    if (loteId == null) {
      return Optional.empty();
    }
    List<LoteLido> rows =
        jdbc.query(
            LOTE_SELECT + " WHERE l.id = :id",
            new MapSqlParameterSource("id", loteId),
            (rs, rowNum) -> lote(rs));
    return rows.stream().findFirst();
  }

  @Override
  public List<LoteLido> listarLotes(Long unitId, Long epiId) {
    if (unitId == null || epiId == null) {
      return List.of();
    }
    return jdbc.query(
        LOTE_SELECT
            + """
             WHERE l.unit_id = :unitId AND l.epi_id = :epiId
             ORDER BY l.piece_valid_until, l.lot_code
            """,
        new MapSqlParameterSource().addValue("unitId", unitId).addValue("epiId", epiId),
        (rs, rowNum) -> lote(rs));
  }

  @Override
  public List<String> casAtivos(Long epiId) {
    if (epiId == null) {
      return List.of();
    }
    return jdbc.query(
        """
        SELECT ca_number
        FROM epi_ca_binding
        WHERE epi_id = :epiId AND active = 1 AND ca_status = 'ACTIVE'
        ORDER BY ca_number
        """,
        new MapSqlParameterSource("epiId", epiId),
        (rs, rowNum) -> rs.getString("ca_number"));
  }

  @Override
  public long inserirFicha(
      Trabalhador trabalhador, Long operatorUserId, LocalDateTime confirmedAt) {
    jdbc.update(
        """
        INSERT INTO fornecimento_ficha (
            employee_id, operator_user_id, employee_code, employee_name,
            unit_name, department_name, job_role_name, confirmed_at)
        VALUES (:employeeId, :operatorId, :matricula, :nome, :unidade, :setor, :funcao, :quando)
        """,
        new MapSqlParameterSource()
            .addValue("employeeId", trabalhador.id())
            .addValue("operatorId", operatorUserId)
            .addValue("matricula", trabalhador.matricula())
            .addValue("nome", trabalhador.nome())
            .addValue("unidade", trabalhador.unidade())
            .addValue("setor", trabalhador.setor())
            .addValue("funcao", trabalhador.funcao())
            .addValue("quando", confirmedAt.toString()));
    return ultimoId();
  }

  @Override
  public long inserirItem(long fichaId, ItemGravado item) {
    jdbc.update(
        """
        INSERT INTO fornecimento_item (
            ficha_id, lote_id, epi_id, quantity, motivo, motivo_texto,
            ciencia, data_treinamento, excecao_texto)
        VALUES (
            :fichaId, :loteId, :epiId, :quantidade, :motivo, :motivoTexto,
            :ciencia, :treinamento, :excecao)
        """,
        new MapSqlParameterSource()
            .addValue("fichaId", fichaId)
            .addValue("loteId", item.loteId())
            .addValue("epiId", item.epiId())
            .addValue("quantidade", item.quantidade())
            .addValue("motivo", item.motivo().name())
            .addValue("motivoTexto", texto(item.motivoTexto()))
            .addValue("ciencia", item.ciencia() ? 1 : 0)
            .addValue(
                "treinamento",
                item.dataTreinamento() == null ? null : item.dataTreinamento().toString())
            .addValue("excecao", texto(item.excecaoTexto())));
    return ultimoId();
  }

  @Override
  public void inserirCa(long itemId, String caNumber) {
    jdbc.update(
        """
        INSERT INTO fornecimento_item_ca (item_id, ca_number)
        VALUES (:itemId, :ca)
        """,
        new MapSqlParameterSource().addValue("itemId", itemId).addValue("ca", caNumber));
  }

  @Override
  public void inserirTermo(
      long fichaId, String versao, String metodo, LocalDateTime acceptedAt, Long operatorUserId) {
    jdbc.update(
        """
        INSERT INTO fornecimento_termo (ficha_id, versao, metodo, accepted_at, operator_user_id)
        VALUES (:fichaId, :versao, :metodo, :quando, :operatorId)
        """,
        new MapSqlParameterSource()
            .addValue("fichaId", fichaId)
            .addValue("versao", versao)
            .addValue("metodo", metodo)
            .addValue("quando", acceptedAt.toString())
            .addValue("operatorId", operatorUserId));
  }

  private long ultimoId() {
    Long id =
        jdbc.queryForObject("SELECT last_insert_rowid()", new MapSqlParameterSource(), Long.class);
    if (id == null) {
      throw new IllegalStateException("Ficha sem identificador.");
    }
    return id;
  }

  private static Trabalhador trabalhador(ResultSet rs) throws SQLException {
    return new Trabalhador(
        rs.getLong("id"),
        rs.getString("employee_code"),
        rs.getString("full_name"),
        rs.getInt("active") == 1,
        rs.getLong("unit_id"),
        rs.getString("unit_name"),
        rs.getString("department_name"),
        rs.getString("job_role_name"),
        rs.getLong("job_role_id"),
        rs.getString("manager_name"));
  }

  private static LoteLido lote(ResultSet rs) throws SQLException {
    int custo = rs.getInt("unit_cost_cents");
    Integer centavos = rs.wasNull() ? null : custo;
    return new LoteLido(
        rs.getLong("id"),
        rs.getLong("epi_id"),
        rs.getLong("unit_id"),
        rs.getString("lot_code"),
        rs.getString("ca_number"),
        rs.getString("ca_status"),
        LocalDate.parse(rs.getString("piece_valid_until")),
        rs.getInt("fisica"),
        rs.getInt("reservada"),
        centavos);
  }

  @Override
  public List<Long> episComEntregaQueConta(Long employeeId) {
    if (employeeId == null) {
      return List.of();
    }
    return jdbc.query(
        """
        SELECT DISTINCT i.epi_id
        FROM fornecimento_item i
        JOIN fornecimento_ficha f ON f.id = i.ficha_id
        WHERE f.employee_id = :employeeId
          AND NOT EXISTS (
              SELECT 1 FROM fornecimento_devolucao d WHERE d.item_id = i.id)
          AND NOT EXISTS (
              SELECT 1 FROM fornecimento_estorno s WHERE s.item_id = i.id)
        """,
        new MapSqlParameterSource("employeeId", employeeId),
        (rs, rowNum) -> rs.getLong("epi_id"));
  }

  @Override
  public Optional<String> funcaoDaUltimaFicha(Long employeeId) {
    if (employeeId == null) {
      return Optional.empty();
    }
    List<String> nomes =
        jdbc.query(
            """
            SELECT job_role_name
            FROM fornecimento_ficha
            WHERE employee_id = :employeeId
            ORDER BY confirmed_at DESC, id DESC
            LIMIT 1
            """,
            new MapSqlParameterSource("employeeId", employeeId),
            (rs, rowNum) -> rs.getString("job_role_name"));
    return nomes.stream().findFirst();
  }

  private static String texto(String valor) {
    if (valor == null || valor.isBlank()) {
      return null;
    }
    return valor.trim();
  }
}
