package br.com.easynr6.gestaoepi.modules.stock.application;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class StockAccessAuthorizer {

  private static final String SEM_PERMISSAO =
      "AUTH-004 Voce nao tem permissao para executar esta acao.";

  private final IdentityRepository identityRepository;

  public StockAccessAuthorizer(IdentityRepository identityRepository) {
    this.identityRepository = identityRepository;
  }

  public void assertCanView(Long actorId) {
    Set<Papel> roles = identityRepository.loadRoles(actorId);
    if (roles.contains(Papel.ADMIN)
        || roles.contains(Papel.SESMT)
        || roles.contains(Papel.ALMOXARIFE)
        || roles.contains(Papel.CONSULTA)) {
      return;
    }
    throw new AuthorizationDeniedException(SEM_PERMISSAO);
  }

  public void assertCanReceive(Long actorId) {
    Set<Papel> roles = identityRepository.loadRoles(actorId);
    if (roles.contains(Papel.ADMIN) || roles.contains(Papel.ALMOXARIFE)) {
      return;
    }
    throw new AuthorizationDeniedException(SEM_PERMISSAO);
  }
}
