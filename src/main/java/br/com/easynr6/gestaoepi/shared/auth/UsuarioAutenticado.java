package br.com.easynr6.gestaoepi.shared.auth;

import java.util.Set;

public record UsuarioAutenticado(Long id, String nome, String login, Set<Papel> papeis) {

  public boolean temPapel(Papel papel) {
    return papeis.contains(Papel.ADMIN) || papeis.contains(papel);
  }
}
