package br.com.easynr6.gestaoepi.modules.issuance.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertThrows(
            IllegalArgumentException.class,
            () -> policy.conferirSenhaRecebimento(null, true, true));

    assertTrue(vencido.getMessage().startsWith("ENT-004"));
    assertTrue(repetido.getMessage().startsWith("ENT-012"));
    assertTrue(termo.getMessage().startsWith("ENT-009"));
    IllegalArgumentException semHash =
        assertThrows(
            IllegalArgumentException.class,
            () -> policy.conferirSenhaRecebimento("capa-77", false, false));
    IllegalArgumentException errada =
        assertThrows(
            IllegalArgumentException.class,
            () -> policy.conferirSenhaRecebimento("errada", true, false));
    assertTrue(semHash.getMessage().startsWith("ENT-014"));
    assertTrue(errada.getMessage().startsWith("ENT-015"));
    policy.conferirSenhaRecebimento("capa-77", true, true);
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
        List.of("12345", "99999"),
        policy.cas("12345", false, List.of("99999"), Set.of("12345", "99999")));
    assertEquals(List.of("13398"), policy.cas("13398", true, List.of(), Set.of("11013")));
  }

  @Test
  void shouldLimitTheReasonToWhatThePreviousDeliveryAllows() {
    assertFalse(policy.motivoPermitido(MotivoFornecimento.PRIMEIRA_ENTREGA, false, true, false));
    assertTrue(policy.motivoPermitido(MotivoFornecimento.TROCA_PERIODICA, false, true, false));
    assertTrue(policy.motivoPermitido(MotivoFornecimento.DANO, false, true, false));
    assertFalse(policy.motivoPermitido(MotivoFornecimento.EXTRAVIO, false, false, false));
    assertFalse(policy.motivoPermitido(MotivoFornecimento.MUDANCA_FUNCAO, false, true, false));
    assertTrue(policy.motivoPermitido(MotivoFornecimento.MUDANCA_FUNCAO, false, true, true));
    assertTrue(policy.motivoPermitido(MotivoFornecimento.PRIMEIRA_ENTREGA, true, true, false));
    assertTrue(policy.motivoPermitido(MotivoFornecimento.OUTRO, false, true, false));
    assertFalse(policy.funcaoMudou("Soldador", "Soldador"));
    assertFalse(policy.funcaoMudou("Soldador", null));
    assertTrue(policy.funcaoMudou("Soldador", "Operador"));
    assertEquals(
        MotivoFornecimento.TROCA_PERIODICA,
        policy.sugerirMotivo(MotivoFornecimento.PRIMEIRA_ENTREGA, false, true, false));
    assertEquals(
        MotivoFornecimento.DANO, policy.sugerirMotivo(MotivoFornecimento.DANO, false, true, false));
  }
}
