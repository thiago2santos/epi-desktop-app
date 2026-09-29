package br.com.easynr6.gestaoepi.identity.domain;

public record IdentityUser(
    Long id,
    String nome,
    String login,
    String credentialHash,
    boolean ativo,
    CredentialState credentialState) {}
