package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetEmployeeStatusUseCase {

  private final EmployeeRepository employeeRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;

  public SetEmployeeStatusUseCase(
      EmployeeRepository employeeRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.employeeRepository = employeeRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @Transactional
  public void execute(Long actorId, Long employeeId, boolean active) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (employeeId == null || !employeeRepository.existsById(employeeId)) {
      throw new IllegalArgumentException("EMP-006 Employee target not found.");
    }
    employeeRepository.setActive(employeeId, active);
    auditTrail.registrarEventoCritico(
        actorId,
        active ? "EMPLOYEE_REACTIVATED" : "EMPLOYEE_DEACTIVATED",
        "EMPLOYEE",
        String.valueOf(employeeId),
        active ? "Employee reactivated." : "Employee deactivated.");
  }
}
