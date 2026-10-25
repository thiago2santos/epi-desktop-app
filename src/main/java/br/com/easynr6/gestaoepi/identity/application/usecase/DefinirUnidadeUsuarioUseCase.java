package br.com.easynr6.gestaoepi.identity.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefinirUnidadeUsuarioUseCase {

  private final IdentityRepository identityRepository;
  private final IdentityAdminAuthorizer adminAuthorizer;
  private final AuditTrail auditTrail;

  public DefinirUnidadeUsuarioUseCase(
      IdentityRepository identityRepository,
      IdentityAdminAuthorizer adminAuthorizer,
      AuditTrail auditTrail) {
    this.identityRepository = identityRepository;
    this.adminAuthorizer = adminAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "USUARIO_UNIDADE", entidade = "USUARIO", alvo = 1)
  @Transactional
  public void execute(Long adminId, Long userId, Long unidadeId) {
    adminAuthorizer.requireAdmin(adminId);
    if (!identityRepository.userExists(userId)) {
      throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
    }
    if (unidadeId != null && !identityRepository.unidadeAtiva(unidadeId)) {
      throw new IllegalArgumentException("AUTH-018 Escolha uma unidade ativa.");
    }
    identityRepository.definirUnidade(userId, unidadeId);
    auditTrail.registrarEventoCritico(
        adminId,
        "USUARIO_UNIDADE",
        "USUARIO",
        String.valueOf(userId),
        unidadeId == null
            ? "Usuario sem vinculo de unidade"
            : "Usuario vinculado a unidade " + unidadeId);
  }
}
