package br.com.easynr6.gestaoepi.ui.auditoria;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AuditoriaUxTest {

  @Test
  void filtroLivreRemoveEspacoNasPontas() {
    assertEquals("", AuditTrailView.termoAuditoria(null));
    assertEquals("", AuditTrailView.termoAuditoria("   "));
    assertEquals("EPI_CREATED", AuditTrailView.termoAuditoria("  EPI_CREATED  "));
  }
}
