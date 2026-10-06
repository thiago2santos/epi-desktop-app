package br.com.easynr6.gestaoepi.modules.matrix.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.usecase.EmployeeAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository.Definicao;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository.Prazo;
import br.com.easynr6.gestaoepi.modules.matrix.domain.PeriodicidadePolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalvarPeriodicidadeUseCase {

  private final PeriodicidadeRepository periodicidadeRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final PeriodicidadePolicy policy = new PeriodicidadePolicy();

  public SalvarPeriodicidadeUseCase(
      PeriodicidadeRepository periodicidadeRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail) {
    this.periodicidadeRepository = periodicidadeRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
  }

  @AcaoAuditada(acao = "PERIODICIDADE_DEFINIDA", entidade = "PERIODICIDADE")
  @Transactional
  public int execute(Long actorId, List<Definicao> linhas) {
    accessAuthorizer.assertCanManageEmployees(actorId);
    if (linhas == null || linhas.isEmpty()) {
      return 0;
    }
    for (Definicao linha : linhas) {
      policy.requireDias(linha.dias());
      policy.requireAviso(linha.dias(), linha.aviso());
      if (!periodicidadeRepository.temLinhaAtiva(linha.epiId())) {
        throw new IllegalArgumentException("EPI sem linha ativa na matriz.");
      }
    }
    int gravadas = 0;
    for (Definicao linha : linhas) {
      int dias = linha.dias();
      int aviso = linha.aviso();
      Prazo atual = periodicidadeRepository.find(linha.epiId()).orElse(null);
      if (atual != null && atual.dias() == dias && atual.aviso() == aviso) {
        continue;
      }
      periodicidadeRepository.salvar(linha.epiId(), dias, aviso);
      auditTrail.registrarEventoCritico(
          actorId,
          "PERIODICIDADE_DEFINIDA",
          "PERIODICIDADE",
          String.valueOf(linha.epiId()),
          "Periodicidade " + dias + " dias, aviso " + aviso);
      gravadas++;
    }
    return gravadas;
  }
}
