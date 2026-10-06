package br.com.easynr6.gestaoepi.modules.employee.domain;

/** Quem a matriz, o fornecimento e a cobertura leem para uma função. */
public record PerfilVigente(Tipo tipo, long id) {

  public enum Tipo {
    FUNCAO,
    GHE
  }

  public static PerfilVigente daFuncao(long jobRoleId) {
    return new PerfilVigente(Tipo.FUNCAO, jobRoleId);
  }

  public static PerfilVigente doGhe(long gheId) {
    return new PerfilVigente(Tipo.GHE, gheId);
  }
}
