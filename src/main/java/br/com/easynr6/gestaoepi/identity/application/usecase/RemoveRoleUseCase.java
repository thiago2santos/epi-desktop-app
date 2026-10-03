package br.com.easynr6.gestaoepi.identity.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveRoleUseCase {

  private final IdentityRepository identityRepository;
  private final IdentityAdminAuthorizer adminAuthorizer;
  private final AuditTrail auditTrail;

  public RemoveRoleUseCase(
      IdentityRepository identityRepository,
      IdentityAdminAuthorizer adminAuthorizer,
      AuditTrail auditTrail) {
    this.identityRepository = identityRepository;
    this.adminAuthorizer = adminAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "PAPEL_REMOVIDO", entidade = "USUARIO", alvo = 1)
  @Transactional
  public void execute(Long adminId, Long userId, Papel papel) {
    adminAuthorizer.requireAdmin(adminId);
    if (!identityRepository.userExists(userId)) {
      throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
    }
    int removed = identityRepository.removeRole(userId, papel);
    if (removed == 0) {
      throw new IllegalArgumentException("AUTH-017 Papel nao encontrado para o usuario.");
    }
    auditTrail.registrarEventoCritico(
        adminId,
        "PAPEL_REMOVIDO",
        "USUARIO",
        String.valueOf(userId),
        "Papel removido: " + papel.name());
  }
}
