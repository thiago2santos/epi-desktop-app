package br.com.easynr6.gestaoepi.modules.issuance.application;

import br.com.easynr6.gestaoepi.modules.issuance.application.port.HistoricoRepository.Linha;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.HistoricoRepository.TrabalhadorBusca;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.ConsultarHistoricoUseCase;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HistoricoManagementService {

  private final ConsultarHistoricoUseCase consultarHistoricoUseCase;

  public HistoricoManagementService(ConsultarHistoricoUseCase consultarHistoricoUseCase) {
    this.consultarHistoricoUseCase = consultarHistoricoUseCase;
  }

  public List<TrabalhadorBusca> buscar(Long actorId, String texto) {
    return consultarHistoricoUseCase.buscar(actorId, texto);
  }

  public List<Linha> listar(Long actorId, Long employeeId, LocalDate inicio, LocalDate fim) {
    return consultarHistoricoUseCase.listar(actorId, employeeId, inicio, fim);
  }
}
