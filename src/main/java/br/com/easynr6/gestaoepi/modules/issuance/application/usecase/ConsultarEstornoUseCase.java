package br.com.easynr6.gestaoepi.modules.issuance.application.usecase;

import br.com.easynr6.gestaoepi.modules.issuance.application.EstornoAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.EstornoRepository;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.EstornoRepository.ItemAberto;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.EstornoRepository.TrabalhadorBusca;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConsultarEstornoUseCase {

  private final EstornoRepository estornoRepository;
  private final EstornoAccessAuthorizer accessAuthorizer;

  public ConsultarEstornoUseCase(
      EstornoRepository estornoRepository, EstornoAccessAuthorizer accessAuthorizer) {
    this.estornoRepository = estornoRepository;
    this.accessAuthorizer = accessAuthorizer;
  }

  public List<TrabalhadorBusca> buscar(Long actorId, String texto) {
    accessAuthorizer.assertCanRegister(actorId);
    return estornoRepository.buscar(texto);
  }

  public List<ItemAberto> abertos(Long actorId, Long employeeId) {
    accessAuthorizer.assertCanRegister(actorId);
    return estornoRepository.listarAbertos(employeeId);
  }

  public ItemAberto exigirAberto(Long actorId, Long itemId) {
    accessAuthorizer.assertCanRegister(actorId);
    return estornoRepository
        .findAberto(itemId)
        .orElseThrow(
            () ->
                new IllegalArgumentException("POS-004 Este fornecimento nao pode ser estornado."));
  }
}
