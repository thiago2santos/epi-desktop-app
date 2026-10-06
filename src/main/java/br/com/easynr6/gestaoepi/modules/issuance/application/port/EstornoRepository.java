package br.com.easynr6.gestaoepi.modules.issuance.application.port;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EstornoRepository {

  List<TrabalhadorBusca> buscar(String texto);

  List<ItemAberto> listarAbertos(Long employeeId);

  Optional<ItemAberto> findAberto(Long itemId);

  long inserir(long itemId, String motivo, Long operatorUserId, LocalDateTime createdAt);

  void registrarMovimento(ItemAberto item, LocalDateTime createdAt);

  record TrabalhadorBusca(
      long id, String matricula, String nome, boolean ativo, String setor, String funcao) {}

  record ItemAberto(
      long itemId,
      long fichaId,
      long employeeId,
      long epiId,
      long loteId,
      String epi,
      String ca,
      String lote,
      int quantidade,
      LocalDate fornecimento,
      String matricula,
      String nome,
      Integer custoCentavos,
      String unidade,
      String setor,
      String funcao) {}
}
