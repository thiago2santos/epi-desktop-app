package br.com.easynr6.gestaoepi.modules.matrix.application.usecase;

import br.com.easynr6.gestaoepi.modules.employee.application.usecase.ResolvePerfilVigenteUseCase;
import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.LinhaMatriz;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository.Prazo;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository.Trabalhador;
import br.com.easynr6.gestaoepi.modules.matrix.domain.PeriodicidadePolicy;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LerCoberturaUseCase {

  private final PeriodicidadeRepository periodicidadeRepository;
  private final MatrizRepository matrizRepository;
  private final ResolvePerfilVigenteUseCase resolvePerfilVigenteUseCase;
  private final PeriodicidadePolicy policy = new PeriodicidadePolicy();

  public LerCoberturaUseCase(
      PeriodicidadeRepository periodicidadeRepository,
      MatrizRepository matrizRepository,
      ResolvePerfilVigenteUseCase resolvePerfilVigenteUseCase) {
    this.periodicidadeRepository = periodicidadeRepository;
    this.matrizRepository = matrizRepository;
    this.resolvePerfilVigenteUseCase = resolvePerfilVigenteUseCase;
  }

  public List<LeituraCobertura> execute(Long employeeId) {
    Trabalhador trabalhador = periodicidadeRepository.findTrabalhador(employeeId).orElse(null);
    if (trabalhador == null || !trabalhador.ativo()) {
      return List.of();
    }
    PerfilVigente perfil = resolvePerfilVigenteUseCase.execute(trabalhador.jobRoleId());
    LocalDate hoje = LocalDate.now();
    return matrizRepository.listarAtivas(perfil.tipo(), perfil.id()).stream()
        .map(linha -> leitura(employeeId, linha, hoje))
        .toList();
  }

  private LeituraCobertura leitura(Long employeeId, LinhaMatriz linha, LocalDate hoje) {
    Prazo prazo = periodicidadeRepository.find(linha.epiId()).orElse(null);
    Integer dias = prazo == null ? null : prazo.dias();
    Integer aviso = prazo == null ? null : prazo.aviso();
    LocalDate fornecimento =
        periodicidadeRepository.fornecimentoQueConta(employeeId, linha.epiId()).orElse(null);
    return new LeituraCobertura(
        linha.epiId(), linha.epi(), policy.situacao(linha.modo(), dias, aviso, fornecimento, hoje));
  }

  public record LeituraCobertura(Long epiId, String epi, String situacao) {}
}
