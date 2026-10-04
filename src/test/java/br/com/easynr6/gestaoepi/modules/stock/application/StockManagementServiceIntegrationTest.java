package br.com.easynr6.gestaoepi.modules.stock.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.epi.PrintConsulta;
import br.com.easynr6.gestaoepi.modules.epi.application.EpiCatalogManagementService;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.LotBalance;
import br.com.easynr6.gestaoepi.modules.stock.domain.PecaVencidaNaoConfirmadaException;
import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:sqlite:file:lot-mem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class StockManagementServiceIntegrationTest {

  private static final AtomicInteger FILIAL = new AtomicInteger(40);
  private static final int[] PESOS_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] PESOS_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

  @Autowired private StockManagementService estoque;
  @Autowired private EmployeeManagementService empregados;
  @Autowired private EpiCatalogManagementService catalogo;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void shouldReceiveCurrentLotWithAudit() {
    Contexto ctx = contexto();
    LocalDate validade = LocalDate.now().plusMonths(6);

    long loteId =
        estoque.receiveLot(
            ctx.almoxId(),
            ctx.unitId(),
            ctx.epiId(),
            ctx.caId(),
            " VG-26 ",
            "Vaqueta SA",
            "G",
            validade,
            "10",
            "12,50",
            false);

    assertEquals("VG-26", codigoOf(loteId));
    assertEquals("Vaqueta SA", fabricanteOf(loteId));
    assertEquals("G", tamanhoOf(loteId));
    assertEquals(1250, custoOf(loteId));
    assertEquals(1, movimentos(loteId, "RECEBIMENTO"));
    assertEquals(10, quantidadeMovimento(loteId));
    LotBalance saldo = saldo(ctx.almoxId(), ctx.unitId(), "VG-26");
    assertEquals(10, saldo.fisica());
    assertEquals(0, saldo.reservada());
    assertEquals(10, saldo.disponivel());
    assertEquals("Vigente", saldo.situacao());
    assertEquals(1, auditSucesso(loteId, ctx.almoxId()));
  }

  @Test
  void shouldStoreNullCostWhenBlank() {
    Contexto ctx = contexto();
    long loteId = receber(ctx, "SEM-CUSTO", LocalDate.now().plusDays(30), "4", "  ", false);

    assertNull(custoNulo(loteId));
    assertEquals(4, quantidadeMovimento(loteId));
  }

  @Test
  void shouldRejectMissingCodeWithoutMovement() {
    Contexto ctx = contexto();
    int antes = totalMovimentos();

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                estoque.receiveLot(
                    ctx.almoxId(),
                    ctx.unitId(),
                    ctx.epiId(),
                    ctx.caId(),
                    " ",
                    null,
                    null,
                    LocalDate.now().plusDays(10),
                    "1",
                    null,
                    false));

    assertTrue(ex.getMessage().startsWith("LOT-001"));
    assertEquals(antes, totalMovimentos());
  }

  @Test
  void shouldRejectFractionalQuantity() {
    Contexto ctx = contexto();

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> receber(ctx, "FRACIONADO", LocalDate.now().plusDays(10), "1,5", null, false));

    assertTrue(ex.getMessage().startsWith("LOT-002"));
  }

  @Test
  void shouldRejectDuplicateInTheSameUnit() {
    Contexto ctx = contexto();
    receber(ctx, "DUP-1", LocalDate.now().plusDays(20), "2", null, false);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> receber(ctx, "DUP-1", LocalDate.now().plusDays(20), "3", null, false));

    assertTrue(ex.getMessage().startsWith("LOT-003"));
    assertEquals(1, movimentosDoCodigo("DUP-1"));
  }

  @Test
  void shouldAllowTheSameCodeInAnotherUnit() {
    Contexto ctx = contexto();
    long outra = outraUnidadeAtiva(ctx.unitId());
    receber(ctx, "MESMO", LocalDate.now().plusDays(15), "1", null, false);

    long segundo =
        estoque.receiveLot(
            ctx.almoxId(),
            outra,
            ctx.epiId(),
            ctx.caId(),
            "MESMO",
            null,
            "",
            LocalDate.now().plusDays(15),
            "1",
            null,
            false);

    assertEquals("MESMO", codigoOf(segundo));
    assertEquals(2, movimentosDoCodigo("MESMO"));
  }

  @Test
  void shouldRejectEpiWithoutActiveCa() {
    Contexto ctx = contexto();
    long epiSemCa =
        catalogo.createEpi(
            ctx.sesmtId(),
            "SEM-" + System.nanoTime(),
            "EPI sem CA " + System.nanoTime(),
            AnnexGroup.A,
            "Fabricante",
            true);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                estoque.receiveLot(
                    ctx.almoxId(),
                    ctx.unitId(),
                    epiSemCa,
                    ctx.caId(),
                    "SEM-CA",
                    null,
                    null,
                    LocalDate.now().plusDays(5),
                    "1",
                    null,
                    false));

    assertTrue(ex.getMessage().startsWith("LOT-004"));
  }

  @Test
  void shouldRejectInactiveUnit() {
    Contexto ctx = contexto();
    long inativa =
        empregados.createUnit(
            ctx.sesmtId(), "Planta parada " + System.nanoTime(), proximoCnpj(), true);
    empregados.setUnitStatus(ctx.sesmtId(), inativa, false);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                estoque.receiveLot(
                    ctx.almoxId(),
                    inativa,
                    ctx.epiId(),
                    ctx.caId(),
                    "UNIDADE-OFF",
                    null,
                    null,
                    LocalDate.now().plusDays(5),
                    "1",
                    null,
                    false));

    assertTrue(ex.getMessage().startsWith("LOT-005"));
  }

  @Test
  void shouldRejectNegativeCost() {
    Contexto ctx = contexto();

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> receber(ctx, "CUSTO-NEG", LocalDate.now().plusDays(5), "1", "-0,01", false));

    assertTrue(ex.getMessage().startsWith("LOT-007"));
  }

  @Test
  void shouldReceiveExpiredPieceWithZeroAvailable() {
    Contexto ctx = contexto();
    long loteId = receber(ctx, "VENCIDO", LocalDate.now().minusDays(1), "6", null, true);

    LotBalance saldo = saldo(ctx.almoxId(), ctx.unitId(), "VENCIDO");
    assertEquals(loteId, saldo.id());
    assertEquals(6, saldo.fisica());
    assertEquals(0, saldo.disponivel());
    assertEquals("Vencido", saldo.situacao());
  }

  @Test
  void shouldRefuseExpiredPieceWithoutConfirmation() {
    Contexto ctx = contexto();
    int antes = totalMovimentos();

    assertThrows(
        PecaVencidaNaoConfirmadaException.class,
        () -> receber(ctx, "VENCIDO-NAO", LocalDate.now().minusDays(2), "6", null, false));

    assertEquals(antes, totalMovimentos());
  }

  @Test
  void shouldDenySesmtAndConsulta() {
    Contexto ctx = contexto();
    long consulta = createUserWithRole("consulta.lote", Papel.CONSULTA);

    AuthorizationDeniedException sesmt =
        assertThrows(
            AuthorizationDeniedException.class,
            () -> receber(ctx, ctx.sesmtId(), "SESMT", LocalDate.now().plusDays(3), "1"));
    AuthorizationDeniedException leitor =
        assertThrows(
            AuthorizationDeniedException.class,
            () ->
                estoque.receiveLot(
                    consulta,
                    ctx.unitId(),
                    ctx.epiId(),
                    ctx.caId(),
                    "CONSULTA",
                    null,
                    null,
                    LocalDate.now().plusDays(3),
                    "1",
                    null,
                    false));

    assertEquals("AUTH-004 Voce nao tem permissao para executar esta acao.", sesmt.getMessage());
    assertEquals("AUTH-004 Voce nao tem permissao para executar esta acao.", leitor.getMessage());
  }

  @Test
  void shouldAllowAdminToReceive() {
    Contexto ctx = contexto();
    long admin = createUserWithRole("admin.lote", Papel.ADMIN);

    long loteId = receber(ctx, admin, "ADMIN-OK", LocalDate.now().plusDays(8), "2");

    assertEquals(2, quantidadeMovimento(loteId));
    assertEquals(1, auditSucesso(loteId, admin));
  }

  private long receber(
      Contexto ctx,
      String codigo,
      LocalDate validade,
      String quantidade,
      String custo,
      boolean aceita) {
    return receber(ctx, ctx.almoxId(), codigo, validade, quantidade, custo, aceita);
  }

  private long receber(
      Contexto ctx, long actorId, String codigo, LocalDate validade, String quantidade) {
    return receber(ctx, actorId, codigo, validade, quantidade, null, false);
  }

  private long receber(
      Contexto ctx,
      long actorId,
      String codigo,
      LocalDate validade,
      String quantidade,
      String custo,
      boolean aceita) {
    return estoque.receiveLot(
        actorId,
        ctx.unitId(),
        ctx.epiId(),
        ctx.caId(),
        codigo,
        null,
        "",
        validade,
        quantidade,
        custo,
        aceita);
  }

  private Contexto contexto() {
    long sesmtId = createUserWithRole("sesmt.lote", Papel.SESMT);
    long almoxId = createUserWithRole("almox.lote", Papel.ALMOXARIFE);
    long unitId =
        empregados.createUnit(sesmtId, "Unidade lote " + System.nanoTime(), proximoCnpj(), true);
    long epiId =
        catalogo.createEpi(
            sesmtId,
            "EPI-" + System.nanoTime(),
            "Luva de teste " + System.nanoTime(),
            AnnexGroup.A,
            "Fabricante teste",
            true);
    long caId =
        catalogo.bindCaToEpi(
            sesmtId,
            epiId,
            String.format("%05d", Math.floorMod(System.nanoTime(), 100000)),
            CaStatus.ACTIVE,
            LocalDate.now().minusDays(1),
            LocalDate.now().plusYears(1),
            LocalDateTime.now().withNano(0),
            "Consulta do teste",
            true,
            PrintConsulta.nome(),
            PrintConsulta.png());
    return new Contexto(sesmtId, almoxId, unitId, epiId, caId);
  }

  private long outraUnidadeAtiva(long atual) {
    Long id =
        jdbcTemplate.queryForObject(
            "SELECT id FROM unit WHERE active = 1 AND id <> ? ORDER BY id LIMIT 1",
            Long.class,
            atual);
    if (id != null) {
      return id;
    }
    throw new IllegalStateException("Sem segunda unidade ativa.");
  }

  private LotBalance saldo(long actorId, long unitId, String codigo) {
    return estoque.listLots(actorId, unitId).stream()
        .filter(lote -> codigo.equals(lote.lotCode()))
        .findFirst()
        .orElseThrow();
  }

  private String codigoOf(long loteId) {
    return jdbcTemplate.queryForObject(
        "SELECT lot_code FROM lote_epi WHERE id = ?", String.class, loteId);
  }

  private String fabricanteOf(long loteId) {
    return jdbcTemplate.queryForObject(
        "SELECT manufacturer FROM lote_epi WHERE id = ?", String.class, loteId);
  }

  private String tamanhoOf(long loteId) {
    return jdbcTemplate.queryForObject(
        "SELECT size_label FROM lote_epi WHERE id = ?", String.class, loteId);
  }

  private int custoOf(long loteId) {
    Integer custo =
        jdbcTemplate.queryForObject(
            "SELECT unit_cost_cents FROM lote_epi WHERE id = ?", Integer.class, loteId);
    return custo == null ? -1 : custo;
  }

  private Integer custoNulo(long loteId) {
    return jdbcTemplate.queryForObject(
        "SELECT unit_cost_cents FROM lote_epi WHERE id = ?", Integer.class, loteId);
  }

  private int movimentos(long loteId, String tipo) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM estoque_movimento WHERE lote_id = ? AND movement_type = ?",
            Integer.class,
            loteId,
            tipo);
    return count == null ? 0 : count;
  }

  private int quantidadeMovimento(long loteId) {
    Integer qty =
        jdbcTemplate.queryForObject(
            "SELECT quantity FROM estoque_movimento WHERE lote_id = ?", Integer.class, loteId);
    return qty == null ? 0 : qty;
  }

  private int movimentosDoCodigo(String codigo) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM estoque_movimento m
            JOIN lote_epi l ON l.id = m.lote_id
            WHERE l.lot_code = ?
            """,
            Integer.class,
            codigo);
    return count == null ? 0 : count;
  }

  private int totalMovimentos() {
    Integer count =
        jdbcTemplate.queryForObject("SELECT COUNT(1) FROM estoque_movimento", Integer.class);
    return count == null ? 0 : count;
  }

  private int auditSucesso(long loteId, long actorId) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = 'LOTE_RECEBIDO'
              AND entidade = 'LOTE'
              AND entidade_id = ?
              AND usuario_id = ?
              AND resultado = 'SUCESSO'
            """,
            Integer.class,
            String.valueOf(loteId),
            actorId);
    return count == null ? 0 : count;
  }

  private static String proximoCnpj() {
    String base = String.format("22755266%04d", FILIAL.incrementAndGet());
    int primeiro = digito(base, PESOS_PRIMEIRO);
    int segundo = digito(base + primeiro, PESOS_SEGUNDO);
    return base + primeiro + segundo;
  }

  private static int digito(String base, int[] pesos) {
    int soma = 0;
    for (int i = 0; i < pesos.length; i++) {
      soma += (base.charAt(i) - '0') * pesos[i];
    }
    int resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  }

  private long createUserWithRole(String loginBase, Papel role) {
    String login = loginBase + "." + System.nanoTime();
    jdbcTemplate.update(
        """
        INSERT INTO usuario (nome, login, senha_hash, ativo, credencial_troca_obrigatoria, tentativas_invalidas)
        VALUES (?, ?, ?, 1, 0, 0)
        """,
        "User " + login,
        login,
        passwordEncoder.encode("SenhaForte#2026"));
    Long userId =
        jdbcTemplate.queryForObject("SELECT id FROM usuario WHERE login = ?", Long.class, login);
    jdbcTemplate.update(
        """
        INSERT INTO usuario_papel (usuario_id, papel_id)
        VALUES (?, (SELECT id FROM papel WHERE codigo = ?))
        """,
        userId,
        role.name());
    return userId;
  }

  private record Contexto(long sesmtId, long almoxId, long unitId, long epiId, long caId) {}
}
