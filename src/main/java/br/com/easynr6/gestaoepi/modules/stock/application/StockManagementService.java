package br.com.easynr6.gestaoepi.modules.stock.application;

import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.CaOption;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.EpiOption;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.LotBalance;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.UnitOption;
import br.com.easynr6.gestaoepi.modules.stock.application.usecase.ListLotsUseCase;
import br.com.easynr6.gestaoepi.modules.stock.application.usecase.ReceiveLotUseCase;
import br.com.easynr6.gestaoepi.modules.stock.domain.LotPolicy;
import br.com.easynr6.gestaoepi.modules.stock.domain.PecaVencidaNaoConfirmadaException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StockManagementService {

  private final ReceiveLotUseCase receiveLotUseCase;
  private final ListLotsUseCase listLotsUseCase;
  private final LotPolicy lotPolicy = new LotPolicy();

  public StockManagementService(
      ReceiveLotUseCase receiveLotUseCase, ListLotsUseCase listLotsUseCase) {
    this.receiveLotUseCase = receiveLotUseCase;
    this.listLotsUseCase = listLotsUseCase;
  }

  public Long receiveLot(
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
    if (lotPolicy.vencida(pieceValidUntil, LocalDate.now()) && !aceitaPecaVencida) {
      throw new PecaVencidaNaoConfirmadaException();
    }
    return receiveLotUseCase.execute(
        actorId,
        unitId,
        epiId,
        caBindingId,
        lotCode,
        manufacturer,
        size,
        pieceValidUntil,
        quantity,
        unitCost,
        aceitaPecaVencida);
  }

  public List<LotBalance> listLots(Long actorId, Long unitId) {
    return listLotsUseCase.listLots(actorId, unitId);
  }

  public List<UnitOption> listUnits(Long actorId) {
    return listLotsUseCase.listUnits(actorId);
  }

  public List<EpiOption> listEpis(Long actorId) {
    return listLotsUseCase.listEpis(actorId);
  }

  public List<CaOption> listCas(Long actorId, Long epiId) {
    return listLotsUseCase.listCas(actorId, epiId);
  }
}
