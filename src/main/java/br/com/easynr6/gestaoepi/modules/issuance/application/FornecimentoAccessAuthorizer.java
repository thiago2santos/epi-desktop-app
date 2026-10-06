package br.com.easynr6.gestaoepi.modules.issuance.application;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class FornecimentoAccessAuthorizer {

  static final String SEM_PERMISSAO =
      "AUTH-004 Voce nao tem permissao para registrar fornecimento.";

  private final IdentityRepository identityRepository;

  public FornecimentoAccessAuthorizer(IdentityRepository identityRepository) {
    this.identityRepository = identityRepository;
  }

  public void assertCanRegister(Long actorId) {
    if (!podeRegistrar(papeis(actorId))) {
      throw new AuthorizationDeniedException(SEM_PERMISSAO);
    }
  }

  public boolean podeRegistrarExcecao(Long actorId) {
    Set<Papel> papeis = papeis(actorId);
    return papeis.contains(Papel.ADMIN) || papeis.contains(Papel.SESMT);
  }

  private Set<Papel> papeis(Long actorId) {
    if (actorId == null) {
      return Set.of();
    }
    return identityRepository.loadRoles(actorId);
  }

  private static boolean podeRegistrar(Set<Papel> papeis) {
    return papeis.contains(Papel.ADMIN)
        || papeis.contains(Papel.SESMT)
        || papeis.contains(Papel.ALMOXARIFE);
  }
}
