package br.com.easynr6.gestaoepi.ui.cadastro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CadastroUnidadeUxTest {

  @Test
  void unidadeNovaExigeNomeECnpj() {
    assertFalse(UnitManagementView.unidadeProntaParaSalvar(true, "Lagoa Santa", " "));
    assertFalse(UnitManagementView.unidadeProntaParaSalvar(true, " ", "22755266000500"));
    assertTrue(
        UnitManagementView.unidadeProntaParaSalvar(true, "Lagoa Santa", "22.755.266/0005-00"));
  }

  @Test
  void edicaoExigeSoONome() {
    assertTrue(UnitManagementView.unidadeProntaParaSalvar(false, "Lagoa Santa", ""));
    assertFalse(UnitManagementView.unidadeProntaParaSalvar(false, " ", "22755266000500"));
  }

  @Test
  void cancelarInativacaoNaoSegue() {
    assertFalse(UnitManagementView.segueAposConfirmacao(false));
    assertTrue(UnitManagementView.segueAposConfirmacao(true));
  }

  @Test
  void mensagensExplicamSemExporOCodigo() {
    String bloqueio =
        MensagensCadastroUnidade.erro(
            new IllegalArgumentException(
                "CAD-045 Inativacao recusada: a unidade ainda tem setor ativo."));

    assertEquals("Não é possível inativar: esta unidade ainda tem setor ativo.", bloqueio);
    assertFalse(bloqueio.contains("CAD-045"));
    assertEquals(
        "Já existe uma unidade com esse CNPJ.",
        MensagensCadastroUnidade.erro(new IllegalArgumentException("CAD-043 CNPJ ja cadastrado.")));
    assertEquals(
        "CNPJ inválido. Use os 14 dígitos, com ou sem máscara.",
        MensagensCadastroUnidade.erro(new IllegalArgumentException("CAD-042 CNPJ invalido.")));
  }
}
