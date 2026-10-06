package br.com.easynr6.gestaoepi.modules.issuance.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.Clock;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.ResolvePerfilVigenteUseCase;
import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import br.com.easynr6.gestaoepi.modules.issuance.application.FornecimentoAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.FornecimentoRepository;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.FornecimentoRepository.LoteLido;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.FornecimentoRepository.Trabalhador;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.EpiOpcao;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.LinhaMatriz;
import br.com.easynr6.gestaoepi.modules.stock.domain.LotPolicy;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConsultarFornecimentoUseCase {

  private final FornecimentoRepository fornecimentoRepository;
  private final MatrizRepository matrizRepository;
  private final ResolvePerfilVigenteUseCase resolvePerfilVigenteUseCase;
  private final FornecimentoAccessAuthorizer accessAuthorizer;
  private final Clock clock;
  private final LotPolicy lotPolicy = new LotPolicy();

  public ConsultarFornecimentoUseCase(
      FornecimentoRepository fornecimentoRepository,
      MatrizRepository matrizRepository,
      ResolvePerfilVigenteUseCase resolvePerfilVigenteUseCase,
      FornecimentoAccessAuthorizer accessAuthorizer,
      Clock clock) {
    this.fornecimentoRepository = fornecimentoRepository;
    this.matrizRepository = matrizRepository;
    this.resolvePerfilVigenteUseCase = resolvePerfilVigenteUseCase;
    this.accessAuthorizer = accessAuthorizer;
    this.clock = clock;
  }

  public List<Trabalhador> buscar(Long actorId, String texto) {
    accessAuthorizer.assertCanRegister(actorId);
    return fornecimentoRepository.buscarAtivos(texto);
  }

  public List<LinhaMatriz> itensDaFuncao(Long actorId, Long employeeId) {
    accessAuthorizer.assertCanRegister(actorId);
    Trabalhador trabalhador = fornecimentoRepository.findTrabalhador(employeeId).orElse(null);
    if (trabalhador == null || !trabalhador.ativo()) {
      return List.of();
    }
    PerfilVigente perfil = resolvePerfilVigenteUseCase.execute(trabalhador.jobRoleId());
    return matrizRepository.listarAtivas(perfil.tipo(), perfil.id());
  }

  public List<EpiOpcao> episForaDaMatriz(Long actorId, Long employeeId) {
    accessAuthorizer.assertCanRegister(actorId);
    if (!accessAuthorizer.podeRegistrarExcecao(actorId)) {
      return List.of();
    }
    Trabalhador trabalhador = fornecimentoRepository.findTrabalhador(employeeId).orElse(null);
    if (trabalhador == null || !trabalhador.ativo()) {
      return List.of();
    }
    PerfilVigente perfil = resolvePerfilVigenteUseCase.execute(trabalhador.jobRoleId());
    return matrizRepository.listarCandidatos(perfil.tipo(), perfil.id());
  }

  public List<LoteVisivel> lotes(Long actorId, Long unitId, Long epiId) {
    accessAuthorizer.assertCanRegister(actorId);
    LocalDate hoje = clock.now().toLocalDate();
    return fornecimentoRepository.listarLotes(unitId, epiId).stream()
        .map(lote -> visivel(lote, hoje))
        .toList();
  }

  private LoteVisivel visivel(LoteLido lote, LocalDate hoje) {
    var saldo = lotPolicy.saldo(lote.fisica(), lote.reservada(), lote.validade(), hoje);
    return new LoteVisivel(
        lote.id(),
        lote.codigo(),
        lote.ca(),
        lote.validade(),
        saldo.fisica(),
        saldo.reservada(),
        saldo.disponivel(),
        saldo.situacao(),
        lote.custoCentavos());
  }

  public record LoteVisivel(
      long id,
      String codigo,
      String ca,
      LocalDate validade,
      int fisica,
      int reservada,
      int disponivel,
      String situacao,
      Integer custoCentavos) {}
}
