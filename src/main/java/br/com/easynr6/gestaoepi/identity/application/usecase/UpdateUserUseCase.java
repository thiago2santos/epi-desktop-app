package br.com.easynr6.gestaoepi.identity.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.DuplicateLoginException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUserUseCase {

  private final IdentityRepository identityRepository;
  private final IdentityAdminAuthorizer adminAuthorizer;
  private final AuditTrail auditTrail;

  public UpdateUserUseCase(
      IdentityRepository identityRepository,
      IdentityAdminAuthorizer adminAuthorizer,
      AuditTrail auditTrail) {
    this.identityRepository = identityRepository;
    this.adminAuthorizer = adminAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "USUARIO_EDITADO", entidade = "USUARIO", alvo = 1)
  @Transactional
  public void execute(Long adminId, Long userId, String nome, String login, boolean ativo) {
    adminAuthorizer.requireAdmin(adminId);
    if (!identityRepository.userExists(userId)) {
      throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
    }
    if (nome == null || nome.isBlank() || login == null || login.isBlank()) {
      throw new IllegalArgumentException("AUTH-010 Nome e login sao obrigatorios.");
    }
    try {
      identityRepository.updateUser(userId, nome.trim(), login.trim(), ativo);
    } catch (DuplicateKeyException ex) {
      throw new DuplicateLoginException("AUTH-005 Login ja utilizado.", ex);
    }
    auditTrail.registrarEventoCritico(
        adminId,
        "USUARIO_EDITADO",
        "USUARIO",
        String.valueOf(userId),
        "Cadastro basico do usuario atualizado");
  }
}
