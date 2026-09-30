package br.com.easynr6.gestaoepi.modules.epi.domain;

public class EpiPolicy {

  public void validateRequiredFields(
      String description, AnnexGroup annexGroup, String manufacturerName) {
    if (description == null
        || description.trim().isEmpty()
        || annexGroup == null
        || manufacturerName == null
        || manufacturerName.trim().isEmpty()) {
      throw new IllegalArgumentException("CAD-031 Campos obrigatorios de EPI ausentes.");
    }
  }

  public String normalizeDescription(String description) {
    return description == null ? "" : description.trim();
  }

  public String normalizeManufacturerName(String manufacturerName) {
    return manufacturerName == null ? "" : manufacturerName.trim();
  }

  public String normalizeEpiCode(String epiCode) {
    String normalized = epiCode == null ? "" : epiCode.trim();
    return normalized.isEmpty() ? null : normalized.toUpperCase();
  }
}
