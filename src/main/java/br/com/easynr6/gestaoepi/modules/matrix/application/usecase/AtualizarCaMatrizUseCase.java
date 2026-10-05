package br.com.easynr6.gestaoepi.modules.matrix.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.usecase.EmployeeAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.CaSugerido;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.LinhaGravada;
import br.com.easynr6.gestaoepi.modules.matrix.domain.MatrizPolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AtualizarCaMatrizUseCase {

  private final MatrizRepository matrizRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final MatrizPolicy policy = new MatrizPolicy();

  public AtualizarCaMatrizUseCase(
      MatrizRepository matrizRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.matrizRepository = matrizRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "MATRIZ_ALTERADA", entidade = "MATRIZ", alvo = 1)
  @Transactional
  public void execute(Long actorId, Long linhaId) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    LinhaGravada linha = matrizRepository.findLinha(linhaId).orElse(null);
    policy.assertLinhaAtiva(linha != null, linha != null && linha.ativa());
    if (linha == null) {
      throw new IllegalArgumentException("MAT-007 Esta linha da matriz nao esta ativa.");
    }
    CaSugerido ca = matrizRepository.caAtivoAtual(linha.epiId()).orElse(null);
    policy.assertTemCaAtivo(ca != null);
    if (ca == null) {
      throw new IllegalArgumentException(
          "MAT-003 Este EPI nao tem CA ativo para entrar na matriz.");
    }
    if (ca.id().equals(linha.caBindingId())) {
      return;
    }
    matrizRepository.alterar(linhaId, ca.id(), linha.modo(), linha.exigeTreinamento());
    auditTrail.registrarEventoCritico(
        actorId,
        "MATRIZ_ALTERADA",
        "MATRIZ",
        String.valueOf(linhaId),
        "CA esperado: " + ca.numero());
  }
}
