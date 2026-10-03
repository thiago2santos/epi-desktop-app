package br.com.easynr6.gestaoepi.modules.employee.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:sqlite:file:org-structure-mem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class OrgStructureManagementServiceIntegrationTest {

  @Autowired private EmployeeManagementService employeeManagementService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void shouldCreateAndUpdateDepartmentWithAudit() {
    long adminId = createUserWithRole("admin.department", Papel.ADMIN);

    long departmentId =
        employeeManagementService.createDepartment(adminId, " Qualidade ", itupevaId(), true);
    employeeManagementService.updateDepartment(adminId, departmentId, "Qualidade e SGI", true);

    String storedName =
        jdbcTemplate.queryForObject(
            "SELECT name FROM department WHERE id = ?", String.class, departmentId);
    Integer auditCreate =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM auditoria WHERE acao = 'DEPARTMENT_CREATED' AND entidade_id = ?",
            Integer.class,
            String.valueOf(departmentId));
    Integer auditUpdate =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM auditoria WHERE acao = 'DEPARTMENT_UPDATED' AND entidade_id = ?",
            Integer.class,
            String.valueOf(departmentId));

    assertEquals("Qualidade e SGI", storedName);
    assertEquals(1, auditCreate);
    assertEquals(1, auditUpdate);
  }

  @Test
  void shouldRejectDuplicateDepartmentName() {
    long adminId = createUserWithRole("admin.department.duplicate", Papel.ADMIN);
    employeeManagementService.createDepartment(adminId, "Logistica", itupevaId(), true);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                employeeManagementService.createDepartment(
                    adminId, " logistica ", itupevaId(), true));
    assertEquals("CAD-021 Nome de setor ja existente.", ex.getMessage());
    Integer falhas =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE usuario_id = ? AND acao = 'DEPARTMENT_CREATED'
              AND resultado = 'FALHA' AND codigo = 'CAD-021'
              AND correlacao IS NOT NULL AND correlacao <> ''
            """,
            Integer.class,
            adminId);
    assertEquals(1, falhas);
  }

  @Test
  void shouldBlockDepartmentDeactivateWhenHasActiveJobRole() {
    long adminId = createUserWithRole("admin.department.block", Papel.ADMIN);
    long departmentId =
        employeeManagementService.createDepartment(adminId, "Manutencao", itupevaId(), true);
    employeeManagementService.createJobRole(adminId, "Mecanico", departmentId, true);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> employeeManagementService.setDepartmentStatus(adminId, departmentId, false));
    assertEquals("CAD-024 Operacao nao permitida por dependencia historica.", ex.getMessage());
  }

  @Test
  void shouldCreateAndUpdateJobRoleWithAudit() {
    long sesmtId = createUserWithRole("sesmt.jobrole", Papel.SESMT);
    long departmentId = findDepartmentIdByName("Operacao");

    long jobRoleId =
        employeeManagementService.createJobRole(sesmtId, "Separador", departmentId, true);
    employeeManagementService.updateJobRole(
        sesmtId, jobRoleId, "Separador Senior", departmentId, true);

    String storedName =
        jdbcTemplate.queryForObject(
            "SELECT name FROM job_role WHERE id = ?", String.class, jobRoleId);
    Integer auditCreate =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM auditoria WHERE acao = 'JOB_ROLE_CREATED' AND entidade_id = ?",
            Integer.class,
            String.valueOf(jobRoleId));
    Integer auditUpdate =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM auditoria WHERE acao = 'JOB_ROLE_UPDATED' AND entidade_id = ?",
            Integer.class,
            String.valueOf(jobRoleId));

    assertEquals("Separador Senior", storedName);
    assertEquals(1, auditCreate);
    assertEquals(1, auditUpdate);
  }

  @Test
  void shouldRejectJobRoleWhenDepartmentInactive() {
    long adminId = createUserWithRole("admin.jobrole.department.inactive", Papel.ADMIN);
    long departmentId =
        employeeManagementService.createDepartment(adminId, "Temporario", itupevaId(), false);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                employeeManagementService.createJobRole(
                    adminId, "Temporario I", departmentId, true));
    assertEquals("CAD-026 Setor invalido ou inativo.", ex.getMessage());
  }

  @Test
  void shouldRejectDuplicateJobRoleInSameDepartment() {
    long adminId = createUserWithRole("admin.jobrole.duplicate", Papel.ADMIN);
    long departmentId = findDepartmentIdByName("SESMT");
    employeeManagementService.createJobRole(adminId, "Assistente SESMT", departmentId, true);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                employeeManagementService.createJobRole(
                    adminId, " assistente sesmt ", departmentId, true));
    assertEquals("CAD-025 Nome de funcao ja existente no setor.", ex.getMessage());
  }

  @Test
  void shouldBlockJobRoleDeactivateWhenHasActiveEmployee() {
    long adminId = createUserWithRole("admin.jobrole.block", Papel.ADMIN);
    long departmentId = findDepartmentIdByName("Operacao");
    long jobRoleId = findJobRoleIdByNameAndDepartment("Almoxarife", departmentId);

    employeeManagementService.createEmployee(
        adminId, "MTR-BLOCK-1", "Empregado Vinculado", departmentId, jobRoleId, null, true);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> employeeManagementService.setJobRoleStatus(adminId, jobRoleId, false));
    assertEquals("CAD-028 Operacao nao permitida por dependencia historica.", ex.getMessage());
  }

  @Test
  void shouldRejectOrgStructureManagementForUnauthorizedRole() {
    long consultaId = createUserWithRole("consulta.org.structure", Papel.CONSULTA);

    assertThrows(
        AuthorizationDeniedException.class,
        () ->
            employeeManagementService.createDepartment(
                consultaId, "Sem Permissao", itupevaId(), true));
    Integer falhas =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE usuario_id = ? AND acao = 'DEPARTMENT_CREATED'
              AND resultado = 'FALHA' AND codigo = 'AUTH-004'
            """,
            Integer.class,
            consultaId);
    assertEquals(1, falhas);
  }

  @Test
  void shouldAllowSameDepartmentNameInAnotherUnit() {
    long adminId = createUserWithRole("admin.department.unit", Papel.ADMIN);
    long curitibaId = findUnitIdByCnpj("22755266000420");

    long itupevaDepartmentId =
        employeeManagementService.createDepartment(adminId, "Expedicao", itupevaId(), true);
    long curitibaDepartmentId =
        employeeManagementService.createDepartment(adminId, "Expedicao", curitibaId, true);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                employeeManagementService.createDepartment(adminId, "expedicao", curitibaId, true));
    assertEquals("CAD-021 Nome de setor ja existente.", ex.getMessage());

    Long itupevaUnit =
        jdbcTemplate.queryForObject(
            "SELECT unit_id FROM department WHERE id = ?", Long.class, itupevaDepartmentId);
    Long curitibaUnit =
        jdbcTemplate.queryForObject(
            "SELECT unit_id FROM department WHERE id = ?", Long.class, curitibaDepartmentId);
    assertEquals(itupevaId(), itupevaUnit);
    assertEquals(curitibaId, curitibaUnit);
  }

  @Test
  void shouldRejectDepartmentWhenUnitIsInactive() {
    long adminId = createUserWithRole("admin.department.unit.inactive", Papel.ADMIN);
    long lagoaSantaId = findUnitIdByCnpj("22755266000691");
    jdbcTemplate.update("UPDATE unit SET active = 0 WHERE id = ?", lagoaSantaId);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                employeeManagementService.createDepartment(adminId, "Arquivo", lagoaSantaId, true));
    assertEquals("CAD-027 Unidade invalida ou inativa.", ex.getMessage());
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

  private long itupevaId() {
    return findUnitIdByCnpj("22755266000268");
  }

  private long findUnitIdByCnpj(String cnpj) {
    Long id = jdbcTemplate.queryForObject("SELECT id FROM unit WHERE cnpj = ?", Long.class, cnpj);
    return id == null ? -1L : id;
  }

  private long findDepartmentIdByName(String name) {
    Long id =
        jdbcTemplate.queryForObject("SELECT id FROM department WHERE name = ?", Long.class, name);
    return id == null ? -1L : id;
  }

  private long findJobRoleIdByNameAndDepartment(String roleName, long departmentId) {
    Long id =
        jdbcTemplate.queryForObject(
            "SELECT id FROM job_role WHERE name = ? AND department_id = ?",
            Long.class,
            roleName,
            departmentId);
    return id == null ? -1L : id;
  }
}
