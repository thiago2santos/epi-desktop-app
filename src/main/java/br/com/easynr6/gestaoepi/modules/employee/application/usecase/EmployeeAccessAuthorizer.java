package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class EmployeeAccessAuthorizer {

  private final IdentityRepository identityRepository;

  public EmployeeAccessAuthorizer(IdentityRepository identityRepository) {
    this.identityRepository = identityRepository;
  }

  public void assertCanManageEmployees(Long actorId) {
    if (!podeDefinirSenha(actorId)) {
      throw new AuthorizationDeniedException(
          "AUTH-004 Voce nao tem permissao para executar esta acao.");
    }
  }

  public void assertCanDefineReceiptPassword(Long actorId) {
    if (!podeDefinirSenha(actorId)) {
      throw new AuthorizationDeniedException(
          "AUTH-004 Voce nao tem permissao para definir a senha de recebimento.");
    }
  }

  private boolean podeDefinirSenha(Long actorId) {
    Set<Papel> roles = identityRepository.loadRoles(actorId);
    return roles.contains(Papel.ADMIN) || roles.contains(Papel.SESMT);
  }
}
