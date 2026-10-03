package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.easynr6.gestaoepi.identity.application.port.IdentityRepository;
import br.com.easynr6.gestaoepi.identity.domain.IdentityUser;
import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeAssignment;
import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeSummary;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UserAdministrationService.UsuarioAdminResumo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SetEmployeeStatusUseCaseTest {

  @Test
  void shouldRejectDeactivateWhenEmployeeHasHistoricalDependencies() {
    FakeEmployeeRepository employeeRepository = new FakeEmployeeRepository();
    employeeRepository.existsById = true;
    employeeRepository.hasHistoricalDependencies = true;
    EmployeeAccessAuthorizer accessAuthorizer = new NoOpEmployeeAccessAuthorizer();
    RecordingAuditTrail auditTrail = new RecordingAuditTrail();
    SetEmployeeStatusUseCase useCase =
        new SetEmployeeStatusUseCase(employeeRepository, accessAuthorizer, auditTrail);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(1L, 10L, false));

    assertEquals("CAD-005 Operacao nao permitida por dependencia historica.", ex.getMessage());
    assertEquals(0, employeeRepository.setActiveCalls);
    assertEquals(0, auditTrail.auditCalls);
  }

  private static final class NoOpEmployeeAccessAuthorizer extends EmployeeAccessAuthorizer {
    private NoOpEmployeeAccessAuthorizer() {
      super(new StubIdentityRepository());
    }

    @Override
    public void assertCanManageEmployees(Long actorId) {
      // no-op
    }
  }

  private static final class StubIdentityRepository implements IdentityRepository {
    @Override
    public Optional<IdentityUser> findUserByLogin(String login) {
      return Optional.empty();
    }

    @Override
    public Set<Papel> loadRoles(Long userId) {
      return Set.of(Papel.ADMIN);
    }

    @Override
    public void updateFailedAuthentication(
        Long userId, int invalidAttempts, LocalDateTime blockedUntil) {}

    @Override
    public void clearAuthenticationFailures(Long userId) {}

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
      return 1L;
    }

    @Override
    public boolean userExists(Long userId) {
      return true;
    }

    @Override
    public void assignRole(Long userId, Papel papel) {}

    @Override
    public int removeRole(Long userId, Papel papel) {
      return 0;
    }

    @Override
    public IdentityUser requireUserByLogin(String login) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void updateUser(Long userId, String nome, String login, boolean ativo) {}

    @Override
    public void setUserActive(Long userId, boolean ativo) {}

    @Override
    public int deleteUser(Long userId) {
      return 0;
    }

    @Override
    public void deleteUserRoles(Long userId) {}

    @Override
    public List<UsuarioAdminResumo> listUsers() {
      return List.of();
    }
  }

  private static final class RecordingAuditTrail implements AuditTrail {
    private int auditCalls;

    @Override
    public void registrarEventoCritico(
        Long usuarioId, String acao, String entidade, String entidadeId, String detalhes) {
      auditCalls++;
    }
  }

  private static final class FakeEmployeeRepository implements EmployeeRepository {
    private boolean existsById;
    private boolean hasHistoricalDependencies;
    private int setActiveCalls;

    @Override
    public boolean existsByEmployeeCode(String employeeCode) {
      return false;
    }

    @Override
    public boolean existsById(Long employeeId) {
      return existsById;
    }

    @Override
    public Long create(
        String employeeCode,
        String fullName,
        Long departmentId,
        Long jobRoleId,
        Long managerId,
        boolean active) {
      return 1L;
    }

    @Override
    public void update(
        Long employeeId,
        String fullName,
        Long departmentId,
        Long jobRoleId,
        Long managerId,
        boolean active) {}

    @Override
    public Optional<EmployeeAssignment> findAssignmentById(Long employeeId) {
      return Optional.empty();
    }

    @Override
    public List<EmployeeOption> listActiveByUnit(Long unitId) {
      return List.of();
    }

    @Override
    public void setActive(Long employeeId, boolean active) {
      setActiveCalls++;
    }

    @Override
    public boolean hasHistoricalDependencies(Long employeeId) {
      return hasHistoricalDependencies;
    }

    @Override
    public List<EmployeeSummary> listByTerm(String term) {
      return List.of();
    }
  }
}
