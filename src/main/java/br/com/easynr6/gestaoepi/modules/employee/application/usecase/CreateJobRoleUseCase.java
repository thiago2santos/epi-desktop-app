package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.employee.domain.JobRolePolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateJobRoleUseCase {

  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final JobRolePolicy jobRolePolicy = new JobRolePolicy();

  public CreateJobRoleUseCase(
      OrgStructureRepository orgStructureRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.orgStructureRepository = orgStructureRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "JOB_ROLE_CREATED", entidade = "JOB_ROLE")
  @Transactional
  public Long execute(Long actorId, String name, Long departmentId, boolean active) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    jobRolePolicy.validateRequiredFields(name, departmentId);
    DepartmentOption department =
        orgStructureRepository.findDepartmentById(departmentId).orElse(null);
    jobRolePolicy.validateActiveDepartment(department);
    String normalizedName = jobRolePolicy.normalizeName(name);
    if (orgStructureRepository.existsJobRoleByNameInDepartment(normalizedName, departmentId)) {
      throw new IllegalArgumentException("CAD-025 Nome de funcao ja existente no setor.");
    }
    Long jobRoleId = orgStructureRepository.createJobRole(normalizedName, departmentId, active);
    auditTrail.registrarEventoCritico(
        actorId,
        "JOB_ROLE_CREATED",
        "JOB_ROLE",
        String.valueOf(jobRoleId),
        "Job role created: " + normalizedName);
    return jobRoleId;
  }
}
