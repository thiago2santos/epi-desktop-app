package br.com.easynr6.gestaoepi.shared.auth;

import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PasswordPolicy {

  private static final int MIN_LENGTH = 12;
  private static final Set<String> PROHIBIDAS =
      Set.of("123456", "12345678", "password", "qwerty", "admin123", "abcdef");

  public void validar(String login, String senha) {
    if (senha == null || senha.length() < MIN_LENGTH) {
      throw new WeakPasswordException("AUTH-006 Credencial fora da politica minima de seguranca.");
    }

    String senhaLower = senha.toLowerCase(Locale.ROOT);
    String loginNormalizado = login == null ? "" : login.toLowerCase(Locale.ROOT).trim();
    if (!loginNormalizado.isBlank() && senhaLower.contains(loginNormalizado)) {
      throw new WeakPasswordException("AUTH-006 Credencial fora da politica minima de seguranca.");
    }
    if (PROHIBIDAS.contains(senhaLower)) {
      throw new WeakPasswordException("AUTH-006 Credencial fora da politica minima de seguranca.");
    }

    int categorias = 0;
    if (senha.chars().anyMatch(Character::isUpperCase)) {
      categorias += 1;
    }
    if (senha.chars().anyMatch(Character::isLowerCase)) {
      categorias += 1;
    }
    if (senha.chars().anyMatch(Character::isDigit)) {
      categorias += 1;
    }
    if (senha.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch))) {
      categorias += 1;
    }
    if (categorias < 3) {
      throw new WeakPasswordException("AUTH-006 Credencial fora da politica minima de seguranca.");
    }
  }
}
