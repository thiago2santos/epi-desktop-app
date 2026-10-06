package br.com.easynr6.gestaoepi.modules.issuance.application.usecase;

import br.com.easynr6.gestaoepi.modules.issuance.application.PendenciaAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.PendenciaRepository;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.PendenciaRepository.Pendencia;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConsultarPendenciaUseCase {

  private final PendenciaRepository pendenciaRepository;
  private final PendenciaAccessAuthorizer accessAuthorizer;

  public ConsultarPendenciaUseCase(
      PendenciaRepository pendenciaRepository, PendenciaAccessAuthorizer accessAuthorizer) {
    this.pendenciaRepository = pendenciaRepository;
    this.accessAuthorizer = accessAuthorizer;
  }

  public List<Pendencia> listar(
      Long actorId, Long unitId, LocalDate inicio, LocalDate fim, Long employeeId) {
    accessAuthorizer.assertCanRead(actorId);
    return pendenciaRepository.listar(unitId, inicio, fim, employeeId);
  }
}
