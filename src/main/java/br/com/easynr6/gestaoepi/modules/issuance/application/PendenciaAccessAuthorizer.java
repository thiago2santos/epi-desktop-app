package br.com.easynr6.gestaoepi.modules.issuance.application;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PendenciaAccessAuthorizer {

  static final String SEM_PERMISSAO =
      "AUTH-004 Voce nao tem permissao para consultar pendencias de devolucao.";

  private final IdentityRepository identityRepository;

  public PendenciaAccessAuthorizer(IdentityRepository identityRepository) {
    this.identityRepository = identityRepository;
  }

  public void assertCanRead(Long actorId) {
    Set<Papel> papeis = actorId == null ? Set.of() : identityRepository.loadRoles(actorId);
    if (papeis.contains(Papel.ADMIN)
        || papeis.contains(Papel.SESMT)
        || papeis.contains(Papel.ALMOXARIFE)) {
      return;
    }
    throw new AuthorizationDeniedException(SEM_PERMISSAO);
  }
}
