package br.com.easynr6.gestaoepi.modules.epi.application;

import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository.CaBindingSummary;
import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository.EpiSummary;
import br.com.easynr6.gestaoepi.modules.epi.application.usecase.BindCaToEpiUseCase;
import br.com.easynr6.gestaoepi.modules.epi.application.usecase.CreateEpiUseCase;
import br.com.easynr6.gestaoepi.modules.epi.application.usecase.ListCaByEpiUseCase;
import br.com.easynr6.gestaoepi.modules.epi.application.usecase.ListEpiUseCase;
import br.com.easynr6.gestaoepi.modules.epi.application.usecase.SetCaBindingStatusUseCase;
import br.com.easynr6.gestaoepi.modules.epi.application.usecase.SetEpiStatusUseCase;
import br.com.easynr6.gestaoepi.modules.epi.application.usecase.UpdateCaBindingUseCase;
import br.com.easynr6.gestaoepi.modules.epi.application.usecase.UpdateEpiUseCase;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EpiCatalogManagementService {

  private final CreateEpiUseCase createEpiUseCase;
  private final UpdateEpiUseCase updateEpiUseCase;
  private final SetEpiStatusUseCase setEpiStatusUseCase;
  private final ListEpiUseCase listEpiUseCase;
  private final BindCaToEpiUseCase bindCaToEpiUseCase;
  private final UpdateCaBindingUseCase updateCaBindingUseCase;
  private final SetCaBindingStatusUseCase setCaBindingStatusUseCase;
  private final ListCaByEpiUseCase listCaByEpiUseCase;

  public EpiCatalogManagementService(
      CreateEpiUseCase createEpiUseCase,
      UpdateEpiUseCase updateEpiUseCase,
      SetEpiStatusUseCase setEpiStatusUseCase,
      ListEpiUseCase listEpiUseCase,
      BindCaToEpiUseCase bindCaToEpiUseCase,
      UpdateCaBindingUseCase updateCaBindingUseCase,
      SetCaBindingStatusUseCase setCaBindingStatusUseCase,
      ListCaByEpiUseCase listCaByEpiUseCase) {
    this.createEpiUseCase = createEpiUseCase;
    this.updateEpiUseCase = updateEpiUseCase;
    this.setEpiStatusUseCase = setEpiStatusUseCase;
    this.listEpiUseCase = listEpiUseCase;
    this.bindCaToEpiUseCase = bindCaToEpiUseCase;
    this.updateCaBindingUseCase = updateCaBindingUseCase;
    this.setCaBindingStatusUseCase = setCaBindingStatusUseCase;
    this.listCaByEpiUseCase = listCaByEpiUseCase;
  }

  public Long createEpi(
      Long actorId,
      String epiCode,
      String description,
      AnnexGroup annexGroup,
      String manufacturerName,
      boolean active) {
    return createEpiUseCase.execute(
        actorId, epiCode, description, annexGroup, manufacturerName, active);
  }

  public void updateEpi(
      Long actorId,
      Long epiId,
      String epiCode,
      String description,
      AnnexGroup annexGroup,
      String manufacturerName,
      boolean active) {
    updateEpiUseCase.execute(
        actorId, epiId, epiCode, description, annexGroup, manufacturerName, active);
  }

  public void setEpiStatus(Long actorId, Long epiId, boolean active) {
    setEpiStatusUseCase.execute(actorId, epiId, active);
  }

  public List<EpiSummary> listEpi(Long actorId, String term) {
    return listEpiUseCase.execute(actorId, term);
  }

  public Long bindCaToEpi(
      Long actorId,
      Long epiId,
      String caNumber,
      CaStatus caStatus,
      LocalDate validFrom,
      LocalDate validUntil,
      LocalDateTime officialCheckAt,
      String officialCheckNote,
      boolean active) {
    return bindCaToEpiUseCase.execute(
        actorId,
        epiId,
        caNumber,
        caStatus,
        validFrom,
        validUntil,
        officialCheckAt,
        officialCheckNote,
        active);
  }

  public void updateCaBinding(
      Long actorId,
      Long bindingId,
      String caNumber,
      CaStatus caStatus,
      LocalDate validFrom,
      LocalDate validUntil,
      LocalDateTime officialCheckAt,
      String officialCheckNote,
      boolean active) {
    updateCaBindingUseCase.execute(
        actorId,
        bindingId,
        caNumber,
        caStatus,
        validFrom,
        validUntil,
        officialCheckAt,
        officialCheckNote,
        active);
  }

  public void setCaBindingStatus(Long actorId, Long bindingId, boolean active) {
    setCaBindingStatusUseCase.execute(actorId, bindingId, active);
  }

  public List<CaBindingSummary> listCaByEpi(Long actorId, Long epiId) {
    return listCaByEpiUseCase.execute(actorId, epiId);
  }
}
