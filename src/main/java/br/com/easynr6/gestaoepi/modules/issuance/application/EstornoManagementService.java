package br.com.easynr6.gestaoepi.modules.issuance.application;

import br.com.easynr6.gestaoepi.modules.issuance.application.port.EstornoRepository.ItemAberto;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.EstornoRepository.TrabalhadorBusca;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.ConsultarEstornoUseCase;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarEstornoUseCase;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EstornoManagementService {

  private final RegistrarEstornoUseCase registrarEstornoUseCase;
  private final ConsultarEstornoUseCase consultarEstornoUseCase;

  public EstornoManagementService(
      RegistrarEstornoUseCase registrarEstornoUseCase,
      ConsultarEstornoUseCase consultarEstornoUseCase) {
    this.registrarEstornoUseCase = registrarEstornoUseCase;
    this.consultarEstornoUseCase = consultarEstornoUseCase;
  }

  public long registrar(Long actorId, Long itemId, String motivo) {
    return registrarEstornoUseCase.execute(actorId, itemId, motivo);
  }

  public List<TrabalhadorBusca> buscar(Long actorId, String texto) {
    return consultarEstornoUseCase.buscar(actorId, texto);
  }

  public List<ItemAberto> abertos(Long actorId, Long employeeId) {
    return consultarEstornoUseCase.abertos(actorId, employeeId);
  }

  public ItemAberto exigirAberto(Long actorId, Long itemId) {
    return consultarEstornoUseCase.exigirAberto(actorId, itemId);
  }
}
