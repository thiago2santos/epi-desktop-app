package br.com.easynr6.gestaoepi.ui.cadastro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.ui.cadastro.PlanoCadastroTrabalhador.Passo;
import java.util.List;
import org.junit.jupiter.api.Test;

class CadastroTrabalhadorUxTest {

  @Test
  void novoTrabalhadorSoCriaMesmoInativo() {
    PlanoCadastroTrabalhador plano = PlanoCadastroTrabalhador.de(true, true, false, true);

    assertFalse(plano.confirmarInativacao());
    assertEquals(List.of(Passo.CRIAR), plano.passos());
  }

  @Test
  void inativarPedeConfirmacaoENaoMisturaComAEdicao() {
    PlanoCadastroTrabalhador soStatus = PlanoCadastroTrabalhador.de(false, true, false, false);
    PlanoCadastroTrabalhador comDados = PlanoCadastroTrabalhador.de(false, true, false, true);

    assertTrue(soStatus.confirmarInativacao());
    assertEquals(List.of(Passo.INATIVAR), soStatus.passos());
    assertEquals(List.of(Passo.ATUALIZAR, Passo.INATIVAR), comDados.passos());
  }

  @Test
  void reativarEEditarSaoPassosSeparados() {
    assertEquals(
        List.of(Passo.REATIVAR), PlanoCadastroTrabalhador.de(false, false, true, false).passos());
    assertEquals(
        List.of(Passo.ATUALIZAR, Passo.REATIVAR),
        PlanoCadastroTrabalhador.de(false, false, true, true).passos());
    assertEquals(
        List.of(Passo.ATUALIZAR), PlanoCadastroTrabalhador.de(false, true, true, true).passos());
  }

  @Test
  void salvarSoLigaComLinhaOuCadastroCompleto() {
    assertFalse(EmployeeManagementView.cadastroProntoParaSalvar("", "Ana", true, true));
    assertFalse(EmployeeManagementView.cadastroProntoParaSalvar("4418", "  ", true, true));
    assertFalse(EmployeeManagementView.cadastroProntoParaSalvar("4418", "Ana", false, true));
    assertFalse(EmployeeManagementView.cadastroProntoParaSalvar("4418", "Ana", true, false));
    assertTrue(EmployeeManagementView.cadastroProntoParaSalvar("4418", "Ana", true, true));
  }

  @Test
  void semMudancaNaoHaOQueSalvar() {
    PlanoCadastroTrabalhador plano = PlanoCadastroTrabalhador.de(false, false, false, false);

    assertTrue(plano.vazio());
    assertFalse(plano.confirmarInativacao());
  }

  @Test
  void mensagensMantemOCodigoDoCasoDeUso() {
    assertEquals(
        "CAD-001 Matrícula já cadastrada.",
        MensagensCadastroTrabalhador.erro(
            new IllegalArgumentException("CAD-001 Matricula ja existente.")));
    assertEquals(
        "CAD-005 Não é possível inativar: o histórico deste trabalhador impede a operação.",
        MensagensCadastroTrabalhador.erro(
            new IllegalArgumentException(
                "CAD-005 Operacao nao permitida por dependencia historica.")));
    assertEquals(
        "CAD-007 Escolha um gestor ativo da mesma unidade, ou deixe sem gestor.",
        MensagensCadastroTrabalhador.erro(
            new IllegalArgumentException("CAD-007 Gestor invalido para este trabalhador.")));
    assertEquals(
        "Não foi possível salvar o trabalhador.",
        MensagensCadastroTrabalhador.erro(new IllegalStateException("sql")));
  }
}
