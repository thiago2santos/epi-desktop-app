package br.com.easynr6.gestaoepi.shared.auth;

public class DuplicateLoginException extends RuntimeException {

  public DuplicateLoginException(String message, Throwable cause) {
    super(message, cause);
  }
}
