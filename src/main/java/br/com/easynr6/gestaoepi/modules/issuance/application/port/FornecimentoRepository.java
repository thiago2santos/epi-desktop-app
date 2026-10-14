package br.com.easynr6.gestaoepi.modules.issuance.application.port;

import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoFornecimento;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FornecimentoRepository {

  Optional<Trabalhador> findTrabalhador(Long employeeId);

  Optional<String> hashSenhaRecebimento(Long employeeId);

  Pagina buscar(String texto, int pagina, int tamanho, boolean somenteAtivos, Long setorId);

  /** Página da busca de trabalhador. Com total zero ainda conta uma página. */
  record Pagina(List<Trabalhador> itens, int total, int pagina, int tamanho) {
    public int paginas() {
      if (total <= 0 || tamanho <= 0) {
        return 1;
      }
      return (total + tamanho - 1) / tamanho;
    }
  }

  Optional<LoteLido> findLote(Long loteId);

  List<LoteLido> listarLotes(Long unitId, Long epiId);

  List<Long> episComEntregaQueConta(Long employeeId);

  Optional<String> funcaoDaUltimaFicha(Long employeeId);

  List<String> casAtivos(Long epiId);

  long inserirFicha(Trabalhador trabalhador, Long operatorUserId, LocalDateTime confirmedAt);

  long inserirItem(long fichaId, ItemGravado item);

  void inserirCa(long itemId, String caNumber);

  void inserirTermo(
      long fichaId, String versao, String metodo, LocalDateTime acceptedAt, Long operatorUserId);

  record Trabalhador(
      long id,
      String matricula,
      String nome,
      boolean ativo,
      long unitId,
      String unidade,
      String setor,
      String funcao,
      long jobRoleId,
      String gestor) {}

  record LoteLido(
      long id,
      long epiId,
      long unitId,
      String codigo,
      String ca,
      String caStatus,
      LocalDate validade,
      int fisica,
      int reservada,
      Integer custoCentavos) {}

  record ItemGravado(
      long loteId,
      long epiId,
      int quantidade,
      MotivoFornecimento motivo,
      String motivoTexto,
      boolean ciencia,
      LocalDate dataTreinamento,
      String excecaoTexto) {}
}
