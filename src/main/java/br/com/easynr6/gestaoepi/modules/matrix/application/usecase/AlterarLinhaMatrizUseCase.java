package br.com.easynr6.gestaoepi.modules.matrix.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.usecase.EmployeeAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.LinhaGravada;
import br.com.easynr6.gestaoepi.modules.matrix.domain.MatrizPolicy;
import br.com.easynr6.gestaoepi.modules.matrix.domain.ModoMatriz;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlterarLinhaMatrizUseCase {

  private final MatrizRepository matrizRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final MatrizPolicy policy = new MatrizPolicy();

  public AlterarLinhaMatrizUseCase(
      MatrizRepository matrizRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.matrizRepository = matrizRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "MATRIZ_ALTERADA", entidade = "MATRIZ", alvo = 1)
  @Transactional
  public void execute(Long actorId, Long linhaId, ModoMatriz modo, boolean exigeTreinamento) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (modo == null) {
      throw new IllegalArgumentException("MAT-001 Escolha o perfil e o EPI.");
    }
    LinhaGravada linha = linhaAtiva(linhaId);
    if (linha.modo() == modo && linha.exigeTreinamento() == exigeTreinamento) {
      return;
    }
    matrizRepository.alterar(linhaId, linha.caBindingId(), modo, exigeTreinamento);
    auditTrail.registrarEventoCritico(
        actorId,
        "MATRIZ_ALTERADA",
        "MATRIZ",
        String.valueOf(linhaId),
        "Modo " + modo.name() + "; treinamento " + exigeTreinamento);
  }

  private LinhaGravada linhaAtiva(Long linhaId) {
    LinhaGravada linha = matrizRepository.findLinha(linhaId).orElse(null);
    policy.assertLinhaAtiva(linha != null, linha != null && linha.ativa());
    if (linha == null) {
      throw new IllegalArgumentException("MAT-007 Esta linha da matriz nao esta ativa.");
    }
    return linha;
  }
}
