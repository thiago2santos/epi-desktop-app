package br.com.easynr6.gestaoepi.shared.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PasswordPolicyTest {

  private final PasswordPolicy passwordPolicy = new PasswordPolicy();

  @Test
  void deveAceitarSenhaForteValida() {
    assertDoesNotThrow(() -> passwordPolicy.validar("operador", "SenhaForte#2026"));
  }

  @Test
  void deveRecusarSenhaCurta() {
    assertThrows(WeakPasswordException.class, () -> passwordPolicy.validar("operador", "Abc1#"));
  }

  @Test
  void deveRecusarSenhaComLogin() {
    assertThrows(
        WeakPasswordException.class, () -> passwordPolicy.validar("operador", "operador#2026"));
  }

  @Test
  void deveRecusarSenhaTrivial() {
    assertThrows(WeakPasswordException.class, () -> passwordPolicy.validar("admin", "admin123"));
  }
}
