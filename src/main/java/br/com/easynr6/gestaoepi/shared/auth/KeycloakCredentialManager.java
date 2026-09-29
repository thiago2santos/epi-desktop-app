package br.com.easynr6.gestaoepi.shared.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "easy-nr6.auth.provider", havingValue = "keycloak")
public class KeycloakCredentialManager implements CredentialManager {

  @Override
  public void alterarCredencialObrigatoria(Long usuarioId, String login, String novaSenha) {
    throw new UnsupportedOperationException(
        "AUTH-020 Troca de credencial deve ser realizada no IAM provider configurado.");
  }
}
