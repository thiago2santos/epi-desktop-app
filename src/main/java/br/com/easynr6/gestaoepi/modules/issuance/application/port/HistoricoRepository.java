package br.com.easynr6.gestaoepi.modules.issuance.application.port;

import java.time.LocalDate;
import java.util.List;

public interface HistoricoRepository {

  List<TrabalhadorBusca> buscar(String texto);

  List<Linha> listar(Long employeeId, LocalDate inicio, LocalDate fim);

  record TrabalhadorBusca(
      long id, String matricula, String nome, boolean ativo, String setor, String funcao) {}

  record Linha(
      LocalDate data,
      String epi,
      String ca,
      String lote,
      int quantidade,
      String motivo,
      String situacao,
      String fato,
      long itemId) {}
}
