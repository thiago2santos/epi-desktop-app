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
      "spring.datasource.url=jdbc:sqlite:file:employee-mem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class EmployeeManagementServiceIntegrationTest {

  @Autowired private EmployeeManagementService employeeManagementService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void shouldCreateEmployeeWhenActorIsAdmin() {
    long adminId = createUserWithRole("admin.employee", Papel.ADMIN);
    long departmentId = findDepartmentIdByName("Operacao");
    long jobRoleId = findJobRoleIdByNameAndDepartment("Almoxarife", departmentId);

    long employeeId =
        employeeManagementService.createEmployee(
            adminId, "MTR-1001", "Thiago Teste", departmentId, jobRoleId, true);

    Integer employeeCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM employee WHERE id = ?", Integer.class, employeeId);
    Integer auditCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM auditoria WHERE acao = 'EMPLOYEE_CREATED' AND entidade_id = ?",
            Integer.class,
            String.valueOf(employeeId));
    assertEquals(1, employeeCount);
    assertEquals(1, auditCount);
  }

  @Test
  void shouldAllowSesmtToCreateEmployee() {
    long sesmtId = createUserWithRole("sesmt.employee", Papel.SESMT);
    long departmentId = findDepartmentIdByName("SESMT");
    long jobRoleId = findJobRoleIdByNameAndDepartment("Tecnico de Seguranca", departmentId);

    employeeManagementService.createEmployee(
        sesmtId, "MTR-2002", "Colaborador Sesmt", departmentId, jobRoleId, true);

    Integer employeeCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM employee WHERE employee_code = ?", Integer.class, "MTR-2002");
    assertEquals(1, employeeCount);
  }

  @Test
  void shouldRejectCreateWhenActorHasNoPermission() {
    long consultaId = createUserWithRole("consulta.employee", Papel.CONSULTA);
    long departmentId = findDepartmentIdByName("Operacao");
    long jobRoleId = findJobRoleIdByNameAndDepartment("Almoxarife", departmentId);

    assertThrows(
        AuthorizationDeniedException.class,
        () ->
            employeeManagementService.createEmployee(
                consultaId, "MTR-3003", "Sem permissao", departmentId, jobRoleId, true));
  }

  @Test
  void shouldRejectWhenJobRoleAndDepartmentMismatch() {
    long adminId = createUserWithRole("admin.employee.mismatch", Papel.ADMIN);
    long departmentOperacaoId = findDepartmentIdByName("Operacao");
    long departmentSesmtId = findDepartmentIdByName("SESMT");
    long sesmtRoleId = findJobRoleIdByNameAndDepartment("Tecnico de Seguranca", departmentSesmtId);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                employeeManagementService.createEmployee(
                    adminId, "MTR-4004", "Mismatch", departmentOperacaoId, sesmtRoleId, true));
    assertEquals("EMP-003 Job role does not belong to selected department.", ex.getMessage());
  }

  @Test
  void shouldUpdateAndDeactivateEmployee() {
    long adminId = createUserWithRole("admin.employee.update", Papel.ADMIN);
    long depOperacao = findDepartmentIdByName("Operacao");
    long roleAlmox = findJobRoleIdByNameAndDepartment("Almoxarife", depOperacao);
    long depSesmt = findDepartmentIdByName("SESMT");
    long roleSesmt = findJobRoleIdByNameAndDepartment("Tecnico de Seguranca", depSesmt);

    long employeeId =
        employeeManagementService.createEmployee(
            adminId, "MTR-5005", "Nome Inicial", depOperacao, roleAlmox, true);

    employeeManagementService.updateEmployee(
        adminId, employeeId, "Nome Atualizado", depSesmt, roleSesmt, true);
    employeeManagementService.setEmployeeStatus(adminId, employeeId, false);

    String updatedName =
        jdbcTemplate.queryForObject(
            "SELECT full_name FROM employee WHERE id = ?", String.class, employeeId);
    Integer active =
        jdbcTemplate.queryForObject(
            "SELECT active FROM employee WHERE id = ?", Integer.class, employeeId);
    Integer updateAudit =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM auditoria WHERE acao = 'EMPLOYEE_UPDATED' AND entidade_id = ?",
            Integer.class,
            String.valueOf(employeeId));
    Integer deactivateAudit =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM auditoria WHERE acao = 'EMPLOYEE_DEACTIVATED' AND entidade_id = ?",
            Integer.class,
            String.valueOf(employeeId));

    assertEquals("Nome Atualizado", updatedName);
    assertEquals(0, active);
    assertEquals(1, updateAudit);
    assertEquals(1, deactivateAudit);
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
