package br.com.easynr6.gestaoepi.shared.audit;

import java.util.UUID;
import org.slf4j.MDC;

/** Id curto da ação em curso. O mesmo valor vai no log e na linha de auditoria. */
public final class Correlacao {

  public static final String CHAVE_MDC = "correlacao";

  private static final ThreadLocal<String> ATUAL = new ThreadLocal<>();

  private Correlacao() {}

  public static String atual() {
    return ATUAL.get();
  }

  public static String novoId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  }

  /**
   * @return true quando esta chamada abriu o contexto e portanto deve fechá-lo.
   */
  public static boolean abrir() {
    if (ATUAL.get() != null) {
      return false;
    }
    String id = novoId();
    ATUAL.set(id);
    MDC.put(CHAVE_MDC, id);
    return true;
  }

  public static void fechar(boolean abertaAqui) {
    if (!abertaAqui) {
      return;
    }
    ATUAL.remove();
    MDC.remove(CHAVE_MDC);
  }
}
