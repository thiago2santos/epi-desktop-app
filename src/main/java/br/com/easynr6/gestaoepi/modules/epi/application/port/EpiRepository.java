package br.com.easynr6.gestaoepi.modules.epi.application.port;

import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EpiRepository {

  boolean existsEpiById(Long epiId);

  boolean existsEpiDuplicate(
      Long excludeEpiId, String description, AnnexGroup annexGroup, String manufacturerName);

  Long createEpi(
      String epiCode,
      String description,
      AnnexGroup annexGroup,
      String manufacturerName,
      boolean active);

  void updateEpi(
      Long epiId,
      String epiCode,
      String description,
      AnnexGroup annexGroup,
      String manufacturerName,
      boolean active);

  void setEpiActive(Long epiId, boolean active);

  boolean hasHistoricalDependencies(Long epiId);

  boolean hasActiveCoherentCaForEpi(Long epiId);

  List<EpiSummary> listByTerm(String term);

  Long createCaBinding(
      Long epiId,
      String caNumber,
      CaStatus caStatus,
      LocalDate validFrom,
      LocalDate validUntil,
      LocalDateTime officialCheckAt,
      String officialCheckNote,
      boolean active);

  boolean existsCaBindingById(Long bindingId);

  Optional<CaBindingDetails> findCaBindingById(Long bindingId);

  void updateCaBinding(
      Long bindingId,
      String caNumber,
      CaStatus caStatus,
      LocalDate validFrom,
      LocalDate validUntil,
      LocalDateTime officialCheckAt,
      String officialCheckNote,
      boolean active);

  void setCaBindingActive(Long bindingId, boolean active);

  boolean hasHistoricalDependenciesForCaBinding(Long bindingId);

  boolean existsCaValidityConflict(
      Long epiId,
      String caNumber,
      LocalDate validFrom,
      LocalDate validUntil,
      Long excludeBindingId);

  List<CaBindingSummary> listCaByEpi(Long epiId);

  record EpiSummary(
      Long id,
      String epiCode,
      String description,
      AnnexGroup annexGroup,
      String manufacturerName,
      boolean active,
      String updatedAt) {}

  record CaBindingSummary(
      Long id,
      Long epiId,
      String caNumber,
      CaStatus caStatus,
      String validFrom,
      String validUntil,
      String officialCheckAt,
      String officialCheckNote,
      boolean active,
      String updatedAt) {}

  record CaBindingDetails(
      Long id,
      Long epiId,
      String caNumber,
      CaStatus caStatus,
      LocalDate validFrom,
      LocalDate validUntil,
      boolean active) {}
}
