package br.com.easynr6.gestaoepi.modules.caepi.application.port;

import br.com.easynr6.gestaoepi.modules.caepi.domain.CaepiParser.Resultado;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CaepiCatalog {

  long publicar(long actorId, String sourceName, int byteSize, String sha256, Resultado resultado);

  long registrarFalha(long actorId, String sourceName, int byteSize, String sha256, String motivo);

  Optional<CargaSucesso> ultimaSucesso();

  Optional<CaPublicado> findByNumber(String caNumber);

  List<Linha> buscar(
      String termo,
      String fabricanteEpi,
      boolean ativos,
      boolean suspensos,
      boolean cancelados,
      boolean expirados);

  List<Tentativa> ultimas();

  record CargaSucesso(long id, LocalDateTime finishedAt, int recordCount) {}

  record CaPublicado(
      String caNumber,
      String status,
      LocalDate validUntil,
      String equipment,
      String manufacturer) {}

  record Linha(
      String caNumber,
      String status,
      LocalDate validUntil,
      String equipment,
      String manufacturer) {}

  record Tentativa(
      long id,
      LocalDateTime finishedAt,
      String result,
      String sourceName,
      int recordCount,
      String failureReason) {}
}
