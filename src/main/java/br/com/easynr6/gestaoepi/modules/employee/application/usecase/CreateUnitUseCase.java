package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.domain.UnitPolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUnitUseCase {

  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final UnitPolicy unitPolicy = new UnitPolicy();

  public CreateUnitUseCase(
      OrgStructureRepository orgStructureRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.orgStructureRepository = orgStructureRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "UNIDADE_CRIADA", entidade = "UNIDADE")
  @Transactional
  public Long execute(Long actorId, String name, String cnpj, boolean active) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    String normalizedName = unitPolicy.requireName(name);
    String digits = unitPolicy.requireCnpj(cnpj);
    if (orgStructureRepository.existsUnitByCnpj(digits)) {
      throw new IllegalArgumentException("CAD-043 CNPJ ja cadastrado.");
    }
    Long unitId = orgStructureRepository.createUnit(normalizedName, digits, active);
    auditTrail.registrarEventoCritico(
        actorId,
        "UNIDADE_CRIADA",
        "UNIDADE",
        String.valueOf(unitId),
        "Unidade criada: " + normalizedName);
    return unitId;
  }
}
