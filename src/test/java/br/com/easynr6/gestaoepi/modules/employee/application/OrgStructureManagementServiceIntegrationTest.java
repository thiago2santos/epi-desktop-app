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

    long departmentId = employeeManagementService.createDepartment(adminId, " Qualidade ", true);
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
    employeeManagementService.createDepartment(adminId, "Logistica", true);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> employeeManagementService.createDepartment(adminId, " logistica ", true));
    assertEquals("CAD-021 Nome de setor ja existente.", ex.getMessage());
  }

  @Test
  void shouldBlockDepartmentDeactivateWhenHasActiveJobRole() {
    long adminId = createUserWithRole("admin.department.block", Papel.ADMIN);
    long departmentId = employeeManagementService.createDepartment(adminId, "Manutencao", true);
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
    long departmentId = employeeManagementService.createDepartment(adminId, "Temporario", false);

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
        adminId, "MTR-BLOCK-1", "Empregado Vinculado", departmentId, jobRoleId, true);

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
        () -> employeeManagementService.createDepartment(consultaId, "Sem Permissao", true));
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
