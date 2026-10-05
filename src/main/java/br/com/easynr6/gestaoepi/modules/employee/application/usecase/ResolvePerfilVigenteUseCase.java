package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.port.GheRepository;
import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import org.springframework.stereotype.Service;

@Service
public class ResolvePerfilVigenteUseCase {

  private final GheRepository gheRepository;

  public ResolvePerfilVigenteUseCase(GheRepository gheRepository) {
    this.gheRepository = gheRepository;
  }

  public PerfilVigente execute(Long jobRoleId) {
    if (jobRoleId == null) {
      throw new IllegalArgumentException(
          "CAD-054 Funcao inativa, de outra unidade ou inexistente.");
    }
    return gheRepository
        .findMembership(jobRoleId)
        .filter(membership -> membership.active())
        .map(membership -> PerfilVigente.doGhe(membership.gheId()))
        .orElseGet(() -> PerfilVigente.daFuncao(jobRoleId));
  }
}
