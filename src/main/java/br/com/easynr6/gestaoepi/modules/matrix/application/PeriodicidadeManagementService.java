package br.com.easynr6.gestaoepi.modules.matrix.application;

import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository.Definicao;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository.LinhaPeriodicidade;
import br.com.easynr6.gestaoepi.modules.matrix.application.usecase.LerCoberturaUseCase;
import br.com.easynr6.gestaoepi.modules.matrix.application.usecase.LerCoberturaUseCase.LeituraCobertura;
import br.com.easynr6.gestaoepi.modules.matrix.application.usecase.ListarPeriodicidadeUseCase;
import br.com.easynr6.gestaoepi.modules.matrix.application.usecase.SalvarPeriodicidadeUseCase;
import br.com.easynr6.gestaoepi.modules.matrix.domain.ModoMatriz;
import br.com.easynr6.gestaoepi.modules.matrix.domain.PeriodicidadePolicy;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PeriodicidadeManagementService {

  private final ListarPeriodicidadeUseCase listarPeriodicidadeUseCase;
  private final SalvarPeriodicidadeUseCase salvarPeriodicidadeUseCase;
  private final LerCoberturaUseCase lerCoberturaUseCase;
  private final PeriodicidadePolicy policy = new PeriodicidadePolicy();

  public PeriodicidadeManagementService(
      ListarPeriodicidadeUseCase listarPeriodicidadeUseCase,
      SalvarPeriodicidadeUseCase salvarPeriodicidadeUseCase,
      LerCoberturaUseCase lerCoberturaUseCase) {
    this.listarPeriodicidadeUseCase = listarPeriodicidadeUseCase;
    this.salvarPeriodicidadeUseCase = salvarPeriodicidadeUseCase;
    this.lerCoberturaUseCase = lerCoberturaUseCase;
  }

  public List<LinhaPeriodicidade> listar(Long actorId) {
    return listarPeriodicidadeUseCase.execute(actorId);
  }

  public int salvar(Long actorId, List<Definicao> linhas) {
    return salvarPeriodicidadeUseCase.execute(actorId, linhas);
  }

  public List<LeituraCobertura> cobertura(Long employeeId) {
    return lerCoberturaUseCase.execute(employeeId);
  }

  public String situacao(
      ModoMatriz modo, Integer dias, Integer aviso, LocalDate fornecimento, LocalDate hoje) {
    return policy.situacao(modo, dias, aviso, fornecimento, hoje);
  }
}
