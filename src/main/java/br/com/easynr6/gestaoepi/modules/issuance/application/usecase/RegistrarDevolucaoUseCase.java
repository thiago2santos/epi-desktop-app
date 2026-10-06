package br.com.easynr6.gestaoepi.modules.issuance.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.Clock;
import br.com.easynr6.gestaoepi.modules.issuance.application.DevolucaoAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.DevolucaoRepository;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.DevolucaoRepository.ItemPendente;
import br.com.easynr6.gestaoepi.modules.issuance.domain.DevolucaoPolicy;
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoDevolucao;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarDevolucaoUseCase {

  private final DevolucaoRepository devolucaoRepository;
  private final DevolucaoAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final Clock clock;
  private final DevolucaoPolicy policy = new DevolucaoPolicy();

  public RegistrarDevolucaoUseCase(
      DevolucaoRepository devolucaoRepository,
      DevolucaoAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail,
      Clock clock) {
    this.devolucaoRepository = devolucaoRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
    this.clock = clock;
  }

  @AcaoAuditada(acao = "DEVOLUCAO_REGISTRADA", entidade = "DEVOLUCAO")
  @Transactional
  public long execute(
      Long actorId, Long itemId, LocalDate data, MotivoDevolucao motivo, String texto) {
    accessAuthorizer.assertCanRegister(actorId);
    ItemPendente item =
        devolucaoRepository
            .findPendente(itemId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "POS-001 Este item nao esta pendente de devolucao."));
    LocalDateTime instante = clock.now();
    policy.data(item.fornecimento(), data, instante.toLocalDate());
    policy.motivo(motivo, texto);
    long id = devolucaoRepository.inserir(item.itemId(), motivo, texto, data, actorId, instante);
    auditTrail.registrarEventoCritico(
        actorId, "DEVOLUCAO_REGISTRADA", "DEVOLUCAO", String.valueOf(id), "Item " + item.itemId());
    return id;
  }
}
