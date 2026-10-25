package br.com.easynr6.gestaoepi.shared.auth;

import java.util.Set;

public record UsuarioAutenticado(
    Long id,
    String nome,
    String login,
    Set<Papel> papeis,
    Long unidadeId,
    String unidadeNome,
    Long unidadeVinculoId) {

  public UsuarioAutenticado(Long id, String nome, String login, Set<Papel> papeis) {
    this(id, nome, login, papeis, null, null, null);
  }

  public UsuarioAutenticado(
      Long id, String nome, String login, Set<Papel> papeis, Long unidadeId, String unidadeNome) {
    this(id, nome, login, papeis, unidadeId, unidadeNome, null);
  }

  public UsuarioAutenticado naUnidade(Long unidadeId, String unidadeNome) {
    return new UsuarioAutenticado(
        id, nome, login, papeis, unidadeId, unidadeNome, unidadeVinculoId);
  }

  public UsuarioAutenticado comVinculo(Long unidadeVinculoId) {
    return new UsuarioAutenticado(
        id, nome, login, papeis, unidadeId, unidadeNome, unidadeVinculoId);
  }

  public boolean veTodasAsUnidades() {
    return EscopoUnidade.veTodas(papeis, unidadeVinculoId);
  }

  public boolean temPapel(Papel papel) {
    return papeis.contains(Papel.ADMIN) || papeis.contains(papel);
  }
}
