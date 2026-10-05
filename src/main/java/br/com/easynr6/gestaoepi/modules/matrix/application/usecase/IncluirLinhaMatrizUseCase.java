package br.com.easynr6.gestaoepi.modules.matrix.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.usecase.EmployeeAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.CaSugerido;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.EpiAlvo;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.PerfilAlvo;
import br.com.easynr6.gestaoepi.modules.matrix.domain.MatrizPolicy;
import br.com.easynr6.gestaoepi.modules.matrix.domain.ModoMatriz;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncluirLinhaMatrizUseCase {

  private final MatrizRepository matrizRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final MatrizPolicy policy = new MatrizPolicy();

  public IncluirLinhaMatrizUseCase(
      MatrizRepository matrizRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.matrizRepository = matrizRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "MATRIZ_INCLUIDA", entidade = "MATRIZ")
  @Transactional
  public Long execute(Long actorId, PerfilVigente.Tipo tipo, Long perfilId, Long epiId) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    policy.requireInclusao(tipo, perfilId, epiId);
    PerfilAlvo perfil =
        matrizRepository.findPerfil(tipo, perfilId).orElse(new PerfilAlvo(false, false, false));
    policy.assertPodeIncluir(tipo, perfil.encontrado(), perfil.ativo(), perfil.emGheAtivo());
    EpiAlvo epi = matrizRepository.findEpi(epiId).orElse(new EpiAlvo(false, false));
    policy.assertEpiAtivo(epi.encontrado(), epi.ativo());
    CaSugerido ca = matrizRepository.caAtivoAtual(epiId).orElse(null);
    policy.assertTemCaAtivo(ca != null);
    if (ca == null) {
      throw new IllegalArgumentException(
          "MAT-003 Este EPI nao tem CA ativo para entrar na matriz.");
    }
    policy.assertSemLinhaAtiva(matrizRepository.existeLinhaAtiva(tipo, perfilId, epiId));
    Long linhaId =
        matrizRepository.incluir(tipo, perfilId, epiId, ca.id(), ModoMatriz.INDIVIDUAL, false);
    auditTrail.registrarEventoCritico(
        actorId, "MATRIZ_INCLUIDA", "MATRIZ", String.valueOf(linhaId), "EPI incluido: " + epiId);
    return linhaId;
  }
}
