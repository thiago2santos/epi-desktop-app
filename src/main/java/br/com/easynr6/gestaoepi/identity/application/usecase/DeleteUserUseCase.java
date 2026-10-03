package br.com.easynr6.gestaoepi.identity.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteUserUseCase {

  private final IdentityRepository identityRepository;
  private final IdentityAdminAuthorizer adminAuthorizer;
  private final AuditTrail auditTrail;

  public DeleteUserUseCase(
      IdentityRepository identityRepository,
      IdentityAdminAuthorizer adminAuthorizer,
      AuditTrail auditTrail) {
    this.identityRepository = identityRepository;
    this.adminAuthorizer = adminAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "USUARIO_EXCLUIDO", entidade = "USUARIO", alvo = 1)
  @Transactional
  public void execute(Long adminId, Long userId) {
    adminAuthorizer.requireAdmin(adminId);
    if (adminId.equals(userId)) {
      throw new IllegalArgumentException("AUTH-016 Nao e permitido excluir o proprio usuario.");
    }
    if (!identityRepository.userExists(userId)) {
      throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
    }
    try {
      identityRepository.deleteUserRoles(userId);
      int removed = identityRepository.deleteUser(userId);
      if (removed == 0) {
        throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
      }
    } catch (DataIntegrityViolationException ex) {
      throw new IllegalStateException(
          "AUTH-015 Usuario possui historico vinculado e nao pode ser excluido.", ex);
    }
    auditTrail.registrarEventoCritico(
        adminId, "USUARIO_EXCLUIDO", "USUARIO", String.valueOf(userId), "Usuario removido da base");
  }
}
