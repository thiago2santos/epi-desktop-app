package br.com.easynr6.gestaoepi.modules.stock.application.usecase;

import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog;
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
  private final CaepiCatalog caepiCatalog;
  private final LotPolicy lotPolicy = new LotPolicy();

  public ListLotsUseCase(
      StockRepository stockRepository,
      StockAccessAuthorizer accessAuthorizer,
      CaepiCatalog caepiCatalog) {
    this.stockRepository = stockRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.caepiCatalog = caepiCatalog;
  }

  public List<LotBalance> listLots(Long actorId, Long unitId) {
    return listLots(actorId, unitId, null, null);
  }

  public List<LotBalance> listLots(Long actorId, Long unitId, String texto, String situacao) {
    accessAuthorizer.assertCanView(actorId);
    LocalDate hoje = LocalDate.now();
    return stockRepository.listByUnit(unitId).stream()
        .map(lote -> saldo(lote, hoje))
        .filter(
            item ->
                lotPolicy.apareceNaConsulta(
                    item.epiDescription(),
                    item.lotCode(),
                    item.sizeLabel(),
                    item.situacao(),
                    texto,
                    situacao))
        .toList();
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
    return stockRepository.listActiveCas(epiId).stream().map(this::comFabricante).toList();
  }

  private CaOption comFabricante(CaOption ca) {
    String fabricante =
        caepiCatalog
            .findByNumber(ca.caNumber())
            .map(publicado -> publicado.manufacturer())
            .orElse(null);
    if (fabricante == null || fabricante.isBlank()) {
      return ca;
    }
    return new CaOption(ca.id(), ca.caNumber(), ca.officialCheckAt(), fabricante);
  }

  private LotBalance saldo(LotStored lote, LocalDate hoje) {
    Saldo saldo = lotPolicy.saldo(lote.fisica(), lote.reservada(), lote.pieceValidUntil(), hoje);
    return new LotBalance(
        lote.id(),
        lote.unitId(),
        lote.unitName(),
        lote.epiId(),
        lote.caBindingId(),
        lote.caNumber(),
        lote.lotCode(),
        lote.epiDescription(),
        lote.sizeLabel(),
        lote.pieceValidUntil(),
        saldo.fisica(),
        saldo.reservada(),
        saldo.disponivel(),
        saldo.situacao(),
        lote.unitCostCents(),
        lote.manufacturer());
  }
}
