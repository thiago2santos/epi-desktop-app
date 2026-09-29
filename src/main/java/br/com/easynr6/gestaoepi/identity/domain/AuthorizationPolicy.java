package br.com.easynr6.gestaoepi.identity.domain;

import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.util.Set;

public class AuthorizationPolicy {

  public void assertAdmin(Set<Papel> actorRoles) {
    if (actorRoles == null || !actorRoles.contains(Papel.ADMIN)) {
      throw new AuthorizationDeniedException(
          "AUTH-004 Voce nao tem permissao para executar esta acao.");
    }
  }
}
