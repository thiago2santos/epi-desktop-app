package br.com.easynr6.gestaoepi.modules.employee.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.UnitOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.UnitSummary;
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
      "spring.datasource.url=jdbc:sqlite:file:unit-mem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class UnitManagementServiceIntegrationTest {

  private static final AtomicInteger FILIAL = new AtomicInteger(19);
  private static final int[] PESOS_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] PESOS_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

  @Autowired private EmployeeManagementService employeeManagementService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void shouldCreateUnitForSeededCompanyWithAudit() {
    long sesmtId = createUserWithRole("sesmt.unidade", Papel.SESMT);

    String cnpj = proximoCnpj();
    long unitId =
        employeeManagementService.createUnit(
            sesmtId, " Lagoa Santa ", UnitPolicy.formatarCnpj(cnpj), true);

    String name =
        jdbcTemplate.queryForObject("SELECT name FROM unit WHERE id = ?", String.class, unitId);
    Long companyId =
        jdbcTemplate.queryForObject("SELECT company_id FROM unit WHERE id = ?", Long.class, unitId);
    Long seedCompany =
        jdbcTemplate.queryForObject(
            "SELECT id FROM company WHERE cnpj_root = '22755266'", Long.class);
    Integer auditoria =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE usuario_id = ? AND acao = 'UNIDADE_CRIADA' AND entidade = 'UNIDADE'
              AND entidade_id = ? AND resultado = 'SUCESSO'
            """,
            Integer.class,
            sesmtId,
            String.valueOf(unitId));

    assertEquals("Lagoa Santa", name);
    assertEquals(cnpj, cnpjOf(unitId));
    assertEquals(seedCompany, companyId);
    assertEquals(1, auditoria);
  }

  @Test
  void shouldRejectMissingNameOrCnpjWithoutInsert() {
    long sesmtId = createUserWithRole("sesmt.unidade.obrigatorio", Papel.SESMT);
    int antes = countUnits();

    IllegalArgumentException semNome =
        assertThrows(
            IllegalArgumentException.class,
            () -> employeeManagementService.createUnit(sesmtId, " ", proximoCnpj(), true));
    IllegalArgumentException semCnpj =
        assertThrows(
            IllegalArgumentException.class,
            () -> employeeManagementService.createUnit(sesmtId, "Planta Nova", " ", true));

    assertEquals("CAD-041 Nome ou CNPJ ausente.", semNome.getMessage());
    assertEquals("CAD-041 Nome ou CNPJ ausente.", semCnpj.getMessage());
    assertEquals(antes, countUnits());
  }

  @Test
  void shouldRejectInvalidCnpj() {
    long sesmtId = createUserWithRole("sesmt.unidade.invalido", Papel.SESMT);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> employeeManagementService.createUnit(sesmtId, "Planta", "22755266000180", true));

    assertEquals("CAD-042 CNPJ invalido.", ex.getMessage());
  }

  @Test
  void shouldRejectDuplicateCnpj() {
    long sesmtId = createUserWithRole("sesmt.unidade.duplicado", Papel.SESMT);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                employeeManagementService.createUnit(
                    sesmtId, "Outra Itupeva", "22.755.266/0002-68", true));

    assertEquals("CAD-043 CNPJ ja cadastrado.", ex.getMessage());
    Integer falhas =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE usuario_id = ? AND acao = 'UNIDADE_CRIADA'
              AND resultado = 'FALHA' AND codigo = 'CAD-043'
            """,
            Integer.class,
            sesmtId);
    assertEquals(1, falhas);
  }

  @Test
  void shouldAllowRepeatedNameWhenCnpjDiffers() {
    long sesmtId = createUserWithRole("sesmt.unidade.nome", Papel.SESMT);

    String cnpj = proximoCnpj();
    long unitId = employeeManagementService.createUnit(sesmtId, "Belo Horizonte", cnpj, true);

    Integer homonimas =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM unit WHERE name = 'Belo Horizonte'", Integer.class);
    assertTrue(homonimas >= 2);
    assertEquals(cnpj, cnpjOf(unitId));
  }

  @Test
  void shouldRenameUnitAndKeepCnpjVisibleToSectorCombo() {
    long sesmtId = createUserWithRole("sesmt.unidade.nome.edicao", Papel.SESMT);
    String cnpj = proximoCnpj();
    long unitId = employeeManagementService.createUnit(sesmtId, "Planta Norte", cnpj, true);
    employeeManagementService.createDepartment(sesmtId, "Expedicao", unitId, true);

    employeeManagementService.updateUnit(sesmtId, unitId, "Planta Norte Ampliada");

    assertEquals("Planta Norte Ampliada", nameOf(unitId));
    assertEquals(cnpj, cnpjOf(unitId));
    assertTrue(
        employeeManagementService.listActiveUnits().stream()
            .filter(item -> item.id().equals(unitId))
            .map(UnitOption::name)
            .anyMatch("Planta Norte Ampliada"::equals));
    assertTrue(
        employeeManagementService.listDepartments(sesmtId, "Expedicao").stream()
            .filter(item -> item.unitId().equals(unitId))
            .map(DepartmentSummary::unitName)
            .anyMatch("Planta Norte Ampliada"::equals));
    Integer auditoria =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = 'UNIDADE_EDITADA' AND entidade = 'UNIDADE'
              AND entidade_id = ? AND resultado = 'SUCESSO'
            """,
            Integer.class,
            String.valueOf(unitId));
    assertEquals(1, auditoria);
  }

  @Test
  void shouldRejectMissingUnitOnEditOrStatusChange() {
    long sesmtId = createUserWithRole("sesmt.unidade.ausente", Papel.SESMT);

    IllegalArgumentException edicao =
        assertThrows(
            IllegalArgumentException.class,
            () -> employeeManagementService.updateUnit(sesmtId, 999_999L, "Nenhuma"));
    IllegalArgumentException status =
        assertThrows(
            IllegalArgumentException.class,
            () -> employeeManagementService.setUnitStatus(sesmtId, 999_999L, false));

    assertEquals("CAD-044 Unidade alvo nao encontrada.", edicao.getMessage());
    assertEquals("CAD-044 Unidade alvo nao encontrada.", status.getMessage());
  }

  @Test
  void shouldDeactivateAndReactivateKeepingIdWhenNoActiveDepartment() {
    long sesmtId = createUserWithRole("sesmt.unidade.status", Papel.SESMT);
    long unitId = employeeManagementService.createUnit(sesmtId, "Planta Sul", proximoCnpj(), true);
    employeeManagementService.createDepartment(sesmtId, "Arquivo morto", unitId, false);

    employeeManagementService.setUnitStatus(sesmtId, unitId, false);
    employeeManagementService.setUnitStatus(sesmtId, unitId, true);

    assertEquals(1, activeOf(unitId));
    assertEquals(1, countUnit(unitId));
    assertEquals(1, auditCount(unitId, "UNIDADE_INATIVADA"));
    assertEquals(1, auditCount(unitId, "UNIDADE_REATIVADA"));
  }

  @Test
  void shouldBlockDeactivationWhenUnitHasActiveDepartment() {
    long sesmtId = createUserWithRole("sesmt.unidade.setor", Papel.SESMT);
    long unitId =
        employeeManagementService.createUnit(sesmtId, "Planta Oeste", proximoCnpj(), true);
    employeeManagementService.createDepartment(sesmtId, "Producao", unitId, true);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> employeeManagementService.setUnitStatus(sesmtId, unitId, false));

    assertEquals("CAD-045 Inativacao recusada: a unidade ainda tem setor ativo.", ex.getMessage());
    assertEquals(1, activeOf(unitId));
  }

  @Test
  void shouldHideInactiveUnitFromSectorCombo() {
    long sesmtId = createUserWithRole("sesmt.unidade.combo", Papel.SESMT);
    String cnpjInativa = proximoCnpj();
    long inativa =
        employeeManagementService.createUnit(sesmtId, "Planta Inativa", cnpjInativa, false);
    long ativa = employeeManagementService.createUnit(sesmtId, "Planta Ativa", proximoCnpj(), true);

    assertFalse(
        employeeManagementService.listActiveUnits().stream()
            .anyMatch(item -> item.id().equals(inativa)));
    assertTrue(
        employeeManagementService.listActiveUnits().stream()
            .anyMatch(item -> item.id().equals(ativa)));
    assertTrue(
        employeeManagementService.listUnits(sesmtId, cnpjInativa).stream()
            .map(UnitSummary::id)
            .anyMatch(id -> id.equals(inativa)));
  }

  @Test
  void shouldAllowAdminToMaintainUnit() {
    long adminId = createUserWithRole("admin.unidade", Papel.ADMIN);
    long unitId =
        employeeManagementService.createUnit(adminId, "Planta Admin", proximoCnpj(), true);

    employeeManagementService.updateUnit(adminId, unitId, "Planta Admin Revisada");
    employeeManagementService.setUnitStatus(adminId, unitId, false);
    employeeManagementService.setUnitStatus(adminId, unitId, true);

    assertEquals("Planta Admin Revisada", nameOf(unitId));
    assertEquals(1, activeOf(unitId));
    assertEquals(1, auditCount(unitId, "UNIDADE_CRIADA"));
    assertEquals(1, auditCount(unitId, "UNIDADE_EDITADA"));
    assertEquals(1, auditCount(unitId, "UNIDADE_INATIVADA"));
    assertEquals(1, auditCount(unitId, "UNIDADE_REATIVADA"));
  }

  @Test
  void shouldAllowSesmtToMaintainUnit() {
    long sesmtId = createUserWithRole("sesmt.unidade.ciclo", Papel.SESMT);
    long unitId =
        employeeManagementService.createUnit(sesmtId, "Planta Sesmt", proximoCnpj(), true);

    employeeManagementService.updateUnit(sesmtId, unitId, "Planta Sesmt Revisada");
    employeeManagementService.setUnitStatus(sesmtId, unitId, false);
    employeeManagementService.setUnitStatus(sesmtId, unitId, true);

    assertEquals(1, activeOf(unitId));
  }

  @Test
  void shouldDenyAlmoxarife() {
    long actorId = createUserWithRole("almox.unidade", Papel.ALMOXARIFE);

    AuthorizationDeniedException ex =
        assertThrows(
            AuthorizationDeniedException.class,
            () -> employeeManagementService.createUnit(actorId, "Planta", proximoCnpj(), true));

    assertEquals("AUTH-004 Voce nao tem permissao para executar esta acao.", ex.getMessage());
  }

  @Test
  void shouldDenyConsulta() {
    long actorId = createUserWithRole("consulta.unidade", Papel.CONSULTA);

    AuthorizationDeniedException ex =
        assertThrows(
            AuthorizationDeniedException.class,
            () -> employeeManagementService.updateUnit(actorId, 1L, "Outro nome"));

    assertEquals("AUTH-004 Voce nao tem permissao para executar esta acao.", ex.getMessage());
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

  private int countUnits() {
    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM unit", Integer.class);
    return count == null ? 0 : count;
  }

  private int countUnit(long unitId) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM unit WHERE id = ?", Integer.class, unitId);
    return count == null ? 0 : count;
  }

  private int activeOf(long unitId) {
    Integer active =
        jdbcTemplate.queryForObject("SELECT active FROM unit WHERE id = ?", Integer.class, unitId);
    return active == null ? -1 : active;
  }

  private String nameOf(long unitId) {
    return jdbcTemplate.queryForObject("SELECT name FROM unit WHERE id = ?", String.class, unitId);
  }

  private String cnpjOf(long unitId) {
    return jdbcTemplate.queryForObject("SELECT cnpj FROM unit WHERE id = ?", String.class, unitId);
  }

  private int auditCount(long unitId, String action) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = ? AND entidade = 'UNIDADE' AND entidade_id = ? AND resultado = 'SUCESSO'
            """,
            Integer.class,
            action,
            String.valueOf(unitId));
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
    if (userId == null) {
      return -1L;
    }
    jdbcTemplate.update(
        """
        INSERT INTO usuario_papel (usuario_id, papel_id)
        VALUES (?, (SELECT id FROM papel WHERE codigo = ?))
        """,
        userId,
        role.name());
    return userId;
  }
}
