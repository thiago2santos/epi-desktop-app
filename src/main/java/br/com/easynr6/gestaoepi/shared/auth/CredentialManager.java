package br.com.easynr6.gestaoepi.shared.auth;

public interface CredentialManager {

  void alterarCredencialObrigatoria(Long usuarioId, String login, String novaSenha);
}
