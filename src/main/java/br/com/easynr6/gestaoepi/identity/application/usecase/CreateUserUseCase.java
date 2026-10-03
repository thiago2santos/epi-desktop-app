package br.com.easynr6.gestaoepi.identity.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.CredentialHasher;
import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.DuplicateLoginException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.PasswordPolicy;
import java.util.Set;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUserUseCase {

  private final IdentityRepository identityRepository;
  private final CredentialHasher credentialHasher;
  private final PasswordPolicy passwordPolicy;
  private final IdentityAdminAuthorizer adminAuthorizer;
  private final AuditTrail auditTrail;

  public CreateUserUseCase(
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

  @AcaoAuditada(acao = "USUARIO_CRIADO", entidade = "USUARIO")
  @Transactional
  public Long execute(
      Long adminId,
      String nome,
      String login,
      String senhaInicial,
      boolean ativo,
      Set<Papel> papeis) {
    adminAuthorizer.requireAdmin(adminId);
    if (nome == null || nome.isBlank() || login == null || login.isBlank()) {
      throw new IllegalArgumentException("AUTH-010 Nome e login sao obrigatorios.");
    }
    if (papeis == null || papeis.isEmpty()) {
      throw new IllegalArgumentException("AUTH-008 Defina ao menos um papel para o usuario.");
    }

    String normalizedLogin = login.trim();
    passwordPolicy.validar(normalizedLogin, senhaInicial);
    String encoded = credentialHasher.encode(senhaInicial);

    final Long createdUserId;
    try {
      createdUserId =
          identityRepository.createUser(nome.trim(), normalizedLogin, encoded, ativo, true);
    } catch (DuplicateKeyException ex) {
      throw new DuplicateLoginException("AUTH-005 Login ja utilizado.", ex);
    }

    for (Papel papel : papeis) {
      identityRepository.assignRole(createdUserId, papel);
    }

    auditTrail.registrarEventoCritico(
        adminId,
        "USUARIO_CRIADO",
        "USUARIO",
        String.valueOf(createdUserId),
        "Cadastro de usuario com troca obrigatoria de credencial");
    return createdUserId;
  }
}
