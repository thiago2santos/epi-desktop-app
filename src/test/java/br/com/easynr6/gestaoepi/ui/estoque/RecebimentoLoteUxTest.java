package br.com.easynr6.gestaoepi.ui.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.CaOption;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
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
  void caSelecionadoTrazARazaoSocial() {
    LocalDateTime consulta = LocalDateTime.of(2026, 10, 4, 21, 0);
    assertEquals(
        "Vaqueta SA",
        LotManagementView.fabricanteDoCa(new CaOption(1L, "28941", consulta, "  Vaqueta SA  ")));
    assertEquals("", LotManagementView.fabricanteDoCa(new CaOption(1L, "28941", consulta, " ")));
    assertEquals("", LotManagementView.fabricanteDoCa(null));
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
  void consultaVeAListaSemFormularioDeRecebimento() {
    UsuarioAutenticado consulta =
        new UsuarioAutenticado(1L, "Consulta", "consulta", Set.of(Papel.CONSULTA));
    UsuarioAutenticado sesmt = new UsuarioAutenticado(2L, "Sesmt", "sesmt", Set.of(Papel.SESMT));
    UsuarioAutenticado almox =
        new UsuarioAutenticado(3L, "Almox", "almox", Set.of(Papel.ALMOXARIFE));
    UsuarioAutenticado admin = new UsuarioAutenticado(4L, "Admin", "admin", Set.of(Papel.ADMIN));

    assertFalse(LotManagementView.exibeRecebimento(consulta));
    assertFalse(LotManagementView.exibeRecebimento(sesmt));
    assertTrue(LotManagementView.exibeRecebimento(almox));
    assertTrue(LotManagementView.exibeRecebimento(admin));
    assertEquals("Nenhum lote com esse filtro.", LotManagementView.FILTRO_VAZIO);
    assertEquals(LotManagementView.FILTRO_VAZIO, LotManagementView.textoListaVazia(true));
    assertEquals(LotManagementView.LISTA_VAZIA, LotManagementView.textoListaVazia(false));
    assertTrue(LotManagementView.filtroAtivo("luva", "Todas"));
    assertTrue(LotManagementView.filtroAtivo(" ", "Vencido"));
    assertFalse(LotManagementView.filtroAtivo(" ", "Todas"));
  }

  @Test
  void falhaDeConsultaNaoMostraListaPelaMetadeNemCodigo() {
    assertEquals(
        "Não foi possível consultar os lotes.",
        MensagensEstoque.erroConsulta(new IllegalStateException("SQLITE_BUSY")));
    assertEquals(
        "Você não tem permissão para esta ação.",
        MensagensEstoque.erroConsulta(
            new IllegalArgumentException(
                "AUTH-004 Voce nao tem permissao para executar esta acao.")));
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
