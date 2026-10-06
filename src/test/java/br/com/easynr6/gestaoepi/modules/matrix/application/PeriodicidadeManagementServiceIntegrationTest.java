package br.com.easynr6.gestaoepi.modules.matrix.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import br.com.easynr6.gestaoepi.modules.employee.domain.UnitPolicy;
import br.com.easynr6.gestaoepi.modules.epi.PrintConsulta;
import br.com.easynr6.gestaoepi.modules.epi.application.EpiCatalogManagementService;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository.Definicao;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository.LinhaPeriodicidade;
import br.com.easynr6.gestaoepi.modules.matrix.application.usecase.LerCoberturaUseCase.LeituraCobertura;
import br.com.easynr6.gestaoepi.modules.matrix.domain.ModoMatriz;
import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:sqlite:file:periodicidade-mem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class PeriodicidadeManagementServiceIntegrationTest {

  private static final AtomicInteger FILIAL = new AtomicInteger(50);
  private static final AtomicInteger CA = new AtomicInteger(610000);
  private static final int[] PESOS_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] PESOS_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

  @Autowired private PeriodicidadeManagementService periodicidade;
  @Autowired private MatrizManagementService matriz;
  @Autowired private EmployeeManagementService estrutura;
  @Autowired private EpiCatalogManagementService catalogo;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void shouldSaveDaysAndWarningOncePerEpi() {
    long sesmt = createUserWithRole("sesmt.prazo", Papel.SESMT);
    Contexto ctx = contexto(sesmt, "Planta prazo");
    long epiId = epiNaMatriz(sesmt, ctx, "Luva prazo", ModoMatriz.INDIVIDUAL);

    int gravadas = periodicidade.salvar(sesmt, List.of(new Definicao(epiId, 180, 15)));
    int deNovo = periodicidade.salvar(sesmt, List.of(new Definicao(epiId, 180, 15)));

    assertEquals(1, gravadas);
    assertEquals(0, deNovo);
    assertEquals(180, dias(epiId));
    assertEquals(15, aviso(epiId));
    assertEquals(1, auditoria(epiId));
    assertEquals(
        1,
        periodicidade.listar(sesmt).stream().filter(linha -> linha.epiId().equals(epiId)).count());
    assertEquals(1, funcoes(sesmt, epiId));
  }

  @Test
  void shouldRejectInvalidWarningWithoutReplacingTheSavedPeriod() {
    long sesmt = createUserWithRole("sesmt.prazo.aviso", Papel.SESMT);
    Contexto ctx = contexto(sesmt, "Planta aviso");
    long epiId = epiNaMatriz(sesmt, ctx, "Capacete aviso", ModoMatriz.INDIVIDUAL);
    periodicidade.salvar(sesmt, List.of(new Definicao(epiId, 180, 15)));

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> periodicidade.salvar(sesmt, List.of(new Definicao(epiId, 180, 180))));
    IllegalArgumentException zero =
        assertThrows(
            IllegalArgumentException.class,
            () -> periodicidade.salvar(sesmt, List.of(new Definicao(epiId, 0, 0))));

    assertTrue(ex.getMessage().startsWith("MAT-006"));
    assertTrue(zero.getMessage().startsWith("MAT-005"));
    assertEquals(180, dias(epiId));
    assertEquals(15, aviso(epiId));
  }

  @Test
  void shouldHideEpiThatIsNotOnAnActiveMatrixLine() {
    long sesmt = createUserWithRole("sesmt.prazo.fora", Papel.SESMT);
    Contexto ctx = contexto(sesmt, "Planta fora");
    long naMatriz = epiComCa(sesmt, "Na matriz");
    long fora = catalogo.createEpi(sesmt, "FORA", "Fora " + System.nanoTime(), AnnexGroup.B, true);
    long linha = matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, ctx.jobRoleId(), naMatriz);
    matriz.inativar(sesmt, linha);

    List<Long> visiveis =
        periodicidade.listar(sesmt).stream().map(LinhaPeriodicidade::epiId).toList();

    assertTrue(visiveis.stream().noneMatch(id -> id.equals(fora) || id.equals(naMatriz)));
  }

  @Test
  void shouldReadNoDeadlinePendingAndPostoUntilIssuanceExists() {
    long sesmt = createUserWithRole("sesmt.prazo.leitura", Papel.SESMT);
    Contexto ctx = contexto(sesmt, "Planta leitura");
    long semPrazo = epiNaMatriz(sesmt, ctx, "Sem prazo", ModoMatriz.INDIVIDUAL);
    long comPrazo = epiNaMatriz(sesmt, ctx, "Com prazo", ModoMatriz.INDIVIDUAL);
    long posto = epiNaMatriz(sesmt, ctx, "No posto", ModoMatriz.POSTO);
    periodicidade.salvar(sesmt, List.of(new Definicao(comPrazo, 180, 15)));
    long trabalhador =
        estrutura.createEmployee(
            sesmt,
            "MAT" + System.nanoTime(),
            "Operador leitura",
            ctx.departmentId(),
            ctx.jobRoleId(),
            null,
            true);

    List<LeituraCobertura> leitura = periodicidade.cobertura(trabalhador);

    assertEquals("Sem prazo", situacao(leitura, semPrazo));
    assertEquals("Pendente", situacao(leitura, comPrazo));
    assertEquals("Posto", situacao(leitura, posto));
  }

  @Test
  void shouldCountEveryFunctionThatSharesTheEpi() {
    long sesmt = createUserWithRole("sesmt.prazo.funcoes", Papel.SESMT);
    long unitId = unidade(sesmt, "Planta compartilhada");
    long departmentId = estrutura.createDepartment(sesmt, "Setor " + unitId, unitId, true);
    long primeira = estrutura.createJobRole(sesmt, "A " + System.nanoTime(), departmentId, true);
    long segunda = estrutura.createJobRole(sesmt, "B " + System.nanoTime(), departmentId, true);
    long epiId = epiComCa(sesmt, "Luva compartilhada");
    matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, primeira, epiId);
    matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, segunda, epiId);

    assertEquals(
        1,
        periodicidade.listar(sesmt).stream().filter(linha -> linha.epiId().equals(epiId)).count());
    assertEquals(2, funcoes(sesmt, epiId));
  }

  @Test
  void shouldDenyAlmoxarife() {
    long sesmt = createUserWithRole("sesmt.prazo.papel", Papel.SESMT);
    long almox = createUserWithRole("almox.prazo", Papel.ALMOXARIFE);
    Contexto ctx = contexto(sesmt, "Planta papel");
    long epiId = epiNaMatriz(sesmt, ctx, "Avental papel", ModoMatriz.INDIVIDUAL);

    AuthorizationDeniedException ex =
        assertThrows(
            AuthorizationDeniedException.class,
            () -> periodicidade.salvar(almox, List.of(new Definicao(epiId, 90, 10))));

    assertTrue(ex.getMessage().startsWith("AUTH-004"));
    assertEquals(0, countPrazo(epiId));
  }

  private String situacao(List<LeituraCobertura> leitura, long epiId) {
    return leitura.stream()
        .filter(item -> item.epiId().equals(epiId))
        .map(LeituraCobertura::situacao)
        .findFirst()
        .orElse("");
  }

  private int funcoes(long actorId, long epiId) {
    return periodicidade.listar(actorId).stream()
        .filter(linha -> linha.epiId().equals(epiId))
        .map(LinhaPeriodicidade::funcoes)
        .findFirst()
        .orElse(-1);
  }

  private Contexto contexto(long actorId, String nomeUnidade) {
    long unitId = unidade(actorId, nomeUnidade);
    long departmentId = estrutura.createDepartment(actorId, "Operacao " + unitId, unitId, true);
    long jobRoleId =
        estrutura.createJobRole(actorId, "Operador " + System.nanoTime(), departmentId, true);
    return new Contexto(departmentId, jobRoleId);
  }

  private long unidade(long actorId, String nome) {
    return estrutura.createUnit(actorId, nome + " " + System.nanoTime(), proximoCnpj(), true);
  }

  private long epiNaMatriz(long actorId, Contexto ctx, String descricao, ModoMatriz modo) {
    long epiId = epiComCa(actorId, descricao);
    long linhaId = matriz.incluir(actorId, PerfilVigente.Tipo.FUNCAO, ctx.jobRoleId(), epiId);
    if (modo == ModoMatriz.POSTO) {
      matriz.alterar(actorId, linhaId, ModoMatriz.POSTO, false);
    }
    return epiId;
  }

  private long epiComCa(long actorId, String descricao) {
    long epiId =
        catalogo.createEpi(actorId, "PRZ", descricao + " " + System.nanoTime(), AnnexGroup.F, true);
    catalogo.bindCaToEpi(
        actorId,
        epiId,
        String.valueOf(CA.incrementAndGet()),
        CaStatus.ACTIVE,
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2028, 1, 1),
        LocalDateTime.of(2026, 10, 5, 8, 0),
        "Consulta oficial para a periodicidade",
        true,
        PrintConsulta.nome(),
        PrintConsulta.png());
    return epiId;
  }

  private int dias(long epiId) {
    Integer valor =
        jdbcTemplate.queryForObject(
            "SELECT dias FROM periodicidade_epi WHERE epi_id = ?", Integer.class, epiId);
    return valor == null ? -1 : valor;
  }

  private int aviso(long epiId) {
    Integer valor =
        jdbcTemplate.queryForObject(
            "SELECT aviso_dias FROM periodicidade_epi WHERE epi_id = ?", Integer.class, epiId);
    return valor == null ? -1 : valor;
  }

  private int countPrazo(long epiId) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM periodicidade_epi WHERE epi_id = ?", Integer.class, epiId);
    return count == null ? 0 : count;
  }

  private int auditoria(long epiId) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = 'PERIODICIDADE_DEFINIDA'
              AND entidade = 'PERIODICIDADE'
              AND entidade_id = ?
              AND resultado = 'SUCESSO'
            """,
            Integer.class,
            String.valueOf(epiId));
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
    String base = String.format("22755266%04d", FILIAL.incrementAndGet());
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

  private record Contexto(long departmentId, long jobRoleId) {}
}
