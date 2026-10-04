package br.com.easynr6.gestaoepi.ui.cadastro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CadastroEpiUxTest {

  @Test
  void epiSoSalvaComDescricaoGrupoEFabricante() {
    assertFalse(EpiManagementView.epiProntoParaSalvar("  ", AnnexGroup.F, "Vaqueta"));
    assertFalse(EpiManagementView.epiProntoParaSalvar("Luva", null, "Vaqueta"));
    assertFalse(EpiManagementView.epiProntoParaSalvar("Luva", AnnexGroup.F, ""));
    assertTrue(EpiManagementView.epiProntoParaSalvar("Luva", AnnexGroup.F, "Vaqueta"));
  }

  @Test
  void grupoDoAnexoExplicaAProtecao() {
    assertEquals("F · Membros superiores", EpiManagementView.rotuloAnexo(AnnexGroup.F));
    assertEquals("A · Cabeça", EpiManagementView.rotuloAnexo(AnnexGroup.A));
  }

  @Test
  void vinculoSoSalvaComEpiCaSituacaoConsultaEEvidencia() {
    assertFalse(
        CaBindingManagementView.vinculoProntoParaSalvar(
            false, "28941", CaStatus.ACTIVE, "28/09/2026 03:12", "Carga"));
    assertFalse(
        CaBindingManagementView.vinculoProntoParaSalvar(
            true, " ", CaStatus.ACTIVE, "28/09/2026 03:12", "Carga"));
    assertFalse(
        CaBindingManagementView.vinculoProntoParaSalvar(
            true, "28941", null, "28/09/2026 03:12", "Carga"));
    assertFalse(
        CaBindingManagementView.vinculoProntoParaSalvar(
            true, "28941", CaStatus.ACTIVE, "", "Carga"));
    assertTrue(
        CaBindingManagementView.vinculoProntoParaSalvar(
            true, "28941", CaStatus.ACTIVE, "28/09/2026 03:12", "Carga CAEPI"));
  }

  @Test
  void consultaOficialAceitaOFormatoDaTelaEOisoDoBanco() {
    assertEquals(
        LocalDateTime.of(2026, 9, 28, 3, 12),
        CaBindingManagementView.lerConsulta("28/09/2026 03:12"));
    assertEquals(
        LocalDateTime.of(2026, 9, 28, 3, 12),
        CaBindingManagementView.lerConsulta("2026-09-28T03:12"));
    assertNull(CaBindingManagementView.lerConsulta("28/09/2026"));
    assertEquals("28/09/2026 03:12", CaBindingManagementView.formatarConsulta("2026-09-28T03:12"));
  }

  @Test
  void vigenciaAceitaIsoEFormatoDaTela() {
    assertEquals(LocalDate.of(2026, 3, 12), CaBindingManagementView.lerData("2026-03-12"));
    assertEquals(LocalDate.of(2026, 3, 12), CaBindingManagementView.lerData("12/03/2026"));
    assertEquals("12/03/2026", CaBindingManagementView.formatarData("2026-03-12"));
  }

  @Test
  void mensagensExplicamSemExporOCodigo() {
    assertEquals(
        "Já existe um EPI com essa descrição, grupo e fabricante.",
        MensagensCadastroEpi.erro(
            new IllegalArgumentException("CAD-033 EPI duplicado no escopo definido.")));
    assertEquals(
        "Sem a base, anexe o print da consulta online e informe data e evidência.",
        MensagensCadastroEpi.erro(
            new IllegalArgumentException("CAD-037 Evidencia de consulta oficial do CA ausente.")));
    assertEquals(
        "Esta situação do CA não permite deixar o vínculo ativo.",
        MensagensCadastroEpi.erro(
            new IllegalArgumentException("CAD-036 Situacao do CA impede ativacao do vinculo.")));
    assertEquals(
        "Não é possível alterar o status: confira o CA ativo e os vínculos deste EPI.",
        MensagensCadastroEpi.erro(
            new IllegalArgumentException(
                "CAD-038 Operacao nao permitida por dependencia historica.")));
  }
}
