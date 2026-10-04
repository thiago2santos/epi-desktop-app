package br.com.easynr6.gestaoepi.modules.caepi.application.usecase;

import br.com.easynr6.gestaoepi.modules.caepi.application.CaepiFailureLog;
import br.com.easynr6.gestaoepi.modules.caepi.application.CaepiPrevia;
import br.com.easynr6.gestaoepi.modules.caepi.application.CaepiPublisher;
import br.com.easynr6.gestaoepi.modules.caepi.domain.CaepiHash;
import br.com.easynr6.gestaoepi.modules.caepi.domain.CaepiParser;
import br.com.easynr6.gestaoepi.modules.caepi.domain.CaepiParser.Inspecao;
import br.com.easynr6.gestaoepi.modules.caepi.domain.CaepiParser.Resultado;
import br.com.easynr6.gestaoepi.modules.epi.application.usecase.EpiCatalogAccessAuthorizer;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

@Service
public class ImportCaepiUseCase {

  private final CaepiPublisher publisher;
  private final CaepiFailureLog failureLog;
  private final AuditTrail auditTrail;
  private final EpiCatalogAccessAuthorizer accessAuthorizer;
  private final CaepiParser parser = new CaepiParser();

  public ImportCaepiUseCase(
      CaepiPublisher publisher,
      CaepiFailureLog failureLog,
      AuditTrail auditTrail,
      EpiCatalogAccessAuthorizer accessAuthorizer) {
    this.publisher = publisher;
    this.failureLog = failureLog;
    this.auditTrail = auditTrail;
    this.accessAuthorizer = accessAuthorizer;
  }

  public CaepiPrevia inspecionar(Long actorId, String fileName, byte[] content) {
    autorizar(actorId);
    String nome = nome(fileName);
    byte[] bytes = content == null ? new byte[0] : content;
    Inspecao inspecao = parser.inspecionar(nome, bytes);
    return new CaepiPrevia(nome, bytes.length, CaepiHash.sha256(bytes), inspecao);
  }

  @AcaoAuditada(acao = "CAEPI_IMPORTADA", entidade = "CAEPI")
  public long execute(Long actorId, String fileName, byte[] content) {
    autorizar(actorId);
    String nome = nome(fileName);
    byte[] bytes = content == null ? new byte[0] : content;
    String sha = CaepiHash.sha256(bytes);
    Resultado resultado;
    try {
      resultado = parser.parse(nome, bytes);
    } catch (RuntimeException ex) {
      failureLog.registrar(actorId, nome, bytes.length, sha, ex);
      throw ex;
    }
    long cargaId;
    try {
      cargaId = publisher.publicar(actorId, nome, bytes.length, sha, resultado);
    } catch (RuntimeException ex) {
      failureLog.registrar(actorId, nome, bytes.length, sha, ex);
      throw ex;
    }
    auditTrail.registrarEventoCritico(
        actorId,
        "CAEPI_IMPORTADA",
        "CAEPI",
        String.valueOf(cargaId),
        "Base CAEPI publicada: " + resultado.indice().size() + " CAs");
    return cargaId;
  }

  private void autorizar(Long actorId) {
    try {
      accessAuthorizer.assertCanManageCatalog(actorId);
    } catch (AuthorizationDeniedException ex) {
      throw new AuthorizationDeniedException(
          "CAE-006 Seu perfil nao permite iniciar a atualizacao CAEPI.");
    }
  }

  private static String nome(String fileName) {
    return fileName == null || fileName.isBlank() ? "arquivo-caepi" : fileName.trim();
  }
}
