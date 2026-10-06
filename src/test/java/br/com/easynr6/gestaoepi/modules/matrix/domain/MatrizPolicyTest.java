package br.com.easynr6.gestaoepi.modules.matrix.domain;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import org.junit.jupiter.api.Test;

class MatrizPolicyTest {

  private final MatrizPolicy policy = new MatrizPolicy();

  @Test
  void shouldRejectMissingPairDuplicateAndMissingCa() {
    IllegalArgumentException ausente =
        assertThrows(
            IllegalArgumentException.class,
            () -> policy.requireInclusao(PerfilVigente.Tipo.FUNCAO, null, 1L));
    IllegalArgumentException duplicada =
        assertThrows(IllegalArgumentException.class, () -> policy.assertSemLinhaAtiva(true));
    IllegalArgumentException semCa =
        assertThrows(IllegalArgumentException.class, () -> policy.assertTemCaAtivo(false));

    assertTrue(ausente.getMessage().startsWith("MAT-001"));
    assertTrue(duplicada.getMessage().startsWith("MAT-002"));
    assertTrue(semCa.getMessage().startsWith("MAT-003"));
  }

  @Test
  void shouldRejectInactiveProfileAndFunctionInsideActiveGroup() {
    IllegalArgumentException inativo =
        assertThrows(
            IllegalArgumentException.class,
            () -> policy.assertPodeIncluir(PerfilVigente.Tipo.FUNCAO, true, false, false));
    IllegalArgumentException noGrupo =
        assertThrows(
            IllegalArgumentException.class,
            () -> policy.assertPodeIncluir(PerfilVigente.Tipo.FUNCAO, true, true, true));
    IllegalArgumentException linha =
        assertThrows(IllegalArgumentException.class, () -> policy.assertLinhaAtiva(true, false));

    assertTrue(inativo.getMessage().startsWith("MAT-004"));
    assertTrue(noGrupo.getMessage().startsWith("MAT-008"));
    assertTrue(linha.getMessage().startsWith("MAT-007"));
  }
}
