package br.com.easynr6.gestaoepi.modules.matrix.application.port;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PeriodicidadeRepository {

  List<LinhaPeriodicidade> listar();

  boolean temLinhaAtiva(Long epiId);

  Optional<Prazo> find(Long epiId);

  void salvar(Long epiId, int dias, int aviso);

  Optional<Trabalhador> findTrabalhador(Long employeeId);

  /**
   * Data do fornecimento que ainda conta. Enquanto não houver ficha, não há data: a cobertura fica
   * Pendente quando o prazo já foi salvo.
   */
  Optional<LocalDate> fornecimentoQueConta(Long employeeId, Long epiId);

  record LinhaPeriodicidade(Long epiId, String epi, int funcoes, Integer dias, Integer aviso) {}

  record Prazo(int dias, int aviso) {}

  record Trabalhador(Long jobRoleId, boolean ativo) {}

  record Definicao(Long epiId, Integer dias, Integer aviso) {}
}
