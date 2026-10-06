package br.com.easynr6.gestaoepi.ui.operacao;

import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;

/** Consulta só lê. Estornar fica com SESMT e Admin. */
public final class HistoricoAcoes {

  private HistoricoAcoes() {}

  public static boolean devolver(UsuarioAutenticado usuario) {
    return usuario != null && (usuario.temPapel(Papel.SESMT) || usuario.temPapel(Papel.ALMOXARIFE));
  }

  public static boolean estornar(UsuarioAutenticado usuario) {
    return usuario != null && usuario.temPapel(Papel.SESMT);
  }
}
