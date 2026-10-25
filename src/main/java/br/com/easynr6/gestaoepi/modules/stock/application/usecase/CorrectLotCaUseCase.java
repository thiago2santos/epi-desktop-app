package br.com.easynr6.gestaoepi.modules.stock.application.usecase;

import br.com.easynr6.gestaoepi.modules.stock.application.StockAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.CaParaRecebimento;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.LoteParaCorrecao;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CorrectLotCaUseCase {

  private final StockRepository stockRepository;
  private final StockAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;

  public CorrectLotCaUseCase(
      StockRepository stockRepository,
      StockAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.stockRepository = stockRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "CA_LOTE_CORRIGIDO", entidade = "LOTE", alvo = 1)
  @Transactional
  public void execute(Long actorId, Long lotId, Long caBindingId) {
    accessAuthorizer.assertCanCorrectCa(actorId);
    if (lotId == null || caBindingId == null) {
      throw new IllegalArgumentException("LOT-006 Selecione um lote da lista.");
    }
    LoteParaCorrecao lote =
        stockRepository
            .findLot(lotId)
            .orElseThrow(() -> new IllegalArgumentException("LOT-006 Selecione um lote da lista."));
    if (caBindingId.equals(lote.caBindingId())) {
      throw new IllegalArgumentException("LOT-017 Este lote ja esta com este CA.");
    }
    CaParaRecebimento ca =
        stockRepository
            .findActiveCa(lote.epiId(), caBindingId)
            .orElseThrow(
                () -> new IllegalArgumentException("LOT-018 Escolha um CA ativo deste EPI."));
    LocalDateTime consulta =
        ca.officialCheckAt() == null ? LocalDate.now().atStartOfDay() : ca.officialCheckAt();
    stockRepository.updateLotCa(lote.id(), ca.id(), consulta);
    auditTrail.registrarEventoCritico(
        actorId,
        "CA_LOTE_CORRIGIDO",
        "LOTE",
        String.valueOf(lote.id()),
        "CA de " + lote.caNumber() + " para " + ca.caNumber() + " no lote " + lote.lotCode());
  }
}
