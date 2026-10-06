package br.com.easynr6.gestaoepi.modules.employee.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository.FuncaoDoGhe;
import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import br.com.easynr6.gestaoepi.modules.employee.domain.UnitPolicy;
import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:sqlite:file:ghe-mem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class GheManagementServiceIntegrationTest {

  private static final AtomicInteger FILIAL = new AtomicInteger(30);
  private static final int[] PESOS_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] PESOS_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

  @Autowired private EmployeeManagementService estrutura;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void shouldCreateGheAndListItWithAudit() {
    long sesmt = createUserWithRole("sesmt.ghe", Papel.SESMT);
    long unitId = unidade(sesmt, "Planta ruido");

    long gheId = estrutura.createGhe(sesmt, unitId, "  Ruido caldeira  ", true);

    assertEquals("Ruido caldeira", nome(gheId));
    assertEquals(1, estrutura.listGhes(sesmt, unitId, "caldeira").size());
    assertEquals(1, auditoria(gheId, "GHE_CRIADO"));
  }

  @Test
  void shouldRejectBlankNameOrMissingUnitWithoutInsert() {
    long sesmt = createUserWithRole("sesmt.ghe.obrigatorio", Papel.SESMT);
    long unitId = unidade(sesmt, "Planta obrigatoria");
    int antes = countGhe();

    IllegalArgumentException semNome =
        assertThrows(
            IllegalArgumentException.class, () -> estrutura.createGhe(sesmt, unitId, " ", true));
    IllegalArgumentException semUnidade =
        assertThrows(
            IllegalArgumentException.class, () -> estrutura.createGhe(sesmt, null, "Ruido", true));

    assertTrue(semNome.getMessage().startsWith("CAD-051"));
    assertTrue(semUnidade.getMessage().startsWith("CAD-051"));
    assertEquals(antes, countGhe());
  }

  @Test
  void shouldRejectSameNameInTheUnitAndAllowItInAnother() {
    long sesmt = createUserWithRole("sesmt.ghe.nome", Papel.SESMT);
    long unidadeA = unidade(sesmt, "Planta A");
    long unidadeB = unidade(sesmt, "Planta B");
    estrutura.createGhe(sesmt, unidadeA, "Ruido", true);

    IllegalArgumentException duplicado =
        assertThrows(
            IllegalArgumentException.class,
            () -> estrutura.createGhe(sesmt, unidadeA, " ruido ", true));
    long outro = estrutura.createGhe(sesmt, unidadeB, "Ruido", true);

    assertTrue(duplicado.getMessage().startsWith("CAD-052"));
    assertEquals("Ruido", nome(outro));
  }

  @Test
  void shouldLinkOnceAndKeepTheMembership() {
    long sesmt = createUserWithRole("sesmt.ghe.vinculo", Papel.SESMT);
    Contexto ctx = contexto(sesmt, "Planta vinculo");
    long gheId = estrutura.createGhe(sesmt, ctx.unitId(), "Ruido", true);

    estrutura.linkJobRoleToGhe(sesmt, gheId, ctx.jobRoleId());
    estrutura.linkJobRoleToGhe(sesmt, gheId, ctx.jobRoleId());

    assertEquals(1, vinculos(ctx.jobRoleId()));
    assertEquals(1, auditoria(gheId, "GHE_FUNCAO_VINCULADA"));
    assertEquals(1, estrutura.listGheMembers(sesmt, gheId).size());
    assertTrue(estrutura.listGheCandidates(sesmt, ctx.unitId()).isEmpty());
  }

  @Test
  void shouldRejectInactiveForeignOrTakenJobRole() {
    long sesmt = createUserWithRole("sesmt.ghe.recusa", Papel.SESMT);
    Contexto ctx = contexto(sesmt, "Planta recusa");
    long outroGrupo = estrutura.createGhe(sesmt, ctx.unitId(), "Outro", true);
    estrutura.linkJobRoleToGhe(sesmt, outroGrupo, ctx.jobRoleId());
    long gheId = estrutura.createGhe(sesmt, ctx.unitId(), "Ruido", true);
    long inativa = funcao(sesmt, ctx.departmentId(), "Inativa", false);
    Contexto outra = contexto(sesmt, "Planta outra");

    IllegalArgumentException inativaErro =
        assertThrows(
            IllegalArgumentException.class,
            () -> estrutura.linkJobRoleToGhe(sesmt, gheId, inativa));
    IllegalArgumentException outraUnidade =
        assertThrows(
            IllegalArgumentException.class,
            () -> estrutura.linkJobRoleToGhe(sesmt, gheId, outra.jobRoleId()));
    IllegalArgumentException ocupada =
        assertThrows(
            IllegalArgumentException.class,
            () -> estrutura.linkJobRoleToGhe(sesmt, gheId, ctx.jobRoleId()));

    assertTrue(inativaErro.getMessage().startsWith("CAD-054"));
    assertTrue(outraUnidade.getMessage().startsWith("CAD-054"));
    assertTrue(ocupada.getMessage().startsWith("CAD-055"));
    assertEquals(outroGrupo, grupoDa(ctx.jobRoleId()));
  }

  @Test
  void shouldUnlinkAndRestoreTheFunctionProfile() {
    long sesmt = createUserWithRole("sesmt.ghe.desvinculo", Papel.SESMT);
    Contexto ctx = contexto(sesmt, "Planta sai");
    long gheId = estrutura.createGhe(sesmt, ctx.unitId(), "Ruido", true);
    estrutura.linkJobRoleToGhe(sesmt, gheId, ctx.jobRoleId());

    estrutura.unlinkJobRoleFromGhe(sesmt, gheId, ctx.jobRoleId());

    assertEquals(0, vinculos(ctx.jobRoleId()));
    assertEquals(PerfilVigente.Tipo.FUNCAO, estrutura.perfilVigente(ctx.jobRoleId()).tipo());
    assertEquals(ctx.jobRoleId(), estrutura.perfilVigente(ctx.jobRoleId()).id());
    assertEquals(1, auditoria(gheId, "GHE_FUNCAO_DESVINCULADA"));
  }

  @Test
  void shouldKeepLinksWhenInactiveAndRestoreTheGroupProfile() {
    long sesmt = createUserWithRole("sesmt.ghe.status", Papel.SESMT);
    Contexto ctx = contexto(sesmt, "Planta status");
    long gheId = estrutura.createGhe(sesmt, ctx.unitId(), "Ruido", true);
    estrutura.linkJobRoleToGhe(sesmt, gheId, ctx.jobRoleId());

    estrutura.setGheStatus(sesmt, gheId, false);
    assertEquals(1, vinculos(ctx.jobRoleId()));
    assertEquals(gheId, idDe(gheId));
    assertEquals(PerfilVigente.Tipo.FUNCAO, estrutura.perfilVigente(ctx.jobRoleId()).tipo());

    estrutura.setGheStatus(sesmt, gheId, true);
    PerfilVigente vigente = estrutura.perfilVigente(ctx.jobRoleId());
    assertEquals(PerfilVigente.Tipo.GHE, vigente.tipo());
    assertEquals(gheId, vigente.id());
    assertEquals(1, auditoria(gheId, "GHE_INATIVADO"));
    assertEquals(1, auditoria(gheId, "GHE_REATIVADO"));
  }

  @Test
  void shouldTreatActiveGroupWithoutEpiAsTheEffectiveProfile() {
    long sesmt = createUserWithRole("sesmt.ghe.vazio", Papel.SESMT);
    Contexto ctx = contexto(sesmt, "Planta vazia");
    long gheId = estrutura.createGhe(sesmt, ctx.unitId(), "Ruido", true);
    estrutura.linkJobRoleToGhe(sesmt, gheId, ctx.jobRoleId());

    PerfilVigente vigente = estrutura.perfilVigente(ctx.jobRoleId());

    assertEquals(PerfilVigente.Tipo.GHE, vigente.tipo());
    assertEquals(gheId, vigente.id());
    assertTrue(
        estrutura.listGheMembers(sesmt, gheId).stream()
            .map(FuncaoDoGhe::id)
            .toList()
            .contains(ctx.jobRoleId()));
  }

  @Test
  void shouldHideInactiveJobRoleFromNewLinksAndKeepTheOldOne() {
    long sesmt = createUserWithRole("sesmt.ghe.inativa", Papel.SESMT);
    Contexto ctx = contexto(sesmt, "Planta some");
    long gheId = estrutura.createGhe(sesmt, ctx.unitId(), "Ruido", true);
    estrutura.linkJobRoleToGhe(sesmt, gheId, ctx.jobRoleId());
    estrutura.setJobRoleStatus(sesmt, ctx.jobRoleId(), false);

    assertTrue(
        estrutura.listGheCandidates(sesmt, ctx.unitId()).stream()
            .noneMatch(funcao -> funcao.id().equals(ctx.jobRoleId())));
    assertEquals(1, vinculos(ctx.jobRoleId()));
  }

  @Test
  void shouldAllowAdminAndSesmtAndDenyTheOthers() {
    long admin = createUserWithRole("admin.ghe", Papel.ADMIN);
    long sesmt = createUserWithRole("sesmt.ghe.papel", Papel.SESMT);
    long almox = createUserWithRole("almox.ghe", Papel.ALMOXARIFE);
    long consulta = createUserWithRole("consulta.ghe", Papel.CONSULTA);
    long unitId = unidade(admin, "Planta papel");

    long doAdmin = estrutura.createGhe(admin, unitId, "Do admin", true);
    estrutura.updateGhe(sesmt, doAdmin, "Do sesmt");

    AuthorizationDeniedException almoxErro =
        assertThrows(
            AuthorizationDeniedException.class,
            () -> estrutura.createGhe(almox, unitId, "Negado", true));
    AuthorizationDeniedException consultaErro =
        assertThrows(
            AuthorizationDeniedException.class, () -> estrutura.listGhes(consulta, unitId, null));

    assertEquals("Do sesmt", nome(doAdmin));
    assertEquals(1, auditoria(doAdmin, "GHE_EDITADO"));
    assertTrue(almoxErro.getMessage().startsWith("AUTH-004"));
    assertTrue(consultaErro.getMessage().startsWith("AUTH-004"));
  }

  @Test
  void shouldRejectMissingGhe() {
    long sesmt = createUserWithRole("sesmt.ghe.ausente", Papel.SESMT);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> estrutura.updateGhe(sesmt, 999_999L, "Ninguem"));

    assertTrue(ex.getMessage().startsWith("CAD-053"));
  }

  private Contexto contexto(long actorId, String nomeUnidade) {
    long unitId = unidade(actorId, nomeUnidade);
    long departmentId = estrutura.createDepartment(actorId, "Operacao " + unitId, unitId, true);
    long jobRoleId = funcao(actorId, departmentId, "Operador " + unitId, true);
    return new Contexto(unitId, departmentId, jobRoleId);
  }

  private long unidade(long actorId, String nome) {
    return estrutura.createUnit(actorId, nome + " " + System.nanoTime(), proximoCnpj(), true);
  }

  private long funcao(long actorId, long departmentId, String nome, boolean active) {
    return estrutura.createJobRole(actorId, nome + " " + System.nanoTime(), departmentId, active);
  }

  private String nome(long gheId) {
    return jdbcTemplate.queryForObject("SELECT name FROM ghe WHERE id = ?", String.class, gheId);
  }

  private long idDe(long gheId) {
    Long id = jdbcTemplate.queryForObject("SELECT id FROM ghe WHERE id = ?", Long.class, gheId);
    return id == null ? -1 : id;
  }

  private int countGhe() {
    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM ghe", Integer.class);
    return count == null ? 0 : count;
  }

  private int vinculos(long jobRoleId) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM ghe_job_role WHERE job_role_id = ?", Integer.class, jobRoleId);
    return count == null ? 0 : count;
  }

  private long grupoDa(long jobRoleId) {
    Long id =
        jdbcTemplate.queryForObject(
            "SELECT ghe_id FROM ghe_job_role WHERE job_role_id = ?", Long.class, jobRoleId);
    return id == null ? -1 : id;
  }

  private int auditoria(long gheId, String acao) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = ? AND entidade = 'GHE' AND entidade_id = ? AND resultado = 'SUCESSO'
            """,
            Integer.class,
            acao,
            String.valueOf(gheId));
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

  private record Contexto(long unitId, long departmentId, long jobRoleId) {}
}
