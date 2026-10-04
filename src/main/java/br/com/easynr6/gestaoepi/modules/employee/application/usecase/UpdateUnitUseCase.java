package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.UnitSummary;
import br.com.easynr6.gestaoepi.modules.employee.domain.UnitPolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUnitUseCase {

  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final UnitPolicy unitPolicy = new UnitPolicy();

  public UpdateUnitUseCase(
      OrgStructureRepository orgStructureRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.orgStructureRepository = orgStructureRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "UNIDADE_EDITADA", entidade = "UNIDADE", alvo = 1)
  @Transactional
  public void execute(Long actorId, Long unitId, String name) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    UnitSummary current =
        unitId == null ? null : orgStructureRepository.findUnitById(unitId).orElse(null);
    if (current == null) {
      throw new IllegalArgumentException("CAD-044 Unidade alvo nao encontrada.");
    }
    String normalizedName = unitPolicy.requireName(name);
    orgStructureRepository.updateUnitName(unitId, normalizedName);
    auditTrail.registrarEventoCritico(
        actorId,
        "UNIDADE_EDITADA",
        "UNIDADE",
        String.valueOf(unitId),
        "Unidade editada: " + normalizedName);
  }
}
