package br.com.easynr6.gestaoepi.modules.epi.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CaPolicy {

  public void validateRequiredFields(
      String caNumber, CaStatus caStatus, LocalDateTime officialCheckAt, String officialCheckNote) {
    if (caNumber == null || caNumber.trim().isEmpty()) {
      throw new IllegalArgumentException("CAD-034 Numero de CA invalido ou ausente.");
    }
    if (caStatus == null) {
      throw new IllegalArgumentException("CAD-034 Numero de CA invalido ou ausente.");
    }
    if (officialCheckAt == null
        || officialCheckNote == null
        || officialCheckNote.trim().isEmpty()) {
      throw new IllegalArgumentException("CAD-037 Evidencia de consulta oficial do CA ausente.");
    }
  }

  public void validateValidityWindow(LocalDate validFrom, LocalDate validUntil) {
    if (validFrom != null && validUntil != null && validUntil.isBefore(validFrom)) {
      throw new IllegalArgumentException("CAD-035 CA com conflito de vigencia para o mesmo EPI.");
    }
  }

  public void validateStatusForActivation(CaStatus caStatus, boolean active) {
    if (active && caStatus != CaStatus.ACTIVE) {
      throw new IllegalArgumentException("CAD-036 Situacao do CA impede ativacao do vinculo.");
    }
  }

  public String normalizeCaNumber(String caNumber) {
    if (caNumber == null) {
      return "";
    }
    return caNumber.trim().replaceAll("\\s+", "").toUpperCase();
  }

  public String normalizeOfficialNote(String officialCheckNote) {
    return officialCheckNote == null ? "" : officialCheckNote.trim();
  }
}
