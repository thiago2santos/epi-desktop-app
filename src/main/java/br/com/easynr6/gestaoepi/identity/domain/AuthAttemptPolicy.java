package br.com.easynr6.gestaoepi.identity.domain;

import java.time.LocalDateTime;

public record AuthAttemptPolicy(int maxInvalidAttempts, int blockMinutes) {

  public static AuthAttemptPolicy defaults() {
    return new AuthAttemptPolicy(5, 15);
  }

  public boolean shouldBlock(int attempts) {
    return attempts >= maxInvalidAttempts;
  }

  public LocalDateTime blockedUntil(LocalDateTime now) {
    return now.plusMinutes(blockMinutes);
  }
}
