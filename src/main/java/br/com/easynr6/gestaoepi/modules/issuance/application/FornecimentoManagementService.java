package br.com.easynr6.gestaoepi.modules.issuance.application;

import br.com.easynr6.gestaoepi.modules.issuance.application.port.FornecimentoRepository.Trabalhador;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.ConsultarFornecimentoUseCase;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.ConsultarFornecimentoUseCase.LoteVisivel;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarFornecimentoUseCase;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarFornecimentoUseCase.Pedido;
import br.com.easynr6.gestaoepi.modules.issuance.domain.FornecimentoPolicy;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.EpiOpcao;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.LinhaMatriz;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FornecimentoManagementService {

  private final RegistrarFornecimentoUseCase registrarFornecimentoUseCase;
  private final ConsultarFornecimentoUseCase consultarFornecimentoUseCase;

  public FornecimentoManagementService(
      RegistrarFornecimentoUseCase registrarFornecimentoUseCase,
      ConsultarFornecimentoUseCase consultarFornecimentoUseCase) {
    this.registrarFornecimentoUseCase = registrarFornecimentoUseCase;
    this.consultarFornecimentoUseCase = consultarFornecimentoUseCase;
  }

  public long registrar(Long actorId, Pedido pedido) {
    return registrarFornecimentoUseCase.execute(actorId, pedido);
  }

  public List<Trabalhador> buscar(Long actorId, String texto) {
    return consultarFornecimentoUseCase.buscar(actorId, texto);
  }

  public List<LinhaMatriz> itensDaFuncao(Long actorId, Long employeeId) {
    return consultarFornecimentoUseCase.itensDaFuncao(actorId, employeeId);
  }

  public List<EpiOpcao> episForaDaMatriz(Long actorId, Long employeeId) {
    return consultarFornecimentoUseCase.episForaDaMatriz(actorId, employeeId);
  }

  public List<LoteVisivel> lotes(Long actorId, Long unitId, Long epiId) {
    return consultarFornecimentoUseCase.lotes(actorId, unitId, epiId);
  }

  public String textoDoTermo(String nome) {
    return FornecimentoPolicy.textoDoTermo(nome);
  }
}
