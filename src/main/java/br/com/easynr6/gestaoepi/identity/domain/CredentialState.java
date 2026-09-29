package br.com.easynr6.gestaoepi.identity.domain;

import java.time.LocalDateTime;

public record CredentialState(
    boolean forcePasswordChange, int invalidAttempts, LocalDateTime blockedUntil) {

  public boolean isBlockedAt(LocalDateTime reference) {
    return blockedUntil != null && blockedUntil.isAfter(reference);
  }
}
