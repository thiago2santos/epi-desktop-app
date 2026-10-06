package br.com.easynr6.gestaoepi.modules.issuance.application.port;

import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoDevolucao;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DevolucaoRepository {

  List<TrabalhadorBusca> buscar(String texto);

  List<ItemPendente> listarPendentes(Long employeeId);

  Optional<ItemPendente> findPendente(Long itemId);

  long inserir(
      long itemId,
      MotivoDevolucao motivo,
      String motivoTexto,
      LocalDate devolvidoEm,
      Long operatorUserId,
      LocalDateTime createdAt);

  record TrabalhadorBusca(
      long id, String matricula, String nome, boolean ativo, String setor, String funcao) {}

  record ItemPendente(
      long itemId,
      long employeeId,
      long epiId,
      String epi,
      String ca,
      String lote,
      int quantidade,
      LocalDate fornecimento,
      String matricula,
      String nome) {}
}
