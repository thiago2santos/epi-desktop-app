package br.com.easynr6.gestaoepi.modules.issuance.application;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class EstornoAccessAuthorizer {

  static final String SEM_PERMISSAO = "AUTH-004 Voce nao tem permissao para estornar fornecimento.";

  private final IdentityRepository identityRepository;

  public EstornoAccessAuthorizer(IdentityRepository identityRepository) {
    this.identityRepository = identityRepository;
  }

  public void assertCanRegister(Long actorId) {
    Set<Papel> papeis = actorId == null ? Set.of() : identityRepository.loadRoles(actorId);
    if (papeis.contains(Papel.ADMIN) || papeis.contains(Papel.SESMT)) {
      return;
    }
    throw new AuthorizationDeniedException(SEM_PERMISSAO);
  }
}
