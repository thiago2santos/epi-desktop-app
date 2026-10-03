package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.employee.domain.JobRolePolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateJobRoleUseCase {

  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final JobRolePolicy jobRolePolicy = new JobRolePolicy();

  public UpdateJobRoleUseCase(
      OrgStructureRepository orgStructureRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.orgStructureRepository = orgStructureRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "JOB_ROLE_UPDATED", entidade = "JOB_ROLE", alvo = 1)
  @Transactional
  public void execute(
      Long actorId, Long jobRoleId, String name, Long departmentId, boolean active) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (jobRoleId == null || !orgStructureRepository.jobRoleExists(jobRoleId)) {
      throw new IllegalArgumentException("CAD-027 Funcao alvo nao encontrada.");
    }
    jobRolePolicy.validateRequiredFields(name, departmentId);
    DepartmentOption department =
        orgStructureRepository.findDepartmentById(departmentId).orElse(null);
    jobRolePolicy.validateActiveDepartment(department);
    String normalizedName = jobRolePolicy.normalizeName(name);
    if (orgStructureRepository.existsJobRoleByNameInDepartmentExcludingId(
        normalizedName, departmentId, jobRoleId)) {
      throw new IllegalArgumentException("CAD-025 Nome de funcao ja existente no setor.");
    }
    if (!active && orgStructureRepository.hasActiveEmployeesByJobRole(jobRoleId)) {
      throw new IllegalArgumentException(
          "CAD-028 Operacao nao permitida por dependencia historica.");
    }
    orgStructureRepository.updateJobRole(jobRoleId, normalizedName, departmentId, active);
    auditTrail.registrarEventoCritico(
        actorId,
        "JOB_ROLE_UPDATED",
        "JOB_ROLE",
        String.valueOf(jobRoleId),
        "Job role updated: " + normalizedName);
  }
}
