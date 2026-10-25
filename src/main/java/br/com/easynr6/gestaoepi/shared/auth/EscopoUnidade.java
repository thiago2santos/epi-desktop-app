package br.com.easynr6.gestaoepi.shared.auth;

import java.util.Set;

/** Quem vê todas as plantas e quem precisa de uma unidade de vínculo. */
public final class EscopoUnidade {

  private EscopoUnidade() {}

  public static boolean veTodas(Set<Papel> papeis, Long unidadeVinculoId) {
    if (unidadeVinculoId != null || papeis == null) {
      return false;
    }
    return papeis.contains(Papel.ADMIN) || papeis.contains(Papel.SESMT);
  }

  public static boolean exigeVinculo(Papel papel) {
    return papel == Papel.ALMOXARIFE || papel == Papel.CONSULTA;
  }
}
