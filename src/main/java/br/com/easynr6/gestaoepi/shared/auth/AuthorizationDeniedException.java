package br.com.easynr6.gestaoepi.shared.auth;

public class AuthorizationDeniedException extends RuntimeException {

  public AuthorizationDeniedException(String message) {
    super(message);
  }
}
