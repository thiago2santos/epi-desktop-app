package br.com.easynr6.gestaoepi.modules.employee.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UnitPolicyTest {

  private final UnitPolicy policy = new UnitPolicy();

  @Test
  void shouldRejectMissingNameOrCnpj() {
    IllegalArgumentException semNome =
        assertThrows(IllegalArgumentException.class, () -> policy.requireName("  "));
    IllegalArgumentException semCnpj =
        assertThrows(IllegalArgumentException.class, () -> policy.requireCnpj("  "));
    assertEquals("CAD-041 Nome ou CNPJ ausente.", semNome.getMessage());
    assertEquals("CAD-041 Nome ou CNPJ ausente.", semCnpj.getMessage());
  }

  @Test
  void shouldNormalizeMaskedCnpj() {
    assertEquals("22755266000268", policy.requireCnpj("22.755.266/0002-68"));
    assertEquals("22.755.266/0002-68", UnitPolicy.formatarCnpj("22755266000268"));
  }

  @Test
  void shouldRejectInvalidCheckDigitOrLength() {
    IllegalArgumentException digito =
        assertThrows(IllegalArgumentException.class, () -> policy.requireCnpj("22755266000180"));
    IllegalArgumentException tamanho =
        assertThrows(IllegalArgumentException.class, () -> policy.requireCnpj("2275526600018"));
    assertEquals("CAD-042 CNPJ invalido.", digito.getMessage());
    assertEquals("CAD-042 CNPJ invalido.", tamanho.getMessage());
  }

  @Test
  void shouldTrimName() {
    assertEquals("Lagoa Santa", policy.requireName("  Lagoa Santa  "));
  }

  @Test
  void shouldRefuseDeactivationWhenUnitHasActiveDepartment() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> policy.assertCanDeactivate(true));
    assertEquals("CAD-045 Inativacao recusada: a unidade ainda tem setor ativo.", ex.getMessage());
  }
}
