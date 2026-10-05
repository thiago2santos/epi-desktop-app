package br.com.easynr6.gestaoepi.modules.stock.application.port;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StockRepository {

  boolean unitIsActive(Long unitId);

  Optional<CaParaRecebimento> findActiveCa(Long epiId, Long caBindingId);

  boolean lotExists(Long unitId, Long epiId, String lotCode, String sizeLabel);

  Long insertReceipt(
      Long unitId,
      Long epiId,
      Long caBindingId,
      String lotCode,
      String manufacturer,
      String sizeLabel,
      LocalDate pieceValidUntil,
      LocalDateTime caCheckedAt,
      Integer unitCostCents,
      int quantity);

  List<LotStored> listByUnit(Long unitId);

  List<UnitOption> listActiveUnits();

  List<EpiOption> listActiveEpis();

  List<CaOption> listActiveCas(Long epiId);

  record CaParaRecebimento(Long id, LocalDateTime officialCheckAt) {}

  record UnitOption(Long id, String name) {
    @Override
    public String toString() {
      return name;
    }
  }

  record EpiOption(Long id, String code, String description) {
    @Override
    public String toString() {
      if (code == null || code.isBlank()) {
        return description;
      }
      return code + " — " + description;
    }
  }

  record CaOption(Long id, String caNumber, LocalDateTime officialCheckAt, String manufacturer) {
    @Override
    public String toString() {
      return caNumber;
    }
  }

  record LotStored(
      Long id,
      String lotCode,
      String epiDescription,
      String sizeLabel,
      LocalDate pieceValidUntil,
      int fisica,
      int reservada,
      Integer unitCostCents,
      String manufacturer) {}

  record LotBalance(
      Long id,
      String lotCode,
      String epiDescription,
      String sizeLabel,
      LocalDate pieceValidUntil,
      int fisica,
      int reservada,
      int disponivel,
      String situacao) {}
}
