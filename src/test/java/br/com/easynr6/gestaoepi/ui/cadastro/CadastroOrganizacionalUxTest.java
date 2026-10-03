package br.com.easynr6.gestaoepi.ui.cadastro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CadastroOrganizacionalUxTest {

  @Test
  void setorSoSalvaComUnidadeENome() {
    assertFalse(OrgStructureManagementView.setorProntoParaSalvar(false, "Guarda"));
    assertFalse(OrgStructureManagementView.setorProntoParaSalvar(true, "  "));
    assertTrue(OrgStructureManagementView.setorProntoParaSalvar(true, "Guarda"));
  }

  @Test
  void funcaoSoSalvaComSetorENome() {
    assertFalse(OrgStructureManagementView.funcaoProntaParaSalvar(false, "Auxiliar"));
    assertFalse(OrgStructureManagementView.funcaoProntaParaSalvar(true, ""));
    assertTrue(OrgStructureManagementView.funcaoProntaParaSalvar(true, "Auxiliar de guarda"));
  }

  @Test
  void unidadeRepeteONomeESeDistinguePeloCnpj() {
    assertEquals(
        "Itupeva · 22.755.266/0002-68",
        OrgStructureManagementView.rotuloUnidade("Itupeva", "22755266000268"));
    assertEquals(
        "Belo Horizonte · 22.755.266/0007-72",
        OrgStructureManagementView.rotuloUnidade("Belo Horizonte", "22755266000772"));
  }

  @Test
  void mensagensExplicamSemExporOCodigo() {
    assertEquals(
        "Já existe um setor com esse nome nesta unidade.",
        MensagensCadastroOrganizacional.erro(
            new IllegalArgumentException("CAD-021 Nome de setor ja existente.")));
    assertEquals(
        "Não é possível inativar: este setor ainda tem função ativa.",
        MensagensCadastroOrganizacional.erro(
            new IllegalArgumentException(
                "CAD-024 Operacao nao permitida por dependencia historica.")));
    assertEquals(
        "Escolha uma unidade ativa.",
        MensagensCadastroOrganizacional.erro(
            new IllegalArgumentException("CAD-027 Unidade invalida ou inativa.")));
    assertEquals(
        "Não é possível inativar: esta função ainda tem trabalhador ativo.",
        MensagensCadastroOrganizacional.erro(
            new IllegalArgumentException(
                "CAD-028 Operacao nao permitida por dependencia historica.")));
  }
}
