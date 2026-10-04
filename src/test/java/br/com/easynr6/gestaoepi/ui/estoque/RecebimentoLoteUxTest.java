package br.com.easynr6.gestaoepi.ui.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RecebimentoLoteUxTest {

  @Test
  void salvarSoLigaComOsObrigatorios() {
    LocalDate validade = LocalDate.of(2027, 3, 15);
    assertFalse(LotManagementView.loteProntoParaSalvar(true, true, true, " ", validade, "10"));
    assertFalse(LotManagementView.loteProntoParaSalvar(true, true, false, "VG-1", validade, "10"));
    assertFalse(LotManagementView.loteProntoParaSalvar(true, true, true, "VG-1", null, "10"));
    assertTrue(LotManagementView.loteProntoParaSalvar(true, true, true, "VG-1", validade, "10"));
  }

  @Test
  void mesmoDiaAindaEstaNoPrazo() {
    LocalDate hoje = LocalDate.of(2026, 10, 4);
    assertFalse(LotManagementView.pecaVencida(hoje, hoje));
    assertTrue(LotManagementView.pecaVencida(hoje.minusDays(1), hoje));
  }

  @Test
  void cancelarPecaVencidaNaoSegue() {
    assertFalse(LotManagementView.segueAposConfirmacao(false));
    assertTrue(LotManagementView.segueAposConfirmacao(true));
  }

  @Test
  void listaVaziaDizOQueFaltaEOfereceORecebimento() {
    assertEquals("Nenhum lote recebido nesta unidade.", LotManagementView.LISTA_VAZIA);
    assertEquals("Registrar recebimento", LotManagementView.ACAO_REGISTRAR);
    assertEquals("Único", LotManagementView.textoTamanho(" "));
  }

  @Test
  void consultaAnteriorAHojeAvisaSemBloquearOTexto() {
    LocalDate hoje = LocalDate.of(2026, 10, 4);
    assertTrue(LotManagementView.consultaAntiga(hoje.minusDays(1), hoje));
    assertFalse(LotManagementView.consultaAntiga(hoje, hoje));
    assertEquals(
        "A consulta deste CA não é de hoje. Confira o número impresso na peça.",
        LotManagementView.AVISO_CONSULTA);
  }

  @Test
  void duplicidadeExplicaSemMostrarOCodigo() {
    String texto =
        MensagensEstoque.erro(
            new IllegalArgumentException(
                "LOT-003 Ja existe este lote para este EPI nesta unidade."));

    assertEquals("Já existe este lote para este EPI nesta unidade.", texto);
    assertFalse(texto.contains("LOT-003"));
  }
}
