package br.com.easynr6.gestaoepi.identity.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.identity.application.port.CredentialHasher;
import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.identity.domain.CredentialState;
import br.com.easynr6.gestaoepi.identity.domain.IdentityUser;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.AuthenticationResult;
import br.com.easynr6.gestaoepi.shared.auth.AuthenticationStatus;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UserAdministrationService.UsuarioAdminResumo;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AuthenticateUserUseCaseTest {

  @Test
  void deveRetornarContaBloqueadaComClockInjetavel() {
    LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0);
    LocalDateTime blockedUntil = now.plusMinutes(10);
    StubIdentityRepository repository =
        new StubIdentityRepository(
            new IdentityUser(
                10L,
                "Operador",
                "operador",
                "hash-ok",
                true,
                new CredentialState(false, 0, blockedUntil)),
            EnumSet.of(Papel.SESMT));
    RecordingAuditTrail auditTrail = new RecordingAuditTrail();
    AuthenticateUserUseCase useCase =
        new AuthenticateUserUseCase(repository, new StubHasher(), () -> now, auditTrail);

    AuthenticationResult result = useCase.execute("operador", "SenhaCorreta#2026");

    assertEquals(AuthenticationStatus.BLOCKED, result.status());
    assertEquals(blockedUntil, result.bloqueadoAte());
    assertEquals("LOGIN_BLOQUEADO", auditTrail.lastAction);
  }

  @Test
  void deveAplicarBloqueioAoAtingirLimiteDeTentativas() {
    LocalDateTime now = LocalDateTime.of(2026, 1, 1, 11, 0);
    StubIdentityRepository repository =
        new StubIdentityRepository(
            new IdentityUser(
                20L, "Operador", "operador", "hash-ok", true, new CredentialState(false, 4, null)),
            EnumSet.of(Papel.ALMOXARIFE));
    RecordingAuditTrail auditTrail = new RecordingAuditTrail();
    AuthenticateUserUseCase useCase =
        new AuthenticateUserUseCase(repository, new StubHasher(), () -> now, auditTrail);

    AuthenticationResult result = useCase.execute("operador", "senha-errada");

    assertEquals(AuthenticationStatus.BLOCKED, result.status());
    assertNotNull(result.bloqueadoAte());
    assertEquals(0, repository.lastInvalidAttempts);
    assertEquals("LOGIN_BLOQUEIO_TEMPORARIO", auditTrail.lastAction);
  }

  @Test
  void deveRetornarForcePasswordChangeQuandoFlagAtiva() {
    StubIdentityRepository repository =
        new StubIdentityRepository(
            new IdentityUser(
                30L, "Operador", "operador", "hash-ok", true, new CredentialState(true, 0, null)),
            EnumSet.of(Papel.CONSULTA));
    AuthenticateUserUseCase useCase =
        new AuthenticateUserUseCase(
            repository, new StubHasher(), LocalDateTime::now, new RecordingAuditTrail());

    AuthenticationResult result = useCase.execute("operador", "SenhaCorreta#2026");

    assertEquals(AuthenticationStatus.FORCE_PASSWORD_CHANGE, result.status());
    assertNotNull(result.usuario());
  }

  @Test
  void deveRecusarUsuarioSemPapelMesmoComSenhaCorreta() {
    StubIdentityRepository repository =
        new StubIdentityRepository(
            new IdentityUser(
                40L, "Operador", "operador", "hash-ok", true, new CredentialState(false, 0, null)),
            EnumSet.noneOf(Papel.class));
    AuthenticateUserUseCase useCase =
        new AuthenticateUserUseCase(
            repository, new StubHasher(), LocalDateTime::now, new RecordingAuditTrail());

    AuthenticationResult result = useCase.execute("operador", "SenhaCorreta#2026");

    assertEquals(AuthenticationStatus.NO_ROLE, result.status());
    assertNull(result.usuario());
    assertTrue(repository.clearedFailures);
  }

  private static class StubHasher implements CredentialHasher {

    @Override
    public String encode(String rawCredential) {
      return "hash-ok";
    }

    @Override
    public boolean matches(String rawCredential, String encodedCredential) {
      return "SenhaCorreta#2026".equals(rawCredential) && "hash-ok".equals(encodedCredential);
    }
  }

  private static class RecordingAuditTrail implements AuditTrail {
    String lastAction;

    @Override
    public void registrarEventoCritico(
        Long usuarioId, String acao, String entidade, String entidadeId, String detalhes) {
      this.lastAction = acao;
    }
  }

  private static class StubIdentityRepository implements IdentityRepository {
    private final IdentityUser user;
    private final Set<Papel> roles;
    boolean clearedFailures;
    int lastInvalidAttempts = -1;

    StubIdentityRepository(IdentityUser user, Set<Papel> roles) {
      this.user = user;
      this.roles = roles;
    }

    @Override
    public Optional<IdentityUser> findUserByLogin(String login) {
      if (user != null && user.login().equals(login)) {
        return Optional.of(user);
      }
      return Optional.empty();
    }

    @Override
    public Set<Papel> loadRoles(Long userId) {
      return roles;
    }

    @Override
    public void updateFailedAuthentication(
        Long userId, int invalidAttempts, LocalDateTime blockedUntil) {
      this.lastInvalidAttempts = invalidAttempts;
    }

    @Override
    public void clearAuthenticationFailures(Long userId) {
      this.clearedFailures = true;
    }

    @Override
    public void updateCredential(
        Long userId,
        String credentialHash,
        boolean forcePasswordChange,
        int invalidAttempts,
        LocalDateTime blockedUntil,
        boolean updateCredentialTimestamp) {}

    @Override
    public Long createUser(
        String nome,
        String login,
        String credentialHash,
        boolean ativo,
        boolean forcePasswordChange) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean userExists(Long userId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void assignRole(Long userId, Papel papel) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int removeRole(Long userId, Papel papel) {
      throw new UnsupportedOperationException();
    }

    @Override
    public IdentityUser requireUserByLogin(String login) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void updateUser(Long userId, String nome, String login, boolean ativo) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setUserActive(Long userId, boolean ativo) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int deleteUser(Long userId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void deleteUserRoles(Long userId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<UsuarioAdminResumo> listUsers() {
      throw new UnsupportedOperationException();
    }
  }
}
