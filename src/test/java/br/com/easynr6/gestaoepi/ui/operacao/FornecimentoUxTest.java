package br.com.easynr6.gestaoepi.ui.operacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.modules.issuance.application.port.FornecimentoRepository.Pagina;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class FornecimentoUxTest {

  @Test
  void shouldNotPersistWhenTheDialogReturns() {
    AtomicBoolean gravou = new AtomicBoolean();

    ConfirmacaoFornecimento.seguir(false, () -> gravou.set(true));

    assertFalse(gravou.get());
    ConfirmacaoFornecimento.seguir(true, () -> gravou.set(true));
    assertTrue(gravou.get());
  }

  @Test
  void shouldHideRuleCodesAndParseQuantity() {
    String texto =
        MensagensFornecimento.erro(
            new IllegalArgumentException("ENT-004 Escolha um lote vigente deste EPI."));

    assertFalse(texto.contains("ENT-"));
    assertTrue(texto.contains("Peça vencida"));
    assertTrue(MensagensFornecimento.CONFIRMAR.contains("não poderá ser editada"));
    assertNull(FornecimentoWizardView.quantidade("0"));
    assertEquals(2, FornecimentoWizardView.quantidade("2"));
    assertTrue(MensagensFornecimento.sucesso(15).startsWith("Fornecimento registrado."));
    assertTrue(MensagensFornecimento.DICA_BUSCA.contains("3 caracteres"));
    assertTrue(MensagensFornecimento.DICA_SETOR.contains("setor"));
    assertTrue(MensagensFornecimento.DICA_ATIVOS.contains("inativo"));
    assertTrue(MensagensFornecimento.DICA_FORA.contains("matriz"));
    assertTrue(MensagensFornecimento.DICA_EPIS.contains("Sim"));
    assertTrue(MensagensFornecimento.FUNCAO_SEM_EPI.contains("função"));
    assertTrue(MensagensFornecimento.FORA_DA_MATRIZ.contains("SESMT"));
    assertTrue(MensagensFornecimento.PRIMEIRA_BLOQUEADA.contains("Primeira entrega"));
    assertTrue(MensagensFornecimento.DICA_LOTE.contains("unidade"));
    assertTrue(MensagensFornecimento.DICA_MOTIVO.contains("saindo"));
    assertFalse(MensagensFornecimento.LOTE_VENCIDO.contains("ENT-"));
    assertFalse(MensagensFornecimento.FORA_DA_MATRIZ.contains("ENT-"));
    assertTrue(MensagensFornecimento.TRABALHADOR_INATIVO.contains("inativo"));
    assertFalse(MensagensFornecimento.TRABALHADOR_INATIVO.contains("ENT-"));
    assertTrue(MensagensFornecimento.DICA_SENHA.contains("SESMT"));
    assertFalse(MensagensFornecimento.DICA_SENHA.contains("ENT-"));
    assertFalse(
        MensagensFornecimento.erro(new IllegalArgumentException("ENT-014 sem hash"))
            .contains("ENT-"));
    assertTrue(
        MensagensFornecimento.erro(new IllegalArgumentException("ENT-014 sem hash"))
            .contains("senha de recebimento"));
    assertEquals(
        MensagensFornecimento.SENHA_NAO_CONFERE,
        MensagensFornecimento.erro(new IllegalArgumentException("ENT-015 nao confere")));
    assertTrue(MensagensFornecimento.CIENCIA_CAIXA.startsWith("Eu recebi"));
    assertTrue(MensagensFornecimento.ORIENTACAO.contains("orientação de uso"));
    assertTrue(MensagensFornecimento.DICA_CIENCIA.contains("ajuste"));
    assertTrue(MensagensFornecimento.DICA_TREINAMENTO.contains("posterior a hoje"));
    assertEquals("Revisão", MensagensFornecimento.REVISAO);
    assertEquals("Ficha de entrega de EPI", MensagensFornecimento.FICHA);
    assertEquals("Termo de responsabilidade", MensagensFornecimento.TERMO_TITULO);
    assertTrue(MensagensFornecimento.ORIENTACAO_REGISTRADA.contains("Orientação de uso"));
    assertEquals("Termo aceito.", MensagensFornecimento.TERMO_ACEITO);
    assertTrue(MensagensFornecimento.CONSEQUENCIA.contains("sai do disponível"));
    assertFalse(MensagensFornecimento.CONSEQUENCIA.contains("ENT-"));
    assertFalse(MensagensFornecimento.ORIENTACAO.contains("ENT-"));
    assertEquals(
        MensagensFornecimento.ORIENTACAO,
        MensagensFornecimento.erro(
            new IllegalArgumentException("ENT-008 Registre a orientacao de uso.")));
    assertEquals(1, new Pagina(List.of(), 0, 0, 10).paginas());
    assertEquals(2, new Pagina(List.of(), 11, 0, 10).paginas());
  }
}
