package br.com.easynr6.gestaoepi.modules.caepi.application;

import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog;
import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog.CaPublicado;
import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog.CargaSucesso;
import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog.Linha;
import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog.Tentativa;
import br.com.easynr6.gestaoepi.modules.caepi.application.usecase.ImportCaepiUseCase;
import br.com.easynr6.gestaoepi.modules.epi.application.usecase.EpiCatalogAccessAuthorizer;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Service;

@Service
public class CaepiCatalogService {

  private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private final ImportCaepiUseCase importCaepiUseCase;
  private final CaepiCatalog catalog;
  private final EpiCatalogAccessAuthorizer accessAuthorizer;
  private final AtomicBoolean emAndamento = new AtomicBoolean(false);

  public CaepiCatalogService(
      ImportCaepiUseCase importCaepiUseCase,
      CaepiCatalog catalog,
      EpiCatalogAccessAuthorizer accessAuthorizer) {
    this.importCaepiUseCase = importCaepiUseCase;
    this.catalog = catalog;
    this.accessAuthorizer = accessAuthorizer;
  }

  public CaepiPrevia inspecionar(Long actorId, String fileName, byte[] content) {
    return importCaepiUseCase.inspecionar(actorId, fileName, content);
  }

  public long importar(Long actorId, String fileName, byte[] content) {
    if (!emAndamento.compareAndSet(false, true)) {
      throw new IllegalStateException("A atualizacao ja esta em andamento.");
    }
    try {
      return importCaepiUseCase.execute(actorId, fileName, content);
    } finally {
      emAndamento.set(false);
    }
  }

  public Optional<CargaSucesso> ultimaSucesso() {
    return catalog.ultimaSucesso();
  }

  public Optional<CaPublicado> findByNumber(String caNumber) {
    return catalog.findByNumber(caNumber);
  }

  public List<Linha> buscar(Long actorId, String termo, String fabricanteEpi) {
    accessAuthorizer.assertCanManageCatalog(actorId);
    return catalog.buscar(termo, fabricanteEpi);
  }

  public List<Tentativa> ultimas(Long actorId) {
    accessAuthorizer.assertCanManageCatalog(actorId);
    return catalog.ultimas();
  }

  public String faixaStatus() {
    return ultimaSucesso()
        .map(
            carga ->
                "CAEPI: carga válida "
                    + carga.finishedAt().format(DATA)
                    + " · "
                    + carga.recordCount()
                    + " CAs")
        .orElse("CAEPI: sem carga neste ciclo. Envie o arquivo oficial ou use o print no vínculo.");
  }
}
