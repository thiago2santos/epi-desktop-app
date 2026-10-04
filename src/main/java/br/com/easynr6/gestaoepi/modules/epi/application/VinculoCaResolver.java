package br.com.easynr6.gestaoepi.modules.epi.application;

import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog;
import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog.CaPublicado;
import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog.CargaSucesso;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaPolicy;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Caminho da base importada, ou print da consulta quando a base não cobre o número. */
@Component
public class VinculoCaResolver {

  private static final DateTimeFormatter DATA_HORA =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
  private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private final CaepiCatalog catalog;
  private final CaPolicy caPolicy = new CaPolicy();

  public VinculoCaResolver(CaepiCatalog catalog) {
    this.catalog = catalog;
  }

  public Resolvido resolver(
      String caNumber,
      CaStatus caStatus,
      LocalDate validFrom,
      LocalDate validUntil,
      LocalDateTime officialCheckAt,
      String officialCheckNote,
      boolean active,
      String evidenceFileName,
      byte[] evidence,
      boolean jaTemAnexo) {
    String numero = caPolicy.normalizeCaNumber(caNumber);
    Optional<CargaSucesso> carga = catalog.ultimaSucesso();
    Optional<CaPublicado> oficial = catalog.findByNumber(numero);
    if (carga.isPresent() && oficial.isPresent() && statusConhecido(oficial.get().status())) {
      return pelaBase(
          numero, carga.get(), oficial.get(), validFrom, active, evidenceFileName, evidence);
    }
    return peloPrint(
        numero,
        caStatus,
        validFrom,
        validUntil,
        officialCheckAt,
        officialCheckNote,
        active,
        evidenceFileName,
        evidence,
        jaTemAnexo);
  }

  private Resolvido pelaBase(
      String numero,
      CargaSucesso carga,
      CaPublicado oficial,
      LocalDate validFrom,
      boolean active,
      String evidenceFileName,
      byte[] evidence) {
    CaStatus status = CaStatus.valueOf(oficial.status());
    LocalDate validade = oficial.validUntil();
    caPolicy.validateStatusForActivation(status, active);
    caPolicy.validateValidityWindow(validFrom, validade);
    caPolicy.validarAnexo(evidenceFileName, evidence, false);
    String ate = validade == null ? "sem validade informada" : validade.format(DATA);
    String nota =
        "Carga CAEPI "
            + carga.id()
            + " em "
            + carga.finishedAt().format(DATA_HORA)
            + ". CA "
            + numero
            + " "
            + status.displayLabelPtBr()
            + ", validade "
            + ate
            + ".";
    return new Resolvido(
        numero, status, validFrom, validade, carga.finishedAt(), nota, evidenceFileName, evidence);
  }

  private Resolvido peloPrint(
      String numero,
      CaStatus caStatus,
      LocalDate validFrom,
      LocalDate validUntil,
      LocalDateTime officialCheckAt,
      String officialCheckNote,
      boolean active,
      String evidenceFileName,
      byte[] evidence,
      boolean jaTemAnexo) {
    caPolicy.validarAnexo(evidenceFileName, evidence, !jaTemAnexo);
    caPolicy.validateRequiredFields(numero, caStatus, officialCheckAt, officialCheckNote);
    caPolicy.rejeitarNotaVaga(officialCheckNote);
    caPolicy.validateStatusForActivation(caStatus, active);
    caPolicy.validateValidityWindow(validFrom, validUntil);
    return new Resolvido(
        numero,
        caStatus,
        validFrom,
        validUntil,
        officialCheckAt,
        caPolicy.normalizeOfficialNote(officialCheckNote),
        evidenceFileName,
        evidence);
  }

  private static boolean statusConhecido(String status) {
    return "ACTIVE".equals(status)
        || "SUSPENDED".equals(status)
        || "CANCELED".equals(status)
        || "EXPIRED".equals(status);
  }

  public record Resolvido(
      String caNumber,
      CaStatus caStatus,
      LocalDate validFrom,
      LocalDate validUntil,
      LocalDateTime officialCheckAt,
      String officialCheckNote,
      String evidenceFileName,
      byte[] evidence) {

    public boolean temAnexoNovo() {
      return evidence != null && evidence.length > 0;
    }
  }
}
