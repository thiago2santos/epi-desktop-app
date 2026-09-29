package br.com.easynr6.gestaoepi.shared.auth;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.identity.application.usecase.AssignRoleUseCase;
import br.com.easynr6.gestaoepi.identity.application.usecase.CreateUserUseCase;
import br.com.easynr6.gestaoepi.identity.application.usecase.DeleteUserUseCase;
import br.com.easynr6.gestaoepi.identity.application.usecase.ListUsersUseCase;
import br.com.easynr6.gestaoepi.identity.application.usecase.RemoveRoleUseCase;
import br.com.easynr6.gestaoepi.identity.application.usecase.ResetCredentialUseCase;
import br.com.easynr6.gestaoepi.identity.application.usecase.SetUserStatusUseCase;
import br.com.easynr6.gestaoepi.identity.application.usecase.UpdateUserUseCase;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class UserAdministrationService {

  private final IdentityRepository identityRepository;
  private final CreateUserUseCase createUserUseCase;
  private final AssignRoleUseCase assignRoleUseCase;
  private final RemoveRoleUseCase removeRoleUseCase;
  private final ResetCredentialUseCase resetCredentialUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final SetUserStatusUseCase setUserStatusUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final ListUsersUseCase listUsersUseCase;

  public UserAdministrationService(
      IdentityRepository identityRepository,
      CreateUserUseCase createUserUseCase,
      AssignRoleUseCase assignRoleUseCase,
      RemoveRoleUseCase removeRoleUseCase,
      ResetCredentialUseCase resetCredentialUseCase,
      UpdateUserUseCase updateUserUseCase,
      SetUserStatusUseCase setUserStatusUseCase,
      DeleteUserUseCase deleteUserUseCase,
      ListUsersUseCase listUsersUseCase) {
    this.identityRepository = identityRepository;
    this.createUserUseCase = createUserUseCase;
    this.assignRoleUseCase = assignRoleUseCase;
    this.removeRoleUseCase = removeRoleUseCase;
    this.resetCredentialUseCase = resetCredentialUseCase;
    this.updateUserUseCase = updateUserUseCase;
    this.setUserStatusUseCase = setUserStatusUseCase;
    this.deleteUserUseCase = deleteUserUseCase;
    this.listUsersUseCase = listUsersUseCase;
  }

  public Long cadastrarUsuario(
      Long adminId,
      String nome,
      String login,
      String senhaInicial,
      boolean ativo,
      Set<Papel> papeis) {
    return createUserUseCase.execute(adminId, nome, login, senhaInicial, ativo, papeis);
  }

  public void atribuirPapel(Long adminId, Long usuarioId, Papel papel) {
    assignRoleUseCase.execute(adminId, usuarioId, papel);
  }

  public void removerPapel(Long adminId, Long usuarioId, Papel papel) {
    removeRoleUseCase.execute(adminId, usuarioId, papel);
  }

  public void resetarCredencial(Long adminId, Long usuarioId, String login, String novaSenha) {
    resetCredentialUseCase.execute(adminId, usuarioId, login, novaSenha);
  }

  public void editarUsuario(
      Long adminId, Long usuarioId, String nome, String login, boolean ativo) {
    updateUserUseCase.execute(adminId, usuarioId, nome, login, ativo);
  }

  public void bloquearUsuario(Long adminId, Long usuarioId) {
    setUserStatusUseCase.block(adminId, usuarioId);
  }

  public void reativarUsuario(Long adminId, Long usuarioId) {
    setUserStatusUseCase.reactivate(adminId, usuarioId);
  }

  public void excluirUsuario(Long adminId, Long usuarioId) {
    deleteUserUseCase.execute(adminId, usuarioId);
  }

  public void atribuirPapelPorLogin(Long adminId, String loginAlvo, Papel papel) {
    UsuarioAdminResumo usuario = buscarUsuarioResumoPorLogin(loginAlvo);
    atribuirPapel(adminId, usuario.id(), papel);
  }

  public void resetarCredencialPorLogin(Long adminId, String loginAlvo, String novaSenha) {
    UsuarioAdminResumo usuario = buscarUsuarioResumoPorLogin(loginAlvo);
    resetarCredencial(adminId, usuario.id(), usuario.login(), novaSenha);
  }

  public void removerPapelPorLogin(Long adminId, String loginAlvo, Papel papel) {
    UsuarioAdminResumo usuario = buscarUsuarioResumoPorLogin(loginAlvo);
    removerPapel(adminId, usuario.id(), papel);
  }

  public void editarUsuarioPorLogin(
      Long adminId, String loginAlvo, String novoNome, String novoLogin, boolean ativo) {
    UsuarioAdminResumo usuario = buscarUsuarioResumoPorLogin(loginAlvo);
    editarUsuario(adminId, usuario.id(), novoNome, novoLogin, ativo);
  }

  public void bloquearUsuarioPorLogin(Long adminId, String loginAlvo) {
    UsuarioAdminResumo usuario = buscarUsuarioResumoPorLogin(loginAlvo);
    bloquearUsuario(adminId, usuario.id());
  }

  public void reativarUsuarioPorLogin(Long adminId, String loginAlvo) {
    UsuarioAdminResumo usuario = buscarUsuarioResumoPorLogin(loginAlvo);
    reativarUsuario(adminId, usuario.id());
  }

  public void excluirUsuarioPorLogin(Long adminId, String loginAlvo) {
    UsuarioAdminResumo usuario = buscarUsuarioResumoPorLogin(loginAlvo);
    excluirUsuario(adminId, usuario.id());
  }

  public List<UsuarioAdminResumo> listarUsuarios() {
    return listUsersUseCase.execute();
  }

  private UsuarioAdminResumo buscarUsuarioResumoPorLogin(String loginAlvo) {
    var user = identityRepository.requireUserByLogin(loginAlvo);
    return new UsuarioAdminResumo(user.id(), user.nome(), user.login(), user.ativo(), false, "-");
  }

  public record UsuarioAdminResumo(
      Long id, String nome, String login, boolean ativo, boolean trocaObrigatoria, String papeis) {}
}
