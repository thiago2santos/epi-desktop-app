package br.com.easynr6.gestaoepi.shared.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class InstanteAuditoriaTest {

  private static final Instant UTC = Instant.parse("2026-10-04T19:51:07Z");

  @Test
  void cadaFusoDoBrasilGravaASuaHora() {
    assertEquals(
        "2026-10-04 17:51:07", InstanteAuditoria.formatar(UTC, ZoneId.of("America/Noronha")));
    assertEquals(
        "2026-10-04 16:51:07", InstanteAuditoria.formatar(UTC, ZoneId.of("America/Sao_Paulo")));
    assertEquals(
        "2026-10-04 15:51:07", InstanteAuditoria.formatar(UTC, ZoneId.of("America/Manaus")));
    assertEquals(
        "2026-10-04 14:51:07", InstanteAuditoria.formatar(UTC, ZoneId.of("America/Rio_Branco")));
  }
}
