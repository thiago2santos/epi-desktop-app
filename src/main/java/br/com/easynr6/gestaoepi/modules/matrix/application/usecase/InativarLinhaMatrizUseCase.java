package br.com.easynr6.gestaoepi.modules.matrix.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.usecase.EmployeeAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.LinhaGravada;
import br.com.easynr6.gestaoepi.modules.matrix.domain.MatrizPolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InativarLinhaMatrizUseCase {

  private final MatrizRepository matrizRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final MatrizPolicy policy = new MatrizPolicy();

  public InativarLinhaMatrizUseCase(
      MatrizRepository matrizRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.matrizRepository = matrizRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "MATRIZ_INATIVADA", entidade = "MATRIZ", alvo = 1)
  @Transactional
  public void execute(Long actorId, Long linhaId) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    LinhaGravada linha = matrizRepository.findLinha(linhaId).orElse(null);
    policy.assertLinhaAtiva(linha != null, linha != null && linha.ativa());
    if (linha == null) {
      throw new IllegalArgumentException("MAT-007 Esta linha da matriz nao esta ativa.");
    }
    matrizRepository.inativar(linhaId);
    auditTrail.registrarEventoCritico(
        actorId, "MATRIZ_INATIVADA", "MATRIZ", String.valueOf(linhaId), "Linha inativada.");
  }
}
