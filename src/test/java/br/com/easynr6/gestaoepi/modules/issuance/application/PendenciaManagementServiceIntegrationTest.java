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
import br.com.easynr6.gestaoepi.modules.issuance.application.port.PendenciaRepository.Pendencia;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarFornecimentoUseCase.ItemParaRegistrar;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarFornecimentoUseCase.Pedido;
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoDevolucao;
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoFornecimento;
import br.com.easynr6.gestaoepi.modules.matrix.application.MatrizManagementService;
import br.com.easynr6.gestaoepi.modules.matrix.domain.ModoMatriz;
import br.com.easynr6.gestaoepi.modules.stock.application.StockManagementService;
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

@Import(PendenciaManagementServiceIntegrationTest.RelogioConfig.class)
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:sqlite:file:pendencia-mem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class PendenciaManagementServiceIntegrationTest {

  private static final String FORA = "fora da matriz neste desligamento";
  private static final String ESTORNO = "lancamento na matricula errada";
  private static final AtomicInteger FILIAL = new AtomicInteger(160);
  private static final AtomicInteger CA = new AtomicInteger(760000);
  private static final int[] PESOS_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] PESOS_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

  @Autowired private PendenciaManagementService pendencia;
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
  void shouldListIndividualItemsOfInactiveWorkersWithoutWritingAudit() {
    Cenario cenario = cenario();
    Peca luva = peca(cenario, "Luva", true, ModoMatriz.INDIVIDUAL);
    Peca creme = peca(cenario, "Creme", true, ModoMatriz.POSTO);
    Peca fora = peca(cenario, "Oculos", false, null);
    Peca errado = peca(cenario, "Bota", false, null);
    long luvaItem = item(fornecer(cenario.almoxId(), cenario.inativoId(), luva, null));
    fornecer(cenario.almoxId(), cenario.ativoId(), luva, null);
    fornecer(cenario.almoxId(), cenario.inativoId(), creme, null);
    long foraItem = item(fornecer(cenario.sesmtId(), cenario.inativoId(), fora, FORA));
    long erradoItem = item(fornecer(cenario.sesmtId(), cenario.inativoId(), errado, FORA));
    estorno.registrar(cenario.sesmtId(), erradoItem, ESTORNO);
    estrutura.setEmployeeStatus(cenario.adminId(), cenario.inativoId(), false);
    int antes = auditoria();

    List<Pendencia> lista = pendencia.listar(cenario.almoxId(), null, null, null, null);

    assertEquals(antes, auditoria());
    assertTrue(contem(lista, luvaItem));
    assertTrue(contem(lista, foraItem));
    assertTrue(lista.stream().noneMatch(linha -> linha.epi().startsWith("Creme")));
    assertTrue(lista.stream().noneMatch(linha -> linha.epi().startsWith("Bota")));
    assertTrue(lista.stream().noneMatch(linha -> linha.employeeId() == cenario.ativoId()));
    AuthorizationDeniedException negado =
        assertThrows(
            AuthorizationDeniedException.class,
            () -> pendencia.listar(cenario.consultaId(), null, null, null, null));
    assertTrue(negado.getMessage().startsWith("AUTH-004"));
    assertTrue(
        pendencia.listar(cenario.almoxId(), cenario.unitId() + 99, null, null, null).isEmpty());

    devolucao.registrar(
        cenario.almoxId(), luvaItem, LocalDate.of(2026, 10, 5), MotivoDevolucao.DESLIGAMENTO, null);
    List<Pendencia> depois =
        pendencia.listar(cenario.almoxId(), null, null, null, cenario.inativoId());
    assertTrue(depois.stream().noneMatch(linha -> linha.itemId() == luvaItem));
    assertTrue(contem(depois, foraItem));
  }

  private static boolean contem(List<Pendencia> lista, long itemId) {
    return lista.stream().anyMatch(linha -> linha.itemId() == itemId);
  }

  private long fornecer(long actorId, long employeeId, Peca peca, String excecao) {
    return fornecimento.registrar(
        actorId,
        new Pedido(
            employeeId,
            true,
            List.of(
                new ItemParaRegistrar(
                    peca.epiId(),
                    peca.loteId(),
                    1,
                    MotivoFornecimento.PRIMEIRA_ENTREGA,
                    null,
                    true,
                    null,
                    excecao,
                    List.of(),
                    null,
                    null))));
  }

  private Cenario cenario() {
    long adminId = usuario("admin.pen", Papel.ADMIN);
    long sesmtId = usuario("sesmt.pen", Papel.SESMT);
    long almoxId = usuario("almox.pen", Papel.ALMOXARIFE);
    long consultaId = usuario("consulta.pen", Papel.CONSULTA);
    long unitId =
        estrutura.createUnit(adminId, "Planta pen " + System.nanoTime(), proximoCnpj(), true);
    long departmentId = estrutura.createDepartment(adminId, "Setor " + unitId, unitId, true);
    long jobRoleId =
        estrutura.createJobRole(adminId, "Operador " + System.nanoTime(), departmentId, true);
    long inativoId =
        estrutura.createEmployee(
            adminId, "MAT" + System.nanoTime(), "Saiu pen", departmentId, jobRoleId, null, true);
    long ativoId =
        estrutura.createEmployee(
            adminId, "MAT" + System.nanoTime(), "Fica pen", departmentId, jobRoleId, null, true);
    return new Cenario(
        adminId, sesmtId, almoxId, consultaId, unitId, jobRoleId, inativoId, ativoId);
  }

  private Peca peca(Cenario cenario, String nome, boolean naMatriz, ModoMatriz modo) {
    long epiId =
        catalogo.createEpi(
            cenario.adminId(), "PEN", nome + " " + System.nanoTime(), AnnexGroup.F, true);
    long caId =
        catalogo.bindCaToEpi(
            cenario.adminId(),
            epiId,
            String.valueOf(CA.incrementAndGet()),
            CaStatus.ACTIVE,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2028, 1, 1),
            LocalDateTime.of(2026, 10, 5, 8, 0),
            "Consulta oficial para a pendencia",
            true,
            PrintConsulta.nome(),
            PrintConsulta.png());
    if (naMatriz) {
      matriz.incluir(cenario.adminId(), PerfilVigente.Tipo.FUNCAO, cenario.jobRoleId(), epiId);
      if (modo == ModoMatriz.POSTO) {
        long linhaId =
            matriz
                .listarLinhas(cenario.adminId(), PerfilVigente.Tipo.FUNCAO, cenario.jobRoleId())
                .stream()
                .filter(linha -> linha.epiId().equals(epiId))
                .findFirst()
                .orElseThrow()
                .id();
        matriz.alterar(cenario.adminId(), linhaId, ModoMatriz.POSTO, false);
      }
    }
    long loteId =
        estoque.receiveLot(
            cenario.adminId(),
            cenario.unitId(),
            epiId,
            caId,
            "L" + System.nanoTime(),
            null,
            "",
            LocalDate.of(2099, 1, 1),
            "4",
            null,
            false);
    return new Peca(epiId, loteId);
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
      long inativoId,
      long ativoId) {}

  private record Peca(long epiId, long loteId) {}

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
