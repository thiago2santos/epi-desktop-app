package br.com.easynr6.gestaoepi.identity.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignRoleUseCase {

  private final IdentityRepository identityRepository;
  private final IdentityAdminAuthorizer adminAuthorizer;
  private final AuditTrail auditTrail;

  public AssignRoleUseCase(
      IdentityRepository identityRepository,
      IdentityAdminAuthorizer adminAuthorizer,
      AuditTrail auditTrail) {
    this.identityRepository = identityRepository;
    this.adminAuthorizer = adminAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "PAPEL_ATRIBUIDO", entidade = "USUARIO", alvo = 1)
  @Transactional
  public void execute(Long adminId, Long userId, Papel papel) {
    adminAuthorizer.requireAdmin(adminId);
    if (!identityRepository.userExists(userId)) {
      throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
    }
    identityRepository.assignRole(userId, papel);
    auditTrail.registrarEventoCritico(
        adminId,
        "PAPEL_ATRIBUIDO",
        "USUARIO",
        String.valueOf(userId),
        "Papel atribuido: " + papel.name());
  }
}
