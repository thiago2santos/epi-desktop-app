package br.com.easynr6.gestaoepi.shared.audit;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Hora civil da instalação. Brasília, Manaus, Acre e Fernando de Noronha seguem o fuso da máquina.
 */
public final class InstanteAuditoria {

  private static final DateTimeFormatter FORMATO =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private InstanteAuditoria() {}

  public static String agora() {
    return formatar(Instant.now(), ZoneId.systemDefault());
  }

  static String formatar(Instant instante, ZoneId zona) {
    return FORMATO.format(instante.atZone(zona));
  }
}
