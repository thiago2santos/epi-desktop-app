package br.com.easynr6.gestaoepi.modules.issuance.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.identity.application.port.Clock;
import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import br.com.easynr6.gestaoepi.modules.employee.domain.UnitPolicy;
import br.com.easynr6.gestaoepi.modules.epi.PrintConsulta;
import br.com.easynr6.gestaoepi.modules.epi.application.EpiCatalogManagementService;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.HistoricoRepository.Linha;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarFornecimentoUseCase.ItemParaRegistrar;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarFornecimentoUseCase.Pedido;
import br.com.easynr6.gestaoepi.modules.issuance.domain.HistoricoPolicy;
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoDevolucao;
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoFornecimento;
import br.com.easynr6.gestaoepi.modules.matrix.application.MatrizManagementService;
import br.com.easynr6.gestaoepi.modules.stock.application.StockManagementService;
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

@Import(HistoricoManagementServiceIntegrationTest.RelogioConfig.class)
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:sqlite:file:historico-mem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class HistoricoManagementServiceIntegrationTest {

  private static final String MOTIVO = "lancamento na matricula errada";
  private static final LocalDate INICIO = LocalDate.of(2026, 10, 1);
  private static final LocalDate FIM = LocalDate.of(2026, 10, 5);
  private static final AtomicInteger FILIAL = new AtomicInteger(140);
  private static final AtomicInteger CA = new AtomicInteger(750000);
  private static final int[] PESOS_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] PESOS_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

  @Autowired private HistoricoManagementService historico;
  @Autowired private DevolucaoManagementService devolucao;
  @Autowired private EstornoManagementService estorno;
  @Autowired private FornecimentoManagementService fornecimento;
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
  void shouldListIssuanceReturnAndReversalWithoutWritingAudit() {
    Cenario cenario = cenario();
    long loteId = peca(cenario, 4);
    long devolvido = item(fornecer(cenario, loteId, 1));
    relogio.ajustar(LocalDateTime.of(2026, 10, 5, 11, 0));
    devolucao.registrar(
        cenario.almoxId(),
        devolvido,
        LocalDate.of(2026, 10, 5),
        MotivoDevolucao.DESLIGAMENTO,
        null);
    relogio.ajustar(LocalDateTime.of(2026, 10, 5, 12, 0));
    long estornado = item(fornecer(cenario, loteId, 1));
    relogio.ajustar(LocalDateTime.of(2026, 10, 5, 13, 0));
    estorno.registrar(cenario.sesmtId(), estornado, MOTIVO);
    estrutura.setEmployeeStatus(cenario.adminId(), cenario.employeeId(), false);
    int antes = auditoria();

    List<Linha> linhas = historico.listar(cenario.consultaId(), cenario.employeeId(), INICIO, FIM);

    assertEquals(4, linhas.size());
    assertEquals(1, contar(linhas, "FORNECIMENTO", HistoricoPolicy.ESTORNADO));
    assertEquals(1, contar(linhas, "FORNECIMENTO", HistoricoPolicy.DEVOLVIDO));
    assertEquals(1, contar(linhas, "DEVOLUCAO", HistoricoPolicy.DEVOLVIDO));
    assertEquals(1, contar(linhas, "ESTORNO", HistoricoPolicy.ESTORNADO));
    assertTrue(
        linhas.stream()
            .filter(linha -> "ESTORNO".equals(linha.fato()))
            .allMatch(linha -> linha.motivo().isEmpty()));
    assertTrue(linhas.stream().noneMatch(linha -> linha.fato().contains("PEDIDO")));
    assertEquals(antes, auditoria());
    assertEquals(
        0,
        historico
            .listar(
                cenario.consultaId(),
                cenario.employeeId(),
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2020, 1, 2))
            .size());
    assertTrue(
        historico.buscar(cenario.consultaId(), "Operador hist").stream()
            .anyMatch(
                trabalhador -> trabalhador.id() == cenario.employeeId() && !trabalhador.ativo()));
  }

  private static long contar(List<Linha> linhas, String fato, String situacao) {
    return linhas.stream()
        .filter(linha -> fato.equals(linha.fato()) && situacao.equals(linha.situacao()))
        .count();
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
    long adminId = usuario("admin.hist", Papel.ADMIN);
    long sesmtId = usuario("sesmt.hist", Papel.SESMT);
    long almoxId = usuario("almox.hist", Papel.ALMOXARIFE);
    long consultaId = usuario("consulta.hist", Papel.CONSULTA);
    long unitId =
        estrutura.createUnit(adminId, "Planta hist " + System.nanoTime(), proximoCnpj(), true);
    long departmentId = estrutura.createDepartment(adminId, "Setor " + unitId, unitId, true);
    long jobRoleId =
        estrutura.createJobRole(adminId, "Operador " + System.nanoTime(), departmentId, true);
    long employeeId =
        estrutura.createEmployee(
            adminId,
            "MAT" + System.nanoTime(),
            "Operador hist",
            departmentId,
            jobRoleId,
            null,
            true);
    return new Cenario(adminId, sesmtId, almoxId, consultaId, unitId, jobRoleId, employeeId);
  }

  private long peca(Cenario cenario, int quantidade) {
    long epiId =
        catalogo.createEpi(
            cenario.adminId(), "HIS", "Luva " + System.nanoTime(), AnnexGroup.F, true);
    long caId =
        catalogo.bindCaToEpi(
            cenario.adminId(),
            epiId,
            String.valueOf(CA.incrementAndGet()),
            CaStatus.ACTIVE,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2028, 1, 1),
            LocalDateTime.of(2026, 10, 5, 8, 0),
            "Consulta oficial para o historico",
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

  private int auditoria() {
    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM auditoria", Integer.class);
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
      long adminId,
      long sesmtId,
      long almoxId,
      long consultaId,
      long unitId,
      long jobRoleId,
      long employeeId) {}

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
