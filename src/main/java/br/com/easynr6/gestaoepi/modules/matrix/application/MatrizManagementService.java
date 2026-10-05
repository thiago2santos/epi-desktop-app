package br.com.easynr6.gestaoepi.modules.matrix.application;

import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.EpiOpcao;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.LinhaMatriz;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.PerfilOpcao;
import br.com.easynr6.gestaoepi.modules.matrix.application.usecase.AlterarLinhaMatrizUseCase;
import br.com.easynr6.gestaoepi.modules.matrix.application.usecase.AtualizarCaMatrizUseCase;
import br.com.easynr6.gestaoepi.modules.matrix.application.usecase.InativarLinhaMatrizUseCase;
import br.com.easynr6.gestaoepi.modules.matrix.application.usecase.IncluirLinhaMatrizUseCase;
import br.com.easynr6.gestaoepi.modules.matrix.application.usecase.ListarMatrizUseCase;
import br.com.easynr6.gestaoepi.modules.matrix.domain.ModoMatriz;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MatrizManagementService {

  private final IncluirLinhaMatrizUseCase incluirLinhaMatrizUseCase;
  private final AlterarLinhaMatrizUseCase alterarLinhaMatrizUseCase;
  private final AtualizarCaMatrizUseCase atualizarCaMatrizUseCase;
  private final InativarLinhaMatrizUseCase inativarLinhaMatrizUseCase;
  private final ListarMatrizUseCase listarMatrizUseCase;

  public MatrizManagementService(
      IncluirLinhaMatrizUseCase incluirLinhaMatrizUseCase,
      AlterarLinhaMatrizUseCase alterarLinhaMatrizUseCase,
      AtualizarCaMatrizUseCase atualizarCaMatrizUseCase,
      InativarLinhaMatrizUseCase inativarLinhaMatrizUseCase,
      ListarMatrizUseCase listarMatrizUseCase) {
    this.incluirLinhaMatrizUseCase = incluirLinhaMatrizUseCase;
    this.alterarLinhaMatrizUseCase = alterarLinhaMatrizUseCase;
    this.atualizarCaMatrizUseCase = atualizarCaMatrizUseCase;
    this.inativarLinhaMatrizUseCase = inativarLinhaMatrizUseCase;
    this.listarMatrizUseCase = listarMatrizUseCase;
  }

  public Long incluir(Long actorId, PerfilVigente.Tipo tipo, Long perfilId, Long epiId) {
    return incluirLinhaMatrizUseCase.execute(actorId, tipo, perfilId, epiId);
  }

  public void alterar(Long actorId, Long linhaId, ModoMatriz modo, boolean exigeTreinamento) {
    alterarLinhaMatrizUseCase.execute(actorId, linhaId, modo, exigeTreinamento);
  }

  public void atualizarCa(Long actorId, Long linhaId) {
    atualizarCaMatrizUseCase.execute(actorId, linhaId);
  }

  public void inativar(Long actorId, Long linhaId) {
    inativarLinhaMatrizUseCase.execute(actorId, linhaId);
  }

  public List<PerfilOpcao> listarPerfis(Long actorId) {
    return listarMatrizUseCase.perfis(actorId);
  }

  public List<LinhaMatriz> listarLinhas(Long actorId, PerfilVigente.Tipo tipo, Long perfilId) {
    return listarMatrizUseCase.linhas(actorId, tipo, perfilId);
  }

  public List<EpiOpcao> listarCandidatos(Long actorId, PerfilVigente.Tipo tipo, Long perfilId) {
    return listarMatrizUseCase.candidatos(actorId, tipo, perfilId);
  }

  public List<LinhaMatriz> linhasVigentes(Long jobRoleId) {
    return listarMatrizUseCase.linhasVigentes(jobRoleId);
  }
}
