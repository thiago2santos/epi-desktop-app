package br.com.easynr6.gestaoepi.modules.issuance.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PendenciaPolicyTest {

  private final PendenciaPolicy policy = new PendenciaPolicy();

  @Test
  void shouldListInactiveIndividualAndOffMatrixItemsOnly() {
    assertTrue(policy.entra(false, "INDIVIDUAL"));
    assertTrue(policy.entra(false, null));
    assertFalse(policy.entra(false, "POSTO"));
    assertFalse(policy.entra(true, "INDIVIDUAL"));
  }
}
