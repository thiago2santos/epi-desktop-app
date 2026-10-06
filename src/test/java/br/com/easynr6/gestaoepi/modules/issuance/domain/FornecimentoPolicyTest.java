package br.com.easynr6.gestaoepi.modules.issuance.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FornecimentoPolicyTest {

  private final FornecimentoPolicy policy = new FornecimentoPolicy();

  @Test
  void shouldRefuseWarehouseExceptionEvenWithText() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> policy.excecao(false, false, "texto longo de excecao"));

    assertTrue(ex.getMessage().startsWith("ENT-006"));
  }

  @Test
  void shouldRequireTenCharactersFromSesmt() {
    IllegalArgumentException curto =
        assertThrows(IllegalArgumentException.class, () -> policy.excecao(false, true, "curto"));

    assertTrue(curto.getMessage().startsWith("ENT-007"));
    assertEquals("excecao ok", policy.excecao(false, true, " excecao ok "));
    assertNull(policy.excecao(true, false, "nao aplica"));
  }

  @Test
  void shouldRefuseExpiredLotDuplicateAndMissingTerm() {
    IllegalArgumentException vencido =
        assertThrows(IllegalArgumentException.class, () -> policy.lote(true, true, true, true));
    IllegalArgumentException repetido =
        assertThrows(IllegalArgumentException.class, () -> policy.lotesUnicos(List.of(1L, 1L)));
    IllegalArgumentException termo =
        assertThrows(IllegalArgumentException.class, () -> policy.termo(false));

    assertTrue(vencido.getMessage().startsWith("ENT-004"));
    assertTrue(repetido.getMessage().startsWith("ENT-012"));
    assertTrue(termo.getMessage().startsWith("ENT-009"));
  }

  @Test
  void shouldRefuseTrainingWithoutDateAndKeepTheWorkerNameInTheTerm() {
    LocalDate hoje = LocalDate.of(2026, 10, 5);
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> policy.ciencia(true, true, null, hoje));

    assertTrue(ex.getMessage().startsWith("ENT-008"));
    policy.ciencia(true, true, hoje, hoje);
    assertTrue(FornecimentoPolicy.textoDoTermo("Ana Souza").contains("Ana Souza"));
    assertEquals(
        List.of("12345", "99999"), policy.cas("12345", List.of("99999"), Set.of("12345", "99999")));
  }
}
