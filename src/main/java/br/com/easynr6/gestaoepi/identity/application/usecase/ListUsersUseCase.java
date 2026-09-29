package br.com.easynr6.gestaoepi.identity.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.shared.auth.UserAdministrationService.UsuarioAdminResumo;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListUsersUseCase {

  private final IdentityRepository identityRepository;

  public ListUsersUseCase(IdentityRepository identityRepository) {
    this.identityRepository = identityRepository;
  }

  public List<UsuarioAdminResumo> execute() {
    return identityRepository.listUsers();
  }
}
