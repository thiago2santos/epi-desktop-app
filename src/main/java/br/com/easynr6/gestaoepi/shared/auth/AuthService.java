package br.com.easynr6.gestaoepi.shared.auth;

import br.com.easynr6.gestaoepi.identity.application.usecase.AuthenticateUserUseCase;
import br.com.easynr6.gestaoepi.identity.application.usecase.ChangeOwnCredentialUseCase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "easy-nr6.auth.provider", havingValue = "jdbc", matchIfMissing = true)
public class AuthService implements AuthenticationProvider, CredentialManager {

  private final AuthenticateUserUseCase authenticateUserUseCase;
  private final ChangeOwnCredentialUseCase changeOwnCredentialUseCase;

  public AuthService(
      AuthenticateUserUseCase authenticateUserUseCase,
      ChangeOwnCredentialUseCase changeOwnCredentialUseCase) {
    this.authenticateUserUseCase = authenticateUserUseCase;
    this.changeOwnCredentialUseCase = changeOwnCredentialUseCase;
  }

  @Override
  public AuthenticationResult autenticar(String login, String senha) {
    return authenticateUserUseCase.execute(login, senha);
  }

  @Override
  public void alterarCredencialObrigatoria(Long usuarioId, String login, String novaSenha) {
    changeOwnCredentialUseCase.execute(usuarioId, login, novaSenha);
  }
}
