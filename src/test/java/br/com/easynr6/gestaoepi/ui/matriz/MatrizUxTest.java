package br.com.easynr6.gestaoepi.ui.matriz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MatrizUxTest {

  @Test
  void incluirSoLigaComPerfilEEpi() {
    assertFalse(MatrizManagementView.matrizProntaParaIncluir(false, true));
    assertFalse(MatrizManagementView.matrizProntaParaIncluir(true, false));
    assertTrue(MatrizManagementView.matrizProntaParaIncluir(true, true));
  }

  @Test
  void cancelarConfirmacaoNaoSegue() {
    assertFalse(MatrizManagementView.segueAposConfirmacao(false));
    assertTrue(MatrizManagementView.segueAposConfirmacao(true));
  }

  @Test
  void mensagensExplicamSemExporOCodigo() {
    String duplicado =
        MensagensMatriz.erro(
            new IllegalArgumentException("MAT-002 Este EPI ja esta na matriz deste perfil."));
    String grupo =
        MensagensMatriz.erro(new IllegalArgumentException("MAT-008 Funcao membro de GHE ativo."));

    assertEquals("Este EPI já está na matriz deste perfil.", duplicado);
    assertEquals("Esta função usa a lista do GHE. Edite o grupo.", grupo);
    assertFalse(duplicado.contains("MAT-002"));
    assertFalse(grupo.contains("MAT-008"));
    assertEquals(MatrizManagementView.LISTA_VAZIA, "Nenhum EPI na matriz deste perfil.");
    assertEquals(
        "Você não tem permissão para alterar a matriz.",
        MensagensMatriz.erro(
            new IllegalArgumentException(
                "AUTH-004 Voce nao tem permissao para executar esta acao.")));
  }
}
