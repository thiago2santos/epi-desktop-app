package br.com.easynr6.gestaoepi.identity.application.port;

import br.com.easynr6.gestaoepi.identity.domain.IdentityUser;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UserAdministrationService.UsuarioAdminResumo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IdentityRepository {

  Optional<IdentityUser> findUserByLogin(String login);

  Set<Papel> loadRoles(Long userId);

  void updateFailedAuthentication(Long userId, int invalidAttempts, LocalDateTime blockedUntil);

  void clearAuthenticationFailures(Long userId);

  void updateCredential(
      Long userId,
      String credentialHash,
      boolean forcePasswordChange,
      int invalidAttempts,
      LocalDateTime blockedUntil,
      boolean updateCredentialTimestamp);

  Long createUser(
      String nome, String login, String credentialHash, boolean ativo, boolean forcePasswordChange);

  boolean userExists(Long userId);

  void assignRole(Long userId, Papel papel);

  int removeRole(Long userId, Papel papel);

  IdentityUser requireUserByLogin(String login);

  void updateUser(Long userId, String nome, String login, boolean ativo);

  void setUserActive(Long userId, boolean ativo);

  int deleteUser(Long userId);

  void deleteUserRoles(Long userId);

  List<UsuarioAdminResumo> listUsers();
}
