package br.com.easynr6.gestaoepi.identity.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.CredentialHasher;
import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.PasswordPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeOwnCredentialUseCase {

  private final IdentityRepository identityRepository;
  private final CredentialHasher credentialHasher;
  private final PasswordPolicy passwordPolicy;
  private final AuditTrail auditTrail;

  public ChangeOwnCredentialUseCase(
      IdentityRepository identityRepository,
      CredentialHasher credentialHasher,
      PasswordPolicy passwordPolicy,
      AuditTrail auditTrail) {
    this.identityRepository = identityRepository;
    this.credentialHasher = credentialHasher;
    this.passwordPolicy = passwordPolicy;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "CREDENCIAL_TROCADA", entidade = "USUARIO", alvo = 0)
  @Transactional
  public void execute(Long userId, String login, String newCredential) {
    passwordPolicy.validar(login, newCredential);
    String encoded = credentialHasher.encode(newCredential);
    identityRepository.updateCredential(userId, encoded, false, 0, null, true);
    auditTrail.registrarEventoCritico(
        userId,
        "CREDENCIAL_TROCADA",
        "USUARIO",
        String.valueOf(userId),
        "Troca obrigatoria de credencial concluida");
  }
}
