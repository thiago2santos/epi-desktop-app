package br.com.easynr6.gestaoepi.modules.issuance.application.usecase;

import br.com.easynr6.gestaoepi.modules.issuance.application.DevolucaoAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.DevolucaoRepository;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.DevolucaoRepository.ItemPendente;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.DevolucaoRepository.TrabalhadorBusca;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConsultarDevolucaoUseCase {

  private final DevolucaoRepository devolucaoRepository;
  private final DevolucaoAccessAuthorizer accessAuthorizer;

  public ConsultarDevolucaoUseCase(
      DevolucaoRepository devolucaoRepository, DevolucaoAccessAuthorizer accessAuthorizer) {
    this.devolucaoRepository = devolucaoRepository;
    this.accessAuthorizer = accessAuthorizer;
  }

  public List<TrabalhadorBusca> buscar(Long actorId, String texto) {
    accessAuthorizer.assertCanRegister(actorId);
    return devolucaoRepository.buscar(texto);
  }

  public List<ItemPendente> pendentes(Long actorId, Long employeeId) {
    accessAuthorizer.assertCanRegister(actorId);
    return devolucaoRepository.listarPendentes(employeeId);
  }

  public ItemPendente exigirPendente(Long actorId, Long itemId) {
    accessAuthorizer.assertCanRegister(actorId);
    return devolucaoRepository
        .findPendente(itemId)
        .orElseThrow(
            () ->
                new IllegalArgumentException("POS-001 Este item nao esta pendente de devolucao."));
  }
}
