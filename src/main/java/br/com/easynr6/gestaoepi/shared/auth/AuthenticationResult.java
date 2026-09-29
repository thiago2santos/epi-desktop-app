package br.com.easynr6.gestaoepi.shared.auth;

import java.time.LocalDateTime;

public record AuthenticationResult(
    AuthenticationStatus status, UsuarioAutenticado usuario, LocalDateTime bloqueadoAte) {

  public static AuthenticationResult success(UsuarioAutenticado usuario) {
    return new AuthenticationResult(AuthenticationStatus.SUCCESS, usuario, null);
  }

  public static AuthenticationResult forcePasswordChange(UsuarioAutenticado usuario) {
    return new AuthenticationResult(AuthenticationStatus.FORCE_PASSWORD_CHANGE, usuario, null);
  }

  public static AuthenticationResult blocked(LocalDateTime bloqueadoAte) {
    return new AuthenticationResult(AuthenticationStatus.BLOCKED, null, bloqueadoAte);
  }

  public static AuthenticationResult denied(AuthenticationStatus status) {
    return new AuthenticationResult(status, null, null);
  }

  public boolean autenticado() {
    return status == AuthenticationStatus.SUCCESS;
  }
}
