package br.com.easynr6.gestaoepi.shared.auth;

public class WeakPasswordException extends RuntimeException {

  public WeakPasswordException(String message) {
    super(message);
  }
}
