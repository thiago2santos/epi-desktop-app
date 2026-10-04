package br.com.easynr6.gestaoepi.modules.stock.application.usecase;

import br.com.easynr6.gestaoepi.modules.stock.application.StockAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.CaOption;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.EpiOption;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.LotBalance;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.LotStored;
import br.com.easynr6.gestaoepi.modules.stock.application.port.StockRepository.UnitOption;
import br.com.easynr6.gestaoepi.modules.stock.domain.LotPolicy;
import br.com.easynr6.gestaoepi.modules.stock.domain.LotPolicy.Saldo;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListLotsUseCase {

  private final StockRepository stockRepository;
  private final StockAccessAuthorizer accessAuthorizer;
  private final LotPolicy lotPolicy = new LotPolicy();

  public ListLotsUseCase(StockRepository stockRepository, StockAccessAuthorizer accessAuthorizer) {
    this.stockRepository = stockRepository;
    this.accessAuthorizer = accessAuthorizer;
  }

  public List<LotBalance> listLots(Long actorId, Long unitId) {
    accessAuthorizer.assertCanView(actorId);
    if (unitId == null) {
      return List.of();
    }
    LocalDate hoje = LocalDate.now();
    return stockRepository.listByUnit(unitId).stream().map(lote -> saldo(lote, hoje)).toList();
  }

  public List<UnitOption> listUnits(Long actorId) {
    accessAuthorizer.assertCanView(actorId);
    return stockRepository.listActiveUnits();
  }

  public List<EpiOption> listEpis(Long actorId) {
    accessAuthorizer.assertCanView(actorId);
    return stockRepository.listActiveEpis();
  }

  public List<CaOption> listCas(Long actorId, Long epiId) {
    accessAuthorizer.assertCanView(actorId);
    if (epiId == null) {
      return List.of();
    }
    return stockRepository.listActiveCas(epiId);
  }

  private LotBalance saldo(LotStored lote, LocalDate hoje) {
    Saldo saldo = lotPolicy.saldo(lote.fisica(), 0, lote.pieceValidUntil(), hoje);
    return new LotBalance(
        lote.id(),
        lote.lotCode(),
        lote.epiDescription(),
        lote.sizeLabel(),
        lote.pieceValidUntil(),
        saldo.fisica(),
        saldo.reservada(),
        saldo.disponivel(),
        saldo.situacao());
  }
}
