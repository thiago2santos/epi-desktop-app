package br.com.easynr6.gestaoepi.modules.matrix.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.usecase.EmployeeAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository.LinhaPeriodicidade;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListarPeriodicidadeUseCase {

  private final PeriodicidadeRepository periodicidadeRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;

  public ListarPeriodicidadeUseCase(
      PeriodicidadeRepository periodicidadeRepository, EmployeeAccessAuthorizer accessAuthorizer) {
    this.periodicidadeRepository = periodicidadeRepository;
    this.accessAuthorizer = accessAuthorizer;
  }

  public List<LinhaPeriodicidade> execute(Long actorId) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    return periodicidadeRepository.listar();
  }
}
