package br.com.easynr6.gestaoepi.modules.matrix.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.usecase.EmployeeAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.ResolvePerfilVigenteUseCase;
import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.EpiOpcao;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.LinhaMatriz;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.PerfilOpcao;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListarMatrizUseCase {

  private final MatrizRepository matrizRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final ResolvePerfilVigenteUseCase resolvePerfilVigenteUseCase;

  public ListarMatrizUseCase(
      MatrizRepository matrizRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      ResolvePerfilVigenteUseCase resolvePerfilVigenteUseCase) {
    this.matrizRepository = matrizRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.resolvePerfilVigenteUseCase = resolvePerfilVigenteUseCase;
  }

  public List<PerfilOpcao> perfis(Long actorId) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    return matrizRepository.listPerfis();
  }

  public List<LinhaMatriz> linhas(Long actorId, PerfilVigente.Tipo tipo, Long perfilId) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (tipo == null || perfilId == null) {
      return List.of();
    }
    return matrizRepository.listarAtivas(tipo, perfilId);
  }

  public List<EpiOpcao> candidatos(Long actorId, PerfilVigente.Tipo tipo, Long perfilId) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (tipo == null || perfilId == null) {
      return List.of();
    }
    return matrizRepository.listarCandidatos(tipo, perfilId);
  }

  public List<LinhaMatriz> linhasVigentes(Long jobRoleId) {
    PerfilVigente perfil = resolvePerfilVigenteUseCase.execute(jobRoleId);
    return matrizRepository.listarAtivas(perfil.tipo(), perfil.id());
  }
}
