package br.com.easynr6.gestaoepi.identity.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.CredentialHasher;
import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.PasswordPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResetCredentialUseCase {

  private final IdentityRepository identityRepository;
  private final CredentialHasher credentialHasher;
  private final PasswordPolicy passwordPolicy;
  private final IdentityAdminAuthorizer adminAuthorizer;
  private final AuditTrail auditTrail;

  public ResetCredentialUseCase(
      IdentityRepository identityRepository,
      CredentialHasher credentialHasher,
      PasswordPolicy passwordPolicy,
      IdentityAdminAuthorizer adminAuthorizer,
      AuditTrail auditTrail) {
    this.identityRepository = identityRepository;
    this.credentialHasher = credentialHasher;
    this.passwordPolicy = passwordPolicy;
    this.adminAuthorizer = adminAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "CREDENCIAL_RESETADA", entidade = "USUARIO", alvo = 1)
  @Transactional
  public void execute(Long adminId, Long userId, String login, String newCredential) {
    adminAuthorizer.requireAdmin(adminId);
    if (!identityRepository.userExists(userId)) {
      throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
    }
    passwordPolicy.validar(login, newCredential);
    String encoded = credentialHasher.encode(newCredential);
    identityRepository.updateCredential(userId, encoded, true, 0, null, false);
    auditTrail.registrarEventoCritico(
        adminId,
        "CREDENCIAL_RESETADA",
        "USUARIO",
        String.valueOf(userId),
        "Reset administrativo de credencial");
  }
}
