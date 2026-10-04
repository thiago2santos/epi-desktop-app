package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository;
import br.com.easynr6.gestaoepi.modules.employee.domain.UnitPolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetUnitStatusUseCase {

  private final OrgStructureRepository orgStructureRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final UnitPolicy unitPolicy = new UnitPolicy();

  public SetUnitStatusUseCase(
      OrgStructureRepository orgStructureRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.orgStructureRepository = orgStructureRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(
      entidade = "UNIDADE",
      acaoQuandoAtivo = "UNIDADE_REATIVADA",
      acaoQuandoInativo = "UNIDADE_INATIVADA",
      alvo = 1)
  @Transactional
  public void execute(Long actorId, Long unitId, boolean active) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (unitId == null || orgStructureRepository.findUnitById(unitId).isEmpty()) {
      throw new IllegalArgumentException("CAD-044 Unidade alvo nao encontrada.");
    }
    if (!active) {
      unitPolicy.assertCanDeactivate(orgStructureRepository.hasActiveDepartments(unitId));
    }
    orgStructureRepository.setUnitActive(unitId, active);
    auditTrail.registrarEventoCritico(
        actorId,
        active ? "UNIDADE_REATIVADA" : "UNIDADE_INATIVADA",
        "UNIDADE",
        String.valueOf(unitId),
        active ? "Unidade reativada." : "Unidade inativada.");
  }
}
