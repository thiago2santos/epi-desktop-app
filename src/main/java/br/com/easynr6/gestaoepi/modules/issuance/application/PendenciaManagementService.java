package br.com.easynr6.gestaoepi.modules.issuance.application;

import br.com.easynr6.gestaoepi.modules.issuance.application.port.PendenciaRepository.Pendencia;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.ConsultarPendenciaUseCase;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PendenciaManagementService {

  private final ConsultarPendenciaUseCase consultarPendenciaUseCase;

  public PendenciaManagementService(ConsultarPendenciaUseCase consultarPendenciaUseCase) {
    this.consultarPendenciaUseCase = consultarPendenciaUseCase;
  }

  public List<Pendencia> listar(
      Long actorId, Long unitId, LocalDate inicio, LocalDate fim, Long employeeId) {
    return consultarPendenciaUseCase.listar(actorId, unitId, inicio, fim, employeeId);
  }
}
