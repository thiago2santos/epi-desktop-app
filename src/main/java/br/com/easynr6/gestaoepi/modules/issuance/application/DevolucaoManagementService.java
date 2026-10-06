package br.com.easynr6.gestaoepi.modules.issuance.application;

import br.com.easynr6.gestaoepi.modules.issuance.application.port.DevolucaoRepository.ItemPendente;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.DevolucaoRepository.TrabalhadorBusca;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.ConsultarDevolucaoUseCase;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarDevolucaoUseCase;
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoDevolucao;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DevolucaoManagementService {

  private final RegistrarDevolucaoUseCase registrarDevolucaoUseCase;
  private final ConsultarDevolucaoUseCase consultarDevolucaoUseCase;

  public DevolucaoManagementService(
      RegistrarDevolucaoUseCase registrarDevolucaoUseCase,
      ConsultarDevolucaoUseCase consultarDevolucaoUseCase) {
    this.registrarDevolucaoUseCase = registrarDevolucaoUseCase;
    this.consultarDevolucaoUseCase = consultarDevolucaoUseCase;
  }

  public long registrar(
      Long actorId, Long itemId, LocalDate data, MotivoDevolucao motivo, String texto) {
    return registrarDevolucaoUseCase.execute(actorId, itemId, data, motivo, texto);
  }

  public List<TrabalhadorBusca> buscar(Long actorId, String texto) {
    return consultarDevolucaoUseCase.buscar(actorId, texto);
  }

  public List<ItemPendente> pendentes(Long actorId, Long employeeId) {
    return consultarDevolucaoUseCase.pendentes(actorId, employeeId);
  }

  public ItemPendente exigirPendente(Long actorId, Long itemId) {
    return consultarDevolucaoUseCase.exigirPendente(actorId, itemId);
  }
}
