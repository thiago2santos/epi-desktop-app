package br.com.easynr6.gestaoepi.shared.auth;

public enum AuthenticationStatus {
  SUCCESS,
  INVALID_CREDENTIAL,
  INACTIVE_USER,
  BLOCKED,
  FORCE_PASSWORD_CHANGE,
  NO_ROLE
}
