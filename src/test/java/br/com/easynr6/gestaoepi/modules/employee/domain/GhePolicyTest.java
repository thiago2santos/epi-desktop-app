package br.com.easynr6.gestaoepi.modules.employee.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class GhePolicyTest {

  private final GhePolicy policy = new GhePolicy();

  @Test
  void shouldTrimNameAndRejectBlankOrMissingUnit() {
    assertEquals("Ruido", policy.requireName("  Ruido  "));
    assertThrows(IllegalArgumentException.class, () -> policy.requireName(" ")).getMessage();
    IllegalArgumentException semNome =
        assertThrows(IllegalArgumentException.class, () -> policy.requireName(null));
    IllegalArgumentException semUnidade =
        assertThrows(IllegalArgumentException.class, () -> policy.requireUnit(null));

    assertTrue(semNome.getMessage().startsWith("CAD-051"));
    assertTrue(semUnidade.getMessage().startsWith("CAD-051"));
  }

  @Test
  void shouldRejectInactiveOrForeignJobRoleAndOtherGroup() {
    IllegalArgumentException inativa =
        assertThrows(
            IllegalArgumentException.class, () -> policy.assertJobRoleCanLink(true, false, true));
    IllegalArgumentException outraUnidade =
        assertThrows(
            IllegalArgumentException.class, () -> policy.assertJobRoleCanLink(true, true, false));
    IllegalArgumentException outroGrupo =
        assertThrows(IllegalArgumentException.class, () -> policy.assertNotInOtherGhe(8L, 9L));

    assertTrue(inativa.getMessage().startsWith("CAD-054"));
    assertTrue(outraUnidade.getMessage().startsWith("CAD-054"));
    assertTrue(outroGrupo.getMessage().startsWith("CAD-055"));
  }

  @Test
  void shouldUseOnlyTheActiveGroupListEvenWhenEmpty() {
    List<String> doGrupo = List.of();
    List<String> daFuncao = List.of("luva");

    assertEquals(List.of(), policy.listaVigente(true, doGrupo, daFuncao));
    assertEquals(List.of("capacete"), policy.listaVigente(true, List.of("capacete"), daFuncao));
    assertEquals(daFuncao, policy.listaVigente(false, doGrupo, daFuncao));
    assertEquals(List.of(), policy.listaVigente(false, doGrupo, null));
  }
}
