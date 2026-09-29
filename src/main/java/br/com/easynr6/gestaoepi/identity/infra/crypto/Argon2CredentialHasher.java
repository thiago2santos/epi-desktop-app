package br.com.easynr6.gestaoepi.identity.infra.crypto;

import br.com.easynr6.gestaoepi.identity.application.port.CredentialHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Argon2CredentialHasher implements CredentialHasher {

  private final PasswordEncoder passwordEncoder;

  public Argon2CredentialHasher(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public String encode(String rawCredential) {
    return passwordEncoder.encode(rawCredential);
  }

  @Override
  public boolean matches(String rawCredential, String encodedCredential) {
    return passwordEncoder.matches(rawCredential, encodedCredential);
  }
}
