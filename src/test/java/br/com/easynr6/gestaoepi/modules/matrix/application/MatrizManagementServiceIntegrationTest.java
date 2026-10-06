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
import br.com.easynr6.gestaoepi.modules.matrix.domain.ModoMatriz;
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
      "spring.datasource.url=jdbc:sqlite:file:matriz-mem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class MatrizManagementServiceIntegrationTest {

  private static final AtomicInteger FILIAL = new AtomicInteger(40);
  private static final AtomicInteger CA = new AtomicInteger(520000);
  private static final int[] PESOS_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] PESOS_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

  @Autowired private MatrizManagementService matriz;
  @Autowired private EmployeeManagementService estrutura;
  @Autowired private EpiCatalogManagementService catalogo;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void shouldIncludeActiveEpiWithIndividualModeAndAudit() {
    long sesmt = createUserWithRole("sesmt.matriz", Papel.SESMT);
    long funcao = funcaoAtiva(sesmt, "Planta matriz");
    long epiId = epiComCa(sesmt, "Luva matriz");

    long linhaId = matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, funcao, epiId);

    assertEquals("INDIVIDUAL", modo(linhaId));
    assertEquals(0, treinamento(linhaId));
    assertEquals(1, ativas(funcao, epiId));
    assertEquals(1, auditoria(linhaId, "MATRIZ_INCLUIDA"));
    assertEquals(1, matriz.linhasVigentes(funcao).size());
  }

  @Test
  void shouldRejectASecondActiveLine() {
    long sesmt = createUserWithRole("sesmt.matriz.dup", Papel.SESMT);
    long funcao = funcaoAtiva(sesmt, "Planta dup");
    long epiId = epiComCa(sesmt, "Capacete dup");
    matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, funcao, epiId);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, funcao, epiId));

    assertTrue(ex.getMessage().startsWith("MAT-002"));
    assertEquals(1, ativas(funcao, epiId));
  }

  @Test
  void shouldRejectEpiWithoutActiveCa() {
    long sesmt = createUserWithRole("sesmt.matriz.semca", Papel.SESMT);
    long funcao = funcaoAtiva(sesmt, "Planta sem ca");
    long epiId =
        catalogo.createEpi(sesmt, "SEM", "Sem CA " + System.nanoTime(), AnnexGroup.B, true);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, funcao, epiId));

    assertTrue(ex.getMessage().startsWith("MAT-003"));
    assertEquals(0, ativas(funcao, epiId));
  }

  @Test
  void shouldRejectInactiveProfileOrEpi() {
    long sesmt = createUserWithRole("sesmt.matriz.inativo", Papel.SESMT);
    long unitId = unidade(sesmt, "Planta inativa");
    long departmentId = estrutura.createDepartment(sesmt, "Setor " + unitId, unitId, true);
    long inativa =
        estrutura.createJobRole(sesmt, "Inativa " + System.nanoTime(), departmentId, false);
    long epiId = epiComCa(sesmt, "Bota inativa");
    long epiInativo = epiComCa(sesmt, "Creme inativo");
    catalogo.setEpiStatus(sesmt, epiInativo, false);
    long funcao = estrutura.createJobRole(sesmt, "Ativa " + System.nanoTime(), departmentId, true);

    IllegalArgumentException funcaoErro =
        assertThrows(
            IllegalArgumentException.class,
            () -> matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, inativa, epiId));
    IllegalArgumentException epiErro =
        assertThrows(
            IllegalArgumentException.class,
            () -> matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, funcao, epiInativo));
    IllegalArgumentException ausente =
        assertThrows(
            IllegalArgumentException.class, () -> matriz.incluir(sesmt, null, funcao, epiId));

    assertTrue(funcaoErro.getMessage().startsWith("MAT-004"));
    assertTrue(epiErro.getMessage().startsWith("MAT-004"));
    assertTrue(ausente.getMessage().startsWith("MAT-001"));
  }

  @Test
  void shouldChangeModeAndTrainingOnTheSameLine() {
    long sesmt = createUserWithRole("sesmt.matriz.modo", Papel.SESMT);
    long funcao = funcaoAtiva(sesmt, "Planta modo");
    long epiId = epiComCa(sesmt, "Creme posto");
    long linhaId = matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, funcao, epiId);

    matriz.alterar(sesmt, linhaId, ModoMatriz.POSTO, true);

    assertEquals(linhaId, idAtivo(funcao, epiId));
    assertEquals("POSTO", modo(linhaId));
    assertEquals(1, treinamento(linhaId));
    assertEquals(1, auditoria(linhaId, "MATRIZ_ALTERADA"));
  }

  @Test
  void shouldInactivateAndAllowANewLine() {
    long sesmt = createUserWithRole("sesmt.matriz.sai", Papel.SESMT);
    long funcao = funcaoAtiva(sesmt, "Planta sai");
    long epiId = epiComCa(sesmt, "Oculos sai");
    long antiga = matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, funcao, epiId);

    matriz.inativar(sesmt, antiga);
    long nova = matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, funcao, epiId);

    assertEquals(0, ativo(antiga));
    assertEquals(1, ativo(nova));
    assertEquals(1, auditoria(antiga, "MATRIZ_INATIVADA"));
    IllegalArgumentException deNovo =
        assertThrows(IllegalArgumentException.class, () -> matriz.inativar(sesmt, antiga));
    assertTrue(deNovo.getMessage().startsWith("MAT-007"));
  }

  @Test
  void shouldDenyAlmoxarifeAndConsulta() {
    long sesmt = createUserWithRole("sesmt.matriz.papel", Papel.SESMT);
    long almox = createUserWithRole("almox.matriz", Papel.ALMOXARIFE);
    long consulta = createUserWithRole("consulta.matriz", Papel.CONSULTA);
    long funcao = funcaoAtiva(sesmt, "Planta papel");
    long epiId = epiComCa(sesmt, "Avental papel");

    AuthorizationDeniedException mutacao =
        assertThrows(
            AuthorizationDeniedException.class,
            () -> matriz.incluir(almox, PerfilVigente.Tipo.FUNCAO, funcao, epiId));
    AuthorizationDeniedException leitura =
        assertThrows(AuthorizationDeniedException.class, () -> matriz.listarPerfis(consulta));

    assertTrue(mutacao.getMessage().startsWith("AUTH-004"));
    assertTrue(leitura.getMessage().startsWith("AUTH-004"));
  }

  @Test
  void shouldUseOnlyTheActiveGroupAndKeepTheFunctionLine() {
    long sesmt = createUserWithRole("sesmt.matriz.ghe", Papel.SESMT);
    long unitId = unidade(sesmt, "Planta ghe");
    long departmentId = estrutura.createDepartment(sesmt, "Setor " + unitId, unitId, true);
    long funcao =
        estrutura.createJobRole(sesmt, "Operador " + System.nanoTime(), departmentId, true);
    long epiFuncao = epiComCa(sesmt, "Luva da funcao");
    long epiGrupo = epiComCa(sesmt, "Luva do grupo");
    matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, funcao, epiFuncao);
    long gheId = estrutura.createGhe(sesmt, unitId, "Ruido " + unitId, true);
    estrutura.linkJobRoleToGhe(sesmt, gheId, funcao);

    assertTrue(matriz.linhasVigentes(funcao).isEmpty());
    IllegalArgumentException propria =
        assertThrows(
            IllegalArgumentException.class,
            () -> matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, funcao, epiGrupo));
    assertTrue(propria.getMessage().startsWith("MAT-008"));
    assertTrue(
        matriz.listarPerfis(sesmt).stream()
            .noneMatch(
                item -> item.tipo() == PerfilVigente.Tipo.FUNCAO && item.id().equals(funcao)));
    assertTrue(
        matriz.listarPerfis(sesmt).stream()
            .anyMatch(item -> item.tipo() == PerfilVigente.Tipo.GHE && item.id().equals(gheId)));

    matriz.incluir(sesmt, PerfilVigente.Tipo.GHE, gheId, epiGrupo);
    assertEquals(epiGrupo, matriz.linhasVigentes(funcao).getFirst().epiId());

    estrutura.setGheStatus(sesmt, gheId, false);
    assertEquals(epiFuncao, matriz.linhasVigentes(funcao).getFirst().epiId());
  }

  @Test
  void shouldRefreshTheExpectedCa() {
    long sesmt = createUserWithRole("sesmt.matriz.ca", Papel.SESMT);
    long funcao = funcaoAtiva(sesmt, "Planta ca");
    long epiId = epiComCa(sesmt, "Capacete ca");
    long linhaId = matriz.incluir(sesmt, PerfilVigente.Tipo.FUNCAO, funcao, epiId);
    String primeiro = ca(linhaId);
    String segundo = String.valueOf(CA.incrementAndGet());
    catalogo.bindCaToEpi(
        sesmt,
        epiId,
        segundo,
        CaStatus.ACTIVE,
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2028, 1, 1),
        LocalDateTime.of(2026, 10, 5, 9, 0),
        "Consulta oficial do CA novo",
        true,
        PrintConsulta.nome(),
        PrintConsulta.png());

    matriz.atualizarCa(sesmt, linhaId);

    assertEquals(segundo, ca(linhaId));
    assertTrue(!primeiro.equals(segundo));
    assertEquals(1, auditoria(linhaId, "MATRIZ_ALTERADA"));
  }

  private long funcaoAtiva(long actorId, String nomeUnidade) {
    long unitId = unidade(actorId, nomeUnidade);
    long departmentId = estrutura.createDepartment(actorId, "Operacao " + unitId, unitId, true);
    return estrutura.createJobRole(actorId, "Operador " + System.nanoTime(), departmentId, true);
  }

  private long unidade(long actorId, String nome) {
    return estrutura.createUnit(actorId, nome + " " + System.nanoTime(), proximoCnpj(), true);
  }

  private long epiComCa(long actorId, String descricao) {
    long epiId =
        catalogo.createEpi(actorId, "MAT", descricao + " " + System.nanoTime(), AnnexGroup.F, true);
    catalogo.bindCaToEpi(
        actorId,
        epiId,
        String.valueOf(CA.incrementAndGet()),
        CaStatus.ACTIVE,
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2028, 1, 1),
        LocalDateTime.of(2026, 10, 5, 8, 0),
        "Consulta oficial para a matriz",
        true,
        PrintConsulta.nome(),
        PrintConsulta.png());
    return epiId;
  }

  private String modo(long linhaId) {
    return jdbcTemplate.queryForObject(
        "SELECT modo FROM matriz_linha WHERE id = ?", String.class, linhaId);
  }

  private String ca(long linhaId) {
    return jdbcTemplate.queryForObject(
        """
        SELECT b.ca_number
        FROM matriz_linha m
        JOIN epi_ca_binding b ON b.id = m.ca_binding_id
        WHERE m.id = ?
        """,
        String.class,
        linhaId);
  }

  private int treinamento(long linhaId) {
    Integer valor =
        jdbcTemplate.queryForObject(
            "SELECT exige_treinamento FROM matriz_linha WHERE id = ?", Integer.class, linhaId);
    return valor == null ? -1 : valor;
  }

  private int ativo(long linhaId) {
    Integer valor =
        jdbcTemplate.queryForObject(
            "SELECT active FROM matriz_linha WHERE id = ?", Integer.class, linhaId);
    return valor == null ? -1 : valor;
  }

  private int ativas(long funcaoId, long epiId) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM matriz_linha
            WHERE perfil_tipo = 'FUNCAO' AND perfil_id = ? AND epi_id = ? AND active = 1
            """,
            Integer.class,
            funcaoId,
            epiId);
    return count == null ? 0 : count;
  }

  private long idAtivo(long funcaoId, long epiId) {
    Long id =
        jdbcTemplate.queryForObject(
            """
            SELECT id FROM matriz_linha
            WHERE perfil_tipo = 'FUNCAO' AND perfil_id = ? AND epi_id = ? AND active = 1
            """,
            Long.class,
            funcaoId,
            epiId);
    return id == null ? -1 : id;
  }

  private int auditoria(long linhaId, String acao) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = ? AND entidade = 'MATRIZ' AND entidade_id = ? AND resultado = 'SUCESSO'
            """,
            Integer.class,
            acao,
            String.valueOf(linhaId));
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
}
