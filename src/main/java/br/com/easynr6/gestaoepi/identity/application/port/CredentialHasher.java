package br.com.easynr6.gestaoepi.identity.application.port;

public interface CredentialHasher {

  String encode(String rawCredential);

  boolean matches(String rawCredential, String encodedCredential);
}
