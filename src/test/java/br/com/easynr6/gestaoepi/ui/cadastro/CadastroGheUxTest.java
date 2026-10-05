package br.com.easynr6.gestaoepi.ui.cadastro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CadastroGheUxTest {

  @Test
  void salvarSoLigaComUnidadeENome() {
    assertFalse(GheManagementView.gheProntoParaSalvar(false, "Ruido"));
    assertFalse(GheManagementView.gheProntoParaSalvar(true, " "));
    assertTrue(GheManagementView.gheProntoParaSalvar(true, "Ruido"));
  }

  @Test
  void cancelarConfirmacaoNaoSegue() {
    assertFalse(GheManagementView.segueAposConfirmacao(false));
    assertTrue(GheManagementView.segueAposConfirmacao(true));
  }

  @Test
  void mensagensExplicamSemExporOCodigo() {
    String outroGrupo =
        MensagensCadastroGhe.erro(
            new IllegalArgumentException("CAD-055 Funcao ja pertence a outro GHE."));

    assertEquals("Esta função já está em outro GHE. Tire-a de lá antes.", outroGrupo);
    assertFalse(outroGrupo.contains("CAD-055"));
    assertEquals(
        "Você não tem permissão para esta ação.",
        MensagensCadastroGhe.erro(
            new IllegalArgumentException(
                "AUTH-004 Voce nao tem permissao para executar esta acao.")));
    assertEquals(GheManagementView.LISTA_VAZIA, "Nenhum GHE nesta unidade.");
  }
}
