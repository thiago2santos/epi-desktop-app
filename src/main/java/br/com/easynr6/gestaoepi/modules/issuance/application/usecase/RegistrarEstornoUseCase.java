package br.com.easynr6.gestaoepi.modules.issuance.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.Clock;
import br.com.easynr6.gestaoepi.modules.issuance.application.EstornoAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.EstornoRepository;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.EstornoRepository.ItemAberto;
import br.com.easynr6.gestaoepi.modules.issuance.domain.EstornoPolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarEstornoUseCase {

  private final EstornoRepository estornoRepository;
  private final EstornoAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final Clock clock;
  private final EstornoPolicy policy = new EstornoPolicy();

  public RegistrarEstornoUseCase(
      EstornoRepository estornoRepository,
      EstornoAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail,
      Clock clock) {
    this.estornoRepository = estornoRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
    this.clock = clock;
  }

  @AcaoAuditada(acao = "FORNECIMENTO_ESTORNADO", entidade = "FORNECIMENTO")
  @Transactional
  public long execute(Long actorId, Long itemId, String motivo) {
    accessAuthorizer.assertCanRegister(actorId);
    ItemAberto item =
        estornoRepository
            .findAberto(itemId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "POS-004 Este fornecimento nao pode ser estornado."));
    String texto = policy.motivo(motivo);
    LocalDateTime instante = clock.now();
    long id = estornoRepository.inserir(item.itemId(), texto, actorId, instante);
    estornoRepository.registrarMovimento(item, instante);
    auditTrail.registrarEventoCritico(
        actorId,
        "FORNECIMENTO_ESTORNADO",
        "FORNECIMENTO",
        String.valueOf(item.fichaId()),
        "Item " + item.itemId() + " estorno " + id);
    return id;
  }
}
