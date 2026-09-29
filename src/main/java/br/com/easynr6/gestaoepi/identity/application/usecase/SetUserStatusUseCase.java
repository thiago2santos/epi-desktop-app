package br.com.easynr6.gestaoepi.identity.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetUserStatusUseCase {

  private final IdentityRepository identityRepository;
  private final IdentityAdminAuthorizer adminAuthorizer;
  private final AuditTrail auditTrail;

  public SetUserStatusUseCase(
      IdentityRepository identityRepository,
      IdentityAdminAuthorizer adminAuthorizer,
      AuditTrail auditTrail) {
    this.identityRepository = identityRepository;
    this.adminAuthorizer = adminAuthorizer;
    this.auditTrail = auditTrail;
  }

  @Transactional
  public void block(Long adminId, Long userId) {
    setStatus(adminId, userId, false, "USUARIO_BLOQUEADO", "Usuario bloqueado para autenticacao");
  }

  @Transactional
  public void reactivate(Long adminId, Long userId) {
    setStatus(adminId, userId, true, "USUARIO_REATIVADO", "Usuario reativado para autenticacao");
  }

  private void setStatus(Long adminId, Long userId, boolean active, String action, String details) {
    adminAuthorizer.requireAdmin(adminId);
    if (!identityRepository.userExists(userId)) {
      throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
    }
    identityRepository.setUserActive(userId, active);
    auditTrail.registrarEventoCritico(adminId, action, "USUARIO", String.valueOf(userId), details);
  }
}
