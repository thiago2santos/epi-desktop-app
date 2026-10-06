package br.com.easynr6.gestaoepi.modules.issuance.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EstornoPolicyTest {

  private final EstornoPolicy policy = new EstornoPolicy();

  @Test
  void shouldRefuseAShortReason() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> policy.motivo("errei"));

    assertTrue(ex.getMessage().startsWith("POS-005"));
    assertEquals("lancamento errado", policy.motivo("  lancamento errado  "));
  }
}
