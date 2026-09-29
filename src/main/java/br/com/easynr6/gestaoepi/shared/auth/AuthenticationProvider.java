package br.com.easynr6.gestaoepi.shared.auth;

public interface AuthenticationProvider {

  AuthenticationResult autenticar(String login, String senha);
}
