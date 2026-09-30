package br.com.easynr6.gestaoepi.modules.epi.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class EpiCatalogAccessAuthorizer {

  private final IdentityRepository identityRepository;

  public EpiCatalogAccessAuthorizer(IdentityRepository identityRepository) {
    this.identityRepository = identityRepository;
  }

  public void assertCanManageCatalog(Long actorId) {
    Set<Papel> roles = identityRepository.loadRoles(actorId);
    if (!roles.contains(Papel.ADMIN) && !roles.contains(Papel.SESMT)) {
      throw new AuthorizationDeniedException(
          "AUTH-004 Voce nao tem permissao para executar esta acao.");
    }
  }
}
