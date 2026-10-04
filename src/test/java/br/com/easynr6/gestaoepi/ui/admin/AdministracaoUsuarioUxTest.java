package br.com.easynr6.gestaoepi.ui.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdministracaoUsuarioUxTest {

  @Test
  void usuarioNovoExigeSenhaEPapel() {
    assertFalse(UserAdministrationView.usuarioProntoParaSalvar(true, "Ana", "ana", "", true));
    assertFalse(
        UserAdministrationView.usuarioProntoParaSalvar(true, "Ana", "ana", "Senha#Forte1", false));
    assertFalse(
        UserAdministrationView.usuarioProntoParaSalvar(true, " ", "ana", "Senha#Forte1", true));
    assertTrue(
        UserAdministrationView.usuarioProntoParaSalvar(true, "Ana", "ana", "Senha#Forte1", true));
  }

  @Test
  void edicaoNaoExigeSenha() {
    assertTrue(UserAdministrationView.usuarioProntoParaSalvar(false, "Ana", "ana", "", true));
  }

  @Test
  void filtroEncontraNomeLoginOuPapel() {
    assertTrue(UserAdministrationView.usuarioCombina("ana", "Ana Lima", "a.lima", "SESMT"));
    assertTrue(UserAdministrationView.usuarioCombina("SESMT", "Ana Lima", "a.lima", "SESMT"));
    assertFalse(UserAdministrationView.usuarioCombina("paulo", "Ana Lima", "a.lima", "SESMT"));
    assertTrue(UserAdministrationView.usuarioCombina("  ", "Ana Lima", "a.lima", "SESMT"));
  }

  @Test
  void papeisDaListaViraramEnum() {
    assertEquals(
        List.of(Papel.ADMIN, Papel.SESMT), UserAdministrationView.papeisAtuais("ADMIN, SESMT"));
    assertEquals(List.of(), UserAdministrationView.papeisAtuais("-"));
    assertEquals(List.of(), UserAdministrationView.papeisAtuais(null));
  }

  @Test
  void mensagensExplicamSemExporOCodigo() {
    assertEquals(
        "Já existe um usuário com esse login.",
        MensagensAdministracaoUsuario.erro(
            new IllegalArgumentException("AUTH-005 Login ja utilizado.")));
    assertEquals(
        "A senha precisa de 12 caracteres, com pelo menos três entre maiúscula, minúscula, número e símbolo, e não pode conter o login.",
        MensagensAdministracaoUsuario.erro(
            new IllegalArgumentException(
                "AUTH-006 Credencial fora da politica minima de seguranca.")));
    assertEquals(
        "Você não tem permissão para esta ação.",
        MensagensAdministracaoUsuario.erro(
            new IllegalArgumentException(
                "AUTH-004 Voce nao tem permissao para executar esta acao.")));
  }
}
