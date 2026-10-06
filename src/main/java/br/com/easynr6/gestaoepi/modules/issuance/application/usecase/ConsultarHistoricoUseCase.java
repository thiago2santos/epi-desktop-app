package br.com.easynr6.gestaoepi.modules.issuance.application.usecase;

import br.com.easynr6.gestaoepi.modules.issuance.application.HistoricoAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.HistoricoRepository;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.HistoricoRepository.Linha;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.HistoricoRepository.TrabalhadorBusca;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConsultarHistoricoUseCase {

  private final HistoricoRepository historicoRepository;
  private final HistoricoAccessAuthorizer accessAuthorizer;

  public ConsultarHistoricoUseCase(
      HistoricoRepository historicoRepository, HistoricoAccessAuthorizer accessAuthorizer) {
    this.historicoRepository = historicoRepository;
    this.accessAuthorizer = accessAuthorizer;
  }

  public List<TrabalhadorBusca> buscar(Long actorId, String texto) {
    accessAuthorizer.assertCanRead(actorId);
    return historicoRepository.buscar(texto);
  }

  public List<Linha> listar(Long actorId, Long employeeId, LocalDate inicio, LocalDate fim) {
    accessAuthorizer.assertCanRead(actorId);
    return historicoRepository.listar(employeeId, inicio, fim);
  }
}
