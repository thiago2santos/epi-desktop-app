package br.com.easynr6.gestaoepi.modules.caepi.application;

import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog;
import org.springframework.stereotype.Component;

/** A falha fica no arquivo do catálogo, fora da transação de auditoria. */
@Component
public class CaepiFailureLog {

  private final CaepiCatalog catalog;

  public CaepiFailureLog(CaepiCatalog catalog) {
    this.catalog = catalog;
  }

  public void registrar(Long actorId, String nome, int tamanho, String sha, RuntimeException ex) {
    String motivo =
        ex.getMessage() == null ? "CAE-004 A carga CAEPI nao foi concluida." : ex.getMessage();
    catalog.registrarFalha(actorId, nome, tamanho, sha, motivo);
  }
}
