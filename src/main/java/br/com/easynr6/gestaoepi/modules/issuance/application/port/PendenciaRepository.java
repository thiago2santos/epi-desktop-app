package br.com.easynr6.gestaoepi.modules.issuance.application.port;

import java.time.LocalDate;
import java.util.List;

public interface PendenciaRepository {

  List<Pendencia> listar(Long unitId, LocalDate inicio, LocalDate fim, Long employeeId);

  record Pendencia(
      long itemId,
      long employeeId,
      String matricula,
      String nome,
      String epi,
      LocalDate fornecimento,
      int quantidade,
      long unitId) {}
}
