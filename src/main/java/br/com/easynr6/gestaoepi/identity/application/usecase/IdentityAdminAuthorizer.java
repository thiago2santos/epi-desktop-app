package br.com.easynr6.gestaoepi.identity.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.identity.domain.AuthorizationPolicy;
import org.springframework.stereotype.Component;

@Component
public class IdentityAdminAuthorizer {

  private final IdentityRepository identityRepository;
  private final AuthorizationPolicy authorizationPolicy = new AuthorizationPolicy();

  public IdentityAdminAuthorizer(IdentityRepository identityRepository) {
    this.identityRepository = identityRepository;
  }

  public void requireAdmin(Long actorId) {
    authorizationPolicy.assertAdmin(identityRepository.loadRoles(actorId));
  }
}
