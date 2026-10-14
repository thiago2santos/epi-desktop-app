package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeAssignment;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.JobRoleOption;
import br.com.easynr6.gestaoepi.modules.employee.domain.EmployeePolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEmployeeUseCase {

  private final EmployeeRepository employeeRepository;
  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final SenhaRecebimentoGravacao senhaRecebimentoGravacao;
  private final EmployeePolicy employeePolicy = new EmployeePolicy();

  public UpdateEmployeeUseCase(
      EmployeeRepository employeeRepository,
      OrgStructureRepository orgStructureRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail,
      SenhaRecebimentoGravacao senhaRecebimentoGravacao) {
    this.employeeRepository = employeeRepository;
    this.orgStructureRepository = orgStructureRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
    this.senhaRecebimentoGravacao = senhaRecebimentoGravacao;
  }

  @AcaoAuditada(acao = "EMPLOYEE_UPDATED", entidade = "EMPLOYEE", alvo = 1)
  @Transactional
  public void execute(
      Long actorId,
      Long employeeId,
      String fullName,
      Long departmentId,
      Long jobRoleId,
      Long managerId,
      boolean active,
      String senhaRecebimento) {
    String senha = CreateEmployeeUseCase.senhaInformada(senhaRecebimento);
    if (senha != null) {
      accessAuthorizer.assertCanDefineReceiptPassword(actorId);
    }
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (employeeId == null || !employeeRepository.existsById(employeeId)) {
      throw new IllegalArgumentException("CAD-006 Trabalhador alvo nao encontrado.");
    }
    employeePolicy.validateUpdateFields(fullName, departmentId, jobRoleId);

    DepartmentOption department =
        orgStructureRepository.findDepartmentById(departmentId).orElse(null);
    JobRoleOption jobRole = orgStructureRepository.findJobRoleById(jobRoleId).orElse(null);
    employeePolicy.validateDepartmentAndRole(department, jobRole);
    EmployeeAssignment manager =
        managerId == null ? null : employeeRepository.findAssignmentById(managerId).orElse(null);
    employeePolicy.validateManager(
        employeeId, managerId, department == null ? null : department.unitId(), manager);

    String matricula =
        employeeRepository
            .findCodeById(employeeId)
            .orElseThrow(
                () -> new IllegalArgumentException("CAD-006 Trabalhador alvo nao encontrado."));
    if (senha != null) {
      employeePolicy.receiptPassword(senha, matricula);
    }
    employeeRepository.update(
        employeeId,
        employeePolicy.normalizeName(fullName),
        departmentId,
        jobRoleId,
        managerId,
        active);
    if (senha != null) {
      senhaRecebimentoGravacao.gravar(actorId, employeeId, matricula, senha);
    }
    auditTrail.registrarEventoCritico(
        actorId, "EMPLOYEE_UPDATED", "EMPLOYEE", String.valueOf(employeeId), "Employee updated");
  }

  @AcaoAuditada(acao = "SENHA_RECEBIMENTO_DEFINIDA", entidade = "EMPLOYEE", alvo = 1)
  @Transactional
  public void definirSenha(Long actorId, Long employeeId, String senhaRecebimento) {
    accessAuthorizer.assertCanDefineReceiptPassword(actorId);
    if (employeeId == null || !employeeRepository.existsById(employeeId)) {
      throw new IllegalArgumentException("CAD-006 Trabalhador alvo nao encontrado.");
    }
    String matricula =
        employeeRepository
            .findCodeById(employeeId)
            .orElseThrow(
                () -> new IllegalArgumentException("CAD-006 Trabalhador alvo nao encontrado."));
    senhaRecebimentoGravacao.gravar(actorId, employeeId, matricula, senhaRecebimento);
  }
}
