package br.com.easynr6.gestaoepi.modules.stock.application.usecase;

import br.com.easynr6.gestaoepi.modules.stock.application.StockAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.CaParaRecebimento;
import br.com.easynr6.gestaoepi.modules.stock.domain.LotPolicy;
import br.com.easynr6.gestaoepi.modules.stock.domain.PecaVencidaNaoConfirmadaException;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReceiveLotUseCase {

  private final StockRepository stockRepository;
  private final StockAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final LotPolicy lotPolicy = new LotPolicy();

  public ReceiveLotUseCase(
      StockRepository stockRepository,
      StockAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.stockRepository = stockRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "LOTE_RECEBIDO", entidade = "LOTE")
  @Transactional
  public Long execute(
      Long actorId,
      Long unitId,
      Long epiId,
      Long caBindingId,
      String lotCode,
      String manufacturer,
      String size,
      LocalDate pieceValidUntil,
      String quantity,
      String unitCost,
      boolean aceitaPecaVencida) {
    accessAuthorizer.assertCanReceive(actorId);
    if (unitId == null || epiId == null || caBindingId == null || pieceValidUntil == null) {
      throw new IllegalArgumentException(
          "LOT-001 Unidade, EPI, CA, codigo, validade ou quantidade ausente.");
    }
    String codigo = lotPolicy.codigo(lotCode);
    String tamanho = lotPolicy.tamanho(size);
    String fabricante = lotPolicy.fabricante(manufacturer);
    int quantidade = lotPolicy.quantidade(quantity);
    Integer custo = lotPolicy.custoCentavos(unitCost);
    if (!stockRepository.unitIsActive(unitId)) {
      throw new IllegalArgumentException("LOT-005 Unidade inexistente ou inativa.");
    }
    CaParaRecebimento ca =
        stockRepository
            .findActiveCa(epiId, caBindingId)
            .orElseThrow(
                () -> new IllegalArgumentException("LOT-004 EPI sem vinculo de CA ativo."));
    if (stockRepository.lotExists(unitId, epiId, codigo, tamanho)) {
      throw new IllegalArgumentException(
          "LOT-003 Ja existe este lote para este EPI nesta unidade.");
    }
    LocalDate hoje = LocalDate.now();
    if (lotPolicy.vencida(pieceValidUntil, hoje) && !aceitaPecaVencida) {
      throw new PecaVencidaNaoConfirmadaException();
    }
    LocalDateTime consulta =
        ca.officialCheckAt() == null ? hoje.atStartOfDay() : ca.officialCheckAt();
    try {
      Long loteId =
          stockRepository.insertReceipt(
              unitId,
              epiId,
              ca.id(),
              codigo,
              fabricante,
              tamanho,
              pieceValidUntil,
              consulta,
              custo,
              quantidade);
      auditTrail.registrarEventoCritico(
          actorId, "LOTE_RECEBIDO", "LOTE", String.valueOf(loteId), "Lote recebido: " + codigo);
      return loteId;
    } catch (DataIntegrityViolationException ex) {
      String detalhe = ex.getMostSpecificCause().getMessage();
      if (detalhe != null && detalhe.contains("UNIQUE")) {
        throw new IllegalArgumentException(
            "LOT-003 Ja existe este lote para este EPI nesta unidade.");
      }
      throw ex;
    }
  }
}
