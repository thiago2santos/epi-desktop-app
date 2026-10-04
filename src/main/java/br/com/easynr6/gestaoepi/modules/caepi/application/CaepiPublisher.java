package br.com.easynr6.gestaoepi.modules.caepi.application;

import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog;
import br.com.easynr6.gestaoepi.modules.caepi.domain.CaepiParser.Resultado;
import org.springframework.stereotype.Service;

/** Publica só o catálogo. A auditoria fica numa gravação curta, depois do commit. */
@Service
public class CaepiPublisher {

  private final CaepiCatalog catalog;

  public CaepiPublisher(CaepiCatalog catalog) {
    this.catalog = catalog;
  }

  public long publicar(Long actorId, String nome, int tamanho, String sha, Resultado resultado) {
    return catalog.publicar(actorId, nome, tamanho, sha, resultado);
  }
}
