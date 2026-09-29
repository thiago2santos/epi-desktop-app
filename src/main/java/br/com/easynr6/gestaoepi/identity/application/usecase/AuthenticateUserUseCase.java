package br.com.easynr6.gestaoepi.identity.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.Clock;
import br.com.easynr6.gestaoepi.identity.application.port.CredentialHasher;
import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.identity.domain.AuthAttemptPolicy;
import br.com.easynr6.gestaoepi.identity.domain.IdentityUser;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.AuthenticationResult;
import br.com.easynr6.gestaoepi.shared.auth.AuthenticationStatus;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateUserUseCase {

  private final IdentityRepository identityRepository;
  private final CredentialHasher credentialHasher;
  private final Clock clock;
  private final AuditTrail auditTrail;
  private final AuthAttemptPolicy authAttemptPolicy;

  public AuthenticateUserUseCase(
      IdentityRepository identityRepository,
      CredentialHasher credentialHasher,
      Clock clock,
      AuditTrail auditTrail) {
    this.identityRepository = identityRepository;
    this.credentialHasher = credentialHasher;
    this.clock = clock;
    this.auditTrail = auditTrail;
    this.authAttemptPolicy = AuthAttemptPolicy.defaults();
  }

  public AuthenticationResult execute(String login, String senha) {
    String normalizedLogin = login == null ? "" : login.trim();
    Optional<IdentityUser> maybeUser = identityRepository.findUserByLogin(normalizedLogin);
    if (maybeUser.isEmpty()) {
      return AuthenticationResult.denied(AuthenticationStatus.INVALID_CREDENTIAL);
    }

    IdentityUser user = maybeUser.get();
    if (!user.ativo()) {
      return AuthenticationResult.denied(AuthenticationStatus.INACTIVE_USER);
    }

    LocalDateTime now = clock.now();
    if (user.credentialState().isBlockedAt(now)) {
      auditTrail.registrarEventoCritico(
          user.id(),
          "LOGIN_BLOQUEADO",
          "USUARIO",
          String.valueOf(user.id()),
          "Tentativa de login em conta bloqueada");
      return AuthenticationResult.blocked(user.credentialState().blockedUntil());
    }

    String rawCredential = senha == null ? "" : senha;
    if (!credentialHasher.matches(rawCredential, user.credentialHash())) {
      int attempts = user.credentialState().invalidAttempts() + 1;
      if (authAttemptPolicy.shouldBlock(attempts)) {
        LocalDateTime blockedUntil = authAttemptPolicy.blockedUntil(now);
        identityRepository.updateFailedAuthentication(user.id(), 0, blockedUntil);
        auditTrail.registrarEventoCritico(
            user.id(),
            "LOGIN_BLOQUEIO_TEMPORARIO",
            "USUARIO",
            String.valueOf(user.id()),
            "Conta bloqueada por tentativas invalidas consecutivas");
        return AuthenticationResult.blocked(blockedUntil);
      }

      identityRepository.updateFailedAuthentication(user.id(), attempts, null);
      auditTrail.registrarEventoCritico(
          user.id(),
          "LOGIN_FALHA_CREDENCIAL",
          "USUARIO",
          String.valueOf(user.id()),
          "Credencial invalida");
      return AuthenticationResult.denied(AuthenticationStatus.INVALID_CREDENTIAL);
    }

    identityRepository.clearAuthenticationFailures(user.id());
    Set<Papel> roles = identityRepository.loadRoles(user.id());
    if (roles.isEmpty()) {
      return AuthenticationResult.denied(AuthenticationStatus.NO_ROLE);
    }

    UsuarioAutenticado authenticated =
        new UsuarioAutenticado(user.id(), user.nome(), user.login(), EnumSet.copyOf(roles));
    if (user.credentialState().forcePasswordChange()) {
      return AuthenticationResult.forcePasswordChange(authenticated);
    }
    return AuthenticationResult.success(authenticated);
  }
}
