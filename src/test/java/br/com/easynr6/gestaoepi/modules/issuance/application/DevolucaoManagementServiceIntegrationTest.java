package br.com.easynr6.gestaoepi.modules.issuance.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoDevolucao;
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoFornecimento;
import br.com.easynr6.gestaoepi.modules.matrix.application.MatrizManagementService;
import br.com.easynr6.gestaoepi.modules.matrix.application.PeriodicidadeManagementService;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository.Definicao;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Import(DevolucaoManagementServiceIntegrationTest.RelogioConfig.class)
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:sqlite:file:devolucao-mem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class DevolucaoManagementServiceIntegrationTest {

  private static final AtomicInteger FILIAL = new AtomicInteger(100);
  private static final AtomicInteger CA = new AtomicInteger(730000);
  private static final int[] PESOS_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] PESOS_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

  @Autowired private DevolucaoManagementService devolucao;
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
    relogio.ajustar(LocalDateTime.of(2026, 10, 5, 10, 0));
  }

  @Test
  void shouldRegisterReturnWithoutPuttingThePieceBackOnTheShelf() {
    Cenario cenario = cenario();
    long loteId = peca(cenario, 5);
    long fichaId = fornecer(cenario, loteId, 1);
    long itemId = item(fichaId);
    periodicidade.salvar(cenario.adminId(), List.of(new Definicao(epi(loteId), 180, 15)));

    long id =
        devolucao.registrar(
            cenario.almoxId(),
            itemId,
            LocalDate.of(2026, 10, 5),
            MotivoDevolucao.DESLIGAMENTO,
            null);

    assertTrue(id > 0);
    assertEquals(4, disponivel(cenario, loteId));
    assertEquals(1, auditoria(id));
    assertEquals("Pendente", situacao(cenario.employeeId(), epi(loteId)));
    IllegalArgumentException deNovo =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                devolucao.registrar(
                    cenario.almoxId(),
                    itemId,
                    LocalDate.of(2026, 10, 5),
                    MotivoDevolucao.DESLIGAMENTO,
                    null));
    assertTrue(deNovo.getMessage().startsWith("POS-001"));
  }

  @Test
  void shouldRefuseADateBeforeTheIssuanceAndDenyConsulta() {
    Cenario cenario = cenario();
    long loteId = peca(cenario, 2);
    long itemId = item(fornecer(cenario, loteId, 1));
    long consulta = usuario("consulta.dev", Papel.CONSULTA);

    IllegalArgumentException data =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                devolucao.registrar(
                    cenario.almoxId(),
                    itemId,
                    LocalDate.of(2026, 10, 4),
                    MotivoDevolucao.DANO,
                    null));
    AuthorizationDeniedException negado =
        assertThrows(
            AuthorizationDeniedException.class,
            () ->
                devolucao.registrar(
                    consulta, itemId, LocalDate.of(2026, 10, 5), MotivoDevolucao.DANO, null));

    assertTrue(data.getMessage().startsWith("POS-002"));
    assertTrue(negado.getMessage().startsWith("AUTH-004"));
    assertEquals(
        0, contar("SELECT COUNT(1) FROM fornecimento_devolucao WHERE item_id = ?", itemId));
  }

  private long fornecer(Cenario cenario, long loteId, int quantidade) {
    return fornecimento.registrar(
        cenario.almoxId(),
        new Pedido(
            cenario.employeeId(),
            true,
            List.of(
                new ItemParaRegistrar(
                    epi(loteId),
                    loteId,
                    quantidade,
                    MotivoFornecimento.PRIMEIRA_ENTREGA,
                    null,
                    true,
                    null,
                    null,
                    List.of(),
                    null,
                    null))));
  }

  private Cenario cenario() {
    long adminId = usuario("admin.dev", Papel.ADMIN);
    long almoxId = usuario("almox.dev", Papel.ALMOXARIFE);
    long unitId =
        estrutura.createUnit(adminId, "Planta dev " + System.nanoTime(), proximoCnpj(), true);
    long departmentId = estrutura.createDepartment(adminId, "Setor " + unitId, unitId, true);
    long jobRoleId =
        estrutura.createJobRole(adminId, "Operador " + System.nanoTime(), departmentId, true);
    long employeeId =
        estrutura.createEmployee(
            adminId,
            "MAT" + System.nanoTime(),
            "Operador dev",
            departmentId,
            jobRoleId,
            null,
            true);
    return new Cenario(adminId, almoxId, unitId, jobRoleId, employeeId);
  }

  private long peca(Cenario cenario, int quantidade) {
    long epiId =
        catalogo.createEpi(
            cenario.adminId(), "DEV", "Luva " + System.nanoTime(), AnnexGroup.F, true);
    long caId =
        catalogo.bindCaToEpi(
            cenario.adminId(),
            epiId,
            String.valueOf(CA.incrementAndGet()),
            CaStatus.ACTIVE,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2028, 1, 1),
            LocalDateTime.of(2026, 10, 5, 8, 0),
            "Consulta oficial para a devolucao",
            true,
            PrintConsulta.nome(),
            PrintConsulta.png());
    matriz.incluir(cenario.adminId(), PerfilVigente.Tipo.FUNCAO, cenario.jobRoleId(), epiId);
    return estoque.receiveLot(
        cenario.adminId(),
        cenario.unitId(),
        epiId,
        caId,
        "L" + System.nanoTime(),
        null,
        "",
        LocalDate.of(2099, 1, 1),
        String.valueOf(quantidade),
        null,
        false);
  }

  private long epi(long loteId) {
    return jdbcTemplate.queryForObject(
        "SELECT epi_id FROM lote_epi WHERE id = ?", Long.class, loteId);
  }

  private long item(long fichaId) {
    return jdbcTemplate.queryForObject(
        "SELECT id FROM fornecimento_item WHERE ficha_id = ?", Long.class, fichaId);
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

  private int auditoria(long devolucaoId) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = 'DEVOLUCAO_REGISTRADA'
              AND entidade = 'DEVOLUCAO'
              AND entidade_id = ?
              AND resultado = 'SUCESSO'
            """,
            Integer.class,
            String.valueOf(devolucaoId));
    return count == null ? 0 : count;
  }

  private int contar(String sql, long id) {
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
    return count == null ? 0 : count;
  }

  private long usuario(String loginBase, Papel role) {
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
    String base = String.format("55755266%04d", FILIAL.incrementAndGet());
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
      long adminId, long almoxId, long unitId, long jobRoleId, long employeeId) {}

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
