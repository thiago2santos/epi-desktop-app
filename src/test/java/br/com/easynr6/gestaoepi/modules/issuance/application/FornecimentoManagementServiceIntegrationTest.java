package br.com.easynr6.gestaoepi.modules.issuance.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.identity.application.port.Clock;
import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import br.com.easynr6.gestaoepi.modules.employee.domain.UnitPolicy;
import br.com.easynr6.gestaoepi.modules.epi.PrintConsulta;
import br.com.easynr6.gestaoepi.modules.epi.application.EpiCatalogManagementService;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarFornecimentoUseCase.ItemParaRegistrar;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarFornecimentoUseCase.Pedido;
import br.com.easynr6.gestaoepi.modules.issuance.domain.FornecimentoPolicy;
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoFornecimento;
import br.com.easynr6.gestaoepi.modules.matrix.application.MatrizManagementService;
import br.com.easynr6.gestaoepi.modules.matrix.application.PeriodicidadeManagementService;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository.Definicao;
import br.com.easynr6.gestaoepi.modules.matrix.domain.ModoMatriz;
import br.com.easynr6.gestaoepi.modules.stock.application.StockManagementService;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.LotBalance;
import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Import(FornecimentoManagementServiceIntegrationTest.RelogioConfig.class)
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:sqlite:file:fornecimento-mem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class FornecimentoManagementServiceIntegrationTest {

  private static final AtomicInteger FILIAL = new AtomicInteger(80);
  private static final AtomicInteger CA = new AtomicInteger(710000);
  private static final int[] PESOS_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] PESOS_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

  @Autowired private FornecimentoManagementService fornecimento;
  @Autowired private PeriodicidadeManagementService periodicidade;
  @Autowired private MatrizManagementService matriz;
  @Autowired private EmployeeManagementService estrutura;
  @Autowired private EpiCatalogManagementService catalogo;
  @Autowired private StockManagementService estoque;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private RelogioMutavel relogio;

  @BeforeEach
  void relogio() {
    relogio.ajustar(LocalDateTime.now().withNano(0));
  }

  @Test
  void shouldRegisterFichaTermMovementAndDropAvailableQuantity() {
    Cenario cenario = cenario("Planta ficha");
    Peca peca = pecaNaMatriz(cenario, "Luva ficha", "0.10", 5, LocalDate.now().plusDays(30), false);

    long fichaId =
        fornecimento.registrar(cenario.almoxId(), pedido(cenario.employeeId(), item(peca, 1)));

    assertEquals(1, fichas(cenario.employeeId()));
    assertEquals(peca.ca(), caDaFicha(fichaId));
    assertEquals(FornecimentoPolicy.TERMO_VERSAO, termo(fichaId, "versao"));
    assertEquals(FornecimentoPolicy.TERMO_METODO, termo(fichaId, "metodo"));
    assertEquals("BAIXA_FORNECIMENTO", movimento(peca.loteId(), "movement_type"));
    assertEquals(cenario.setor(), movimento(peca.loteId(), "department_name"));
    assertEquals(cenario.funcao(), movimento(peca.loteId(), "job_role_name"));
    assertEquals(10, custo(peca.loteId()));
    assertEquals(4, disponivel(cenario, peca.loteId()));
    assertEquals(1, auditoriaSucesso(fichaId));
    assertThrows(DataAccessException.class, () -> alterarFicha(fichaId));
  }

  @Test
  void shouldRefuseExpiredLotAndInsufficientQuantity() {
    Cenario cenario = cenario("Planta recusa");
    Peca vencida =
        pecaNaMatriz(cenario, "Bota vencida", null, 3, LocalDate.now().minusDays(1), true);
    Peca curta = pecaNaMatriz(cenario, "Bota curta", null, 1, LocalDate.now().plusDays(20), false);

    IllegalArgumentException expirada =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                fornecimento.registrar(
                    cenario.almoxId(), pedido(cenario.employeeId(), item(vencida, 1))));
    IllegalArgumentException saldo =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                fornecimento.registrar(
                    cenario.almoxId(), pedido(cenario.employeeId(), item(curta, 2))));

    assertTrue(expirada.getMessage().startsWith("ENT-004"));
    assertTrue(saldo.getMessage().startsWith("ENT-005"));
    assertEquals(0, fichas(cenario.employeeId()));
    assertEquals(0, baixas(vencida.loteId()));
    assertEquals(0, baixas(curta.loteId()));
  }

  @Test
  void shouldKeepOffMatrixItemsWithSesmtOnly() {
    Cenario cenario = cenario("Planta excecao");
    Peca fora = pecaSemMatriz(cenario, "Capacete fora", 2);

    IllegalArgumentException almox =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                fornecimento.registrar(
                    cenario.almoxId(),
                    pedido(cenario.employeeId(), itemExcecao(fora, 1, "texto longo de excecao"))));
    IllegalArgumentException curta =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                fornecimento.registrar(
                    cenario.adminId(), pedido(cenario.employeeId(), itemExcecao(fora, 1, "abc"))));
    long fichaId =
        fornecimento.registrar(
            cenario.adminId(), pedido(cenario.employeeId(), itemExcecao(fora, 1, "1234567890")));

    assertTrue(almox.getMessage().startsWith("ENT-006"));
    assertTrue(curta.getMessage().startsWith("ENT-007"));
    assertEquals("1234567890", excecao(fichaId));
  }

  @Test
  void shouldRequireTrainingDateAndASingleLotLine() {
    Cenario cenario = cenario("Planta treino");
    Peca treino = pecaComTreinamento(cenario, "Protetor treino");
    Peca lote = pecaNaMatriz(cenario, "Oculos unico", null, 4, LocalDate.now().plusDays(15), false);

    IllegalArgumentException semData =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                fornecimento.registrar(
                    cenario.almoxId(), pedido(cenario.employeeId(), item(treino, 1))));
    IllegalArgumentException repetido =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                fornecimento.registrar(
                    cenario.almoxId(),
                    new Pedido(cenario.employeeId(), true, List.of(item(lote, 1), item(lote, 1)))));

    assertTrue(semData.getMessage().startsWith("ENT-008"));
    assertTrue(repetido.getMessage().startsWith("ENT-012"));
    assertEquals(0, fichas(cenario.employeeId()));
  }

  @Test
  void shouldRefuseConfirmationWithoutTheTerm() {
    Cenario cenario = cenario("Planta termo");
    Peca peca = pecaNaMatriz(cenario, "Luva termo", null, 2, LocalDate.now().plusDays(10), false);
    Pedido semTermo = new Pedido(cenario.employeeId(), false, List.of(item(peca, 1)));

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> fornecimento.registrar(cenario.almoxId(), semTermo));

    assertTrue(ex.getMessage().startsWith("ENT-009"));
    assertEquals(0, fichas(cenario.employeeId()));
    assertEquals(0, baixas(peca.loteId()));
  }

  @Test
  void shouldAcceptNullCostAndDenyConsulta() {
    Cenario cenario = cenario("Planta custo");
    Peca peca =
        pecaNaMatriz(cenario, "Creme sem custo", null, 2, LocalDate.now().plusDays(10), false);
    long consulta = createUserWithRole("consulta.ficha", Papel.CONSULTA);

    long fichaId =
        fornecimento.registrar(cenario.almoxId(), pedido(cenario.employeeId(), item(peca, 1)));
    AuthorizationDeniedException negado =
        assertThrows(
            AuthorizationDeniedException.class,
            () -> fornecimento.registrar(consulta, pedido(cenario.employeeId(), item(peca, 1))));

    assertNull(custoOuNulo(peca.loteId()));
    assertEquals(1, fichas(cenario.employeeId()));
    assertTrue(negado.getMessage().startsWith("AUTH-004"));
    assertTrue(fichaId > 0);
  }

  @Test
  void shouldStampTheInjectedClockAndFeedCoverage() {
    LocalDateTime instante = LocalDate.now().atTime(14, 0);
    relogio.ajustar(instante);
    Cenario cenario = cenario("Planta relogio");
    Peca peca = pecaNaMatriz(cenario, "Luva relogio", null, 3, LocalDate.of(2099, 1, 1), false);
    Peca posto = pecaNaMatriz(cenario, "Creme posto", null, 3, LocalDate.of(2099, 1, 1), false);
    matriz.alterar(cenario.adminId(), posto.linhaId(), ModoMatriz.POSTO, false);
    periodicidade.salvar(cenario.adminId(), List.of(new Definicao(peca.epiId(), 180, 15)));

    long fichaId =
        fornecimento.registrar(
            cenario.almoxId(),
            new Pedido(cenario.employeeId(), true, List.of(item(peca, 1), item(posto, 1))));

    assertEquals(instante.toString(), confirmadaEm(fichaId));
    assertEquals("Vigente", situacao(cenario.employeeId(), peca.epiId()));
    assertEquals("Posto", situacao(cenario.employeeId(), posto.epiId()));
  }

  private Pedido pedido(long employeeId, ItemParaRegistrar item) {
    return new Pedido(employeeId, true, List.of(item));
  }

  private ItemParaRegistrar item(Peca peca, int quantidade) {
    return itemExcecao(peca, quantidade, null);
  }

  private ItemParaRegistrar itemExcecao(Peca peca, int quantidade, String excecao) {
    return new ItemParaRegistrar(
        peca.epiId(),
        peca.loteId(),
        quantidade,
        MotivoFornecimento.PRIMEIRA_ENTREGA,
        null,
        true,
        null,
        excecao,
        List.of(),
        null,
        null);
  }

  private Cenario cenario(String nome) {
    long adminId = createUserWithRole("admin.ficha", Papel.ADMIN);
    long almoxId = createUserWithRole("almox.ficha", Papel.ALMOXARIFE);
    long unitId =
        estrutura.createUnit(adminId, nome + " " + System.nanoTime(), proximoCnpj(), true);
    long departmentId = estrutura.createDepartment(adminId, "Setor " + unitId, unitId, true);
    String funcao = "Operador " + System.nanoTime();
    long jobRoleId = estrutura.createJobRole(adminId, funcao, departmentId, true);
    long employeeId =
        estrutura.createEmployee(
            adminId,
            "MAT" + System.nanoTime(),
            "Operador ficha",
            departmentId,
            jobRoleId,
            null,
            true);
    String setor =
        jdbcTemplate.queryForObject(
            "SELECT name FROM department WHERE id = ?", String.class, departmentId);
    return new Cenario(
        adminId, almoxId, unitId, departmentId, jobRoleId, employeeId, setor, funcao);
  }

  private Peca pecaNaMatriz(
      Cenario cenario,
      String descricao,
      String custo,
      int quantidade,
      LocalDate validade,
      boolean vencida) {
    Peca peca = pecaSemMatriz(cenario, descricao, quantidade, custo, validade, vencida);
    long linhaId =
        matriz.incluir(
            cenario.adminId(), PerfilVigente.Tipo.FUNCAO, cenario.jobRoleId(), peca.epiId());
    return new Peca(peca.epiId(), peca.loteId(), peca.ca(), linhaId);
  }

  private Peca pecaComTreinamento(Cenario cenario, String descricao) {
    Peca peca = pecaNaMatriz(cenario, descricao, null, 2, LocalDate.now().plusDays(40), false);
    matriz.alterar(cenario.adminId(), peca.linhaId(), ModoMatriz.INDIVIDUAL, true);
    return peca;
  }

  private Peca pecaSemMatriz(Cenario cenario, String descricao, int quantidade) {
    return pecaSemMatriz(cenario, descricao, quantidade, null, LocalDate.now().plusDays(30), false);
  }

  private Peca pecaSemMatriz(
      Cenario cenario,
      String descricao,
      int quantidade,
      String custo,
      LocalDate validade,
      boolean vencida) {
    long epiId =
        catalogo.createEpi(
            cenario.adminId(), "ENT", descricao + " " + System.nanoTime(), AnnexGroup.F, true);
    String ca = String.valueOf(CA.incrementAndGet());
    long caId =
        catalogo.bindCaToEpi(
            cenario.adminId(),
            epiId,
            ca,
            CaStatus.ACTIVE,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2028, 1, 1),
            LocalDateTime.of(2026, 10, 5, 8, 0),
            "Consulta oficial para o fornecimento",
            true,
            PrintConsulta.nome(),
            PrintConsulta.png());
    long loteId =
        estoque.receiveLot(
            cenario.adminId(),
            cenario.unitId(),
            epiId,
            caId,
            "L" + System.nanoTime(),
            null,
            "",
            validade,
            String.valueOf(quantidade),
            custo,
            vencida);
    return new Peca(epiId, loteId, ca, 0);
  }

  private int disponivel(Cenario cenario, long loteId) {
    return estoque.listLots(cenario.adminId(), cenario.unitId()).stream()
        .filter(lote -> lote.id().equals(loteId))
        .map(LotBalance::disponivel)
        .findFirst()
        .orElse(-1);
  }

  private String situacao(long employeeId, long epiId) {
    return periodicidade.cobertura(employeeId).stream()
        .filter(leitura -> leitura.epiId().equals(epiId))
        .map(leitura -> leitura.situacao())
        .findFirst()
        .orElse("");
  }

  private int fichas(long employeeId) {
    return contar("SELECT COUNT(1) FROM fornecimento_ficha WHERE employee_id = ?", employeeId);
  }

  private int baixas(long loteId) {
    return contar(
        """
        SELECT COUNT(1) FROM estoque_movimento
        WHERE lote_id = ? AND movement_type = 'BAIXA_FORNECIMENTO'
        """,
        loteId);
  }

  private String caDaFicha(long fichaId) {
    return jdbcTemplate.queryForObject(
        """
        SELECT c.ca_number
        FROM fornecimento_item_ca c
        JOIN fornecimento_item i ON i.id = c.item_id
        WHERE i.ficha_id = ?
        """,
        String.class,
        fichaId);
  }

  private String termo(long fichaId, String coluna) {
    return jdbcTemplate.queryForObject(
        "SELECT " + coluna + " FROM fornecimento_termo WHERE ficha_id = ?", String.class, fichaId);
  }

  private String excecao(long fichaId) {
    return jdbcTemplate.queryForObject(
        "SELECT excecao_texto FROM fornecimento_item WHERE ficha_id = ?", String.class, fichaId);
  }

  private String confirmadaEm(long fichaId) {
    return jdbcTemplate.queryForObject(
        "SELECT confirmed_at FROM fornecimento_ficha WHERE id = ?", String.class, fichaId);
  }

  private String movimento(long loteId, String coluna) {
    return jdbcTemplate.queryForObject(
        "SELECT "
            + coluna
            + " FROM estoque_movimento WHERE lote_id = ? AND movement_type = 'BAIXA_FORNECIMENTO'",
        String.class,
        loteId);
  }

  private int custo(long loteId) {
    Integer valor =
        jdbcTemplate.queryForObject(
            """
            SELECT unit_cost_cents FROM estoque_movimento
            WHERE lote_id = ? AND movement_type = 'BAIXA_FORNECIMENTO'
            """,
            Integer.class,
            loteId);
    return valor == null ? -1 : valor;
  }

  private Integer custoOuNulo(long loteId) {
    return jdbcTemplate.queryForObject(
        """
        SELECT unit_cost_cents FROM estoque_movimento
        WHERE lote_id = ? AND movement_type = 'BAIXA_FORNECIMENTO'
        """,
        Integer.class,
        loteId);
  }

  private void alterarFicha(long fichaId) {
    jdbcTemplate.update(
        "UPDATE fornecimento_ficha SET employee_name = 'outro' WHERE id = ?", fichaId);
  }

  private int auditoriaSucesso(long fichaId) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = 'FORNECIMENTO_REGISTRADO'
              AND entidade = 'FORNECIMENTO'
              AND entidade_id = ?
              AND resultado = 'SUCESSO'
            """,
            Integer.class,
            String.valueOf(fichaId));
    return count == null ? 0 : count;
  }

  private int contar(String sql, long id) {
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
    return count == null ? 0 : count;
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

  private static String proximoCnpj() {
    String base = String.format("33755266%04d", FILIAL.incrementAndGet());
    int primeiro = digito(base, PESOS_PRIMEIRO);
    int segundo = digito(base + primeiro, PESOS_SEGUNDO);
    return UnitPolicy.formatarCnpj(base + primeiro + segundo);
  }

  private static int digito(String base, int[] pesos) {
    int soma = 0;
    for (int i = 0; i < pesos.length; i++) {
      soma += (base.charAt(i) - '0') * pesos[i];
    }
    int resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  }

  private record Cenario(
      long adminId,
      long almoxId,
      long unitId,
      long departmentId,
      long jobRoleId,
      long employeeId,
      String setor,
      String funcao) {}

  private record Peca(long epiId, long loteId, String ca, long linhaId) {}

  static final class RelogioMutavel implements Clock {
    private LocalDateTime atual = LocalDateTime.now().withNano(0);

    void ajustar(LocalDateTime valor) {
      atual = valor;
    }

    @Override
    public LocalDateTime now() {
      return atual;
    }
  }

  @TestConfiguration
  static class RelogioConfig {
    @Bean
    @Primary
    RelogioMutavel relogioMutavel() {
      return new RelogioMutavel();
    }
  }
}
