package br.com.easynr6.gestaoepi.shared.auth;

import java.util.Optional;

public interface AuthenticationProvider {

  Optional<UsuarioAutenticado> autenticar(String login, String senha);
}
