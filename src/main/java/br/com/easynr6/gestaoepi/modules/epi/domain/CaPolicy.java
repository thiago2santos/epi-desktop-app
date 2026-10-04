package br.com.easynr6.gestaoepi.modules.epi.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

public class CaPolicy {

  private static final Set<String> NOTAS_VAGAS =
      Set.of("ok", "consultei", "sim", "verificado", "ciente");

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
    StringBuilder digits = new StringBuilder();
    String raw = caNumber == null ? "" : caNumber;
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      if (c >= '0' && c <= '9') {
        digits.append(c);
      }
    }
    int inicio = 0;
    while (inicio < digits.length() - 1 && digits.charAt(inicio) == '0') {
      inicio++;
    }
    if (inicio > 0) {
      digits.delete(0, inicio);
    }
    if (digits.isEmpty() || digits.length() > 6) {
      throw new IllegalArgumentException("CAD-034 Numero de CA invalido ou ausente.");
    }
    return digits.toString();
  }

  public void rejeitarNotaVaga(String note) {
    String texto = normalizeOfficialNote(note).toLowerCase(Locale.ROOT);
    if (texto.isBlank() || NOTAS_VAGAS.contains(texto)) {
      throw new IllegalArgumentException("CAD-037 Evidencia de consulta oficial do CA ausente.");
    }
  }

  public void validarAnexo(String fileName, byte[] content, boolean obrigatorio) {
    boolean vazio = content == null || content.length == 0;
    if (vazio) {
      if (obrigatorio) {
        throw new IllegalArgumentException("CAD-037 Evidencia de consulta oficial do CA ausente.");
      }
      return;
    }
    if (content.length > 5_000_000 || !imagem(fileName, content)) {
      throw new IllegalArgumentException("CAD-037 Anexo de consulta precisa ser PNG ou JPG.");
    }
  }

  private static boolean imagem(String fileName, byte[] content) {
    String nome = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
    boolean extensao = nome.endsWith(".png") || nome.endsWith(".jpg") || nome.endsWith(".jpeg");
    boolean png =
        content.length >= 8
            && content[0] == (byte) 0x89
            && content[1] == 0x50
            && content[2] == 0x4E
            && content[3] == 0x47;
    boolean jpg =
        content.length >= 3
            && (content[0] & 0xFF) == 0xFF
            && (content[1] & 0xFF) == 0xD8
            && (content[2] & 0xFF) == 0xFF;
    return extensao && (png || jpg);
  }

  public String normalizeOfficialNote(String officialCheckNote) {
    return officialCheckNote == null ? "" : officialCheckNote.trim();
  }
}
