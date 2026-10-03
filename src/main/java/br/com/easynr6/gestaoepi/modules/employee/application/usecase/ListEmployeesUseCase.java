package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository;
import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListEmployeesUseCase {

  private final EmployeeRepository employeeRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;

  public ListEmployeesUseCase(
      EmployeeRepository employeeRepository, EmployeeAccessAuthorizer accessAuthorizer) {
    this.employeeRepository = employeeRepository;
    this.accessAuthorizer = accessAuthorizer;
  }

  public List<EmployeeSummary> execute(Long actorId, String term) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    return employeeRepository.listByTerm(term);
  }

  public List<EmployeeOption> listActiveByUnit(Long unitId) {
    return employeeRepository.listActiveByUnit(unitId);
  }
}
