package br.com.easynr6.gestaoepi.modules.issuance.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.Clock;
import br.com.easynr6.gestaoepi.modules.employee.application.usecase.ResolvePerfilVigenteUseCase;
import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import br.com.easynr6.gestaoepi.modules.issuance.application.FornecimentoAccessAuthorizer;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.BaixaFornecimento;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.BaixaFornecimento.Baixa;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.FornecimentoRepository;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.FornecimentoRepository.ItemGravado;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.FornecimentoRepository.LoteLido;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.FornecimentoRepository.Trabalhador;
import br.com.easynr6.gestaoepi.modules.issuance.domain.FornecimentoPolicy;
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoFornecimento;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.LinhaMatriz;
import br.com.easynr6.gestaoepi.modules.stock.domain.LotPolicy;
import br.com.easynr6.gestaoepi.shared.audit.AcaoAuditada;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarFornecimentoUseCase {

  private final FornecimentoRepository fornecimentoRepository;
  private final BaixaFornecimento baixaFornecimento;
  private final MatrizRepository matrizRepository;
  private final ResolvePerfilVigenteUseCase resolvePerfilVigenteUseCase;
  private final FornecimentoAccessAuthorizer accessAuthorizer;
  private final AuditTrail auditTrail;
  private final Clock clock;
  private final FornecimentoPolicy policy = new FornecimentoPolicy();
  private final LotPolicy lotPolicy = new LotPolicy();

  public RegistrarFornecimentoUseCase(
      FornecimentoRepository fornecimentoRepository,
      BaixaFornecimento baixaFornecimento,
      MatrizRepository matrizRepository,
      ResolvePerfilVigenteUseCase resolvePerfilVigenteUseCase,
      FornecimentoAccessAuthorizer accessAuthorizer,
      AuditTrail auditTrail,
      Clock clock) {
    this.fornecimentoRepository = fornecimentoRepository;
    this.baixaFornecimento = baixaFornecimento;
    this.matrizRepository = matrizRepository;
    this.resolvePerfilVigenteUseCase = resolvePerfilVigenteUseCase;
    this.accessAuthorizer = accessAuthorizer;
    this.auditTrail = auditTrail;
    this.clock = clock;
  }

  @AcaoAuditada(acao = "FORNECIMENTO_REGISTRADO", entidade = "FORNECIMENTO")
  @Transactional
  public long execute(Long actorId, Pedido pedido) {
    accessAuthorizer.assertCanRegister(actorId);
    if (pedido == null) {
      throw new IllegalArgumentException("ENT-002 Inclua ao menos um EPI na ficha.");
    }
    policy.termo(pedido.termoAceito());
    List<ItemParaRegistrar> itens = pedido.itens() == null ? List.of() : pedido.itens();
    policy.exigirItens(itens.size());
    policy.lotesUnicos(itens.stream().map(ItemParaRegistrar::loteId).toList());
    Trabalhador trabalhador = trabalhadorAtivo(pedido.employeeId());
    PerfilVigente perfil = resolvePerfilVigenteUseCase.execute(trabalhador.jobRoleId());
    LocalDateTime instante = clock.now();
    LocalDate hoje = instante.toLocalDate();
    boolean podeExcecao = accessAuthorizer.podeRegistrarExcecao(actorId);
    List<ItemPronto> prontos = new ArrayList<>();
    for (ItemParaRegistrar item : itens) {
      prontos.add(preparar(item, trabalhador, perfil, hoje, podeExcecao));
    }
    long fichaId = fornecimentoRepository.inserirFicha(trabalhador, actorId, instante);
    for (ItemPronto pronto : prontos) {
      long itemId = fornecimentoRepository.inserirItem(fichaId, pronto.gravado());
      for (String ca : pronto.cas()) {
        fornecimentoRepository.inserirCa(itemId, ca);
      }
    }
    fornecimentoRepository.inserirTermo(
        fichaId,
        FornecimentoPolicy.TERMO_VERSAO,
        FornecimentoPolicy.TERMO_METODO,
        instante,
        actorId);
    for (ItemPronto pronto : prontos) {
      baixaFornecimento.registrar(baixa(pronto, trabalhador, instante));
    }
    auditTrail.registrarEventoCritico(
        actorId,
        "FORNECIMENTO_REGISTRADO",
        "FORNECIMENTO",
        String.valueOf(fichaId),
        "Ficha " + fichaId);
    return fichaId;
  }

  private Trabalhador trabalhadorAtivo(Long employeeId) {
    return fornecimentoRepository
        .findTrabalhador(employeeId)
        .filter(Trabalhador::ativo)
        .orElseThrow(
            () -> new IllegalArgumentException("ENT-001 Trabalhador nao encontrado ou inativo."));
  }

  private ItemPronto preparar(
      ItemParaRegistrar item,
      Trabalhador trabalhador,
      PerfilVigente perfil,
      LocalDate hoje,
      boolean podeExcecao) {
    policy.motivo(item.motivo(), item.motivoTexto());
    policy.reserva(item.reservaId());
    policy.pedido(item.pedidoId());
    int quantidade = policy.quantidade(item.quantidade());
    LoteLido lote = loteVigente(item, trabalhador, hoje);
    var saldo = lotPolicy.saldo(lote.fisica(), lote.reservada(), lote.validade(), hoje);
    policy.disponivel(quantidade, saldo.disponivel());
    LinhaMatriz linha = linhaDaMatriz(perfil, item.epiId());
    String excecao = policy.excecao(linha != null, podeExcecao, item.excecaoTexto());
    policy.ciencia(
        item.ciencia(), linha != null && linha.exigeTreinamento(), item.dataTreinamento(), hoje);
    List<String> cas =
        policy.cas(
            lote.ca(),
            item.casAdicionais(),
            new HashSet<>(fornecimentoRepository.casAtivos(item.epiId())));
    ItemGravado gravado =
        new ItemGravado(
            lote.id(),
            item.epiId(),
            quantidade,
            item.motivo(),
            item.motivoTexto(),
            item.ciencia(),
            item.dataTreinamento(),
            excecao);
    return new ItemPronto(gravado, cas, lote.custoCentavos());
  }

  private LoteLido loteVigente(ItemParaRegistrar item, Trabalhador trabalhador, LocalDate hoje) {
    LoteLido lote =
        item.loteId() == null ? null : fornecimentoRepository.findLote(item.loteId()).orElse(null);
    if (lote == null || item.epiId() == null) {
      policy.lote(false, false, false, false);
      throw new IllegalArgumentException(
          "ENT-004 Escolha um lote vigente deste EPI. Peca vencida nao pode ser fornecida.");
    }
    policy.lote(
        true,
        lote.epiId() == item.epiId(),
        lote.unitId() == trabalhador.unitId(),
        lotPolicy.vencida(lote.validade(), hoje));
    return lote;
  }

  private LinhaMatriz linhaDaMatriz(PerfilVigente perfil, Long epiId) {
    if (epiId == null) {
      return null;
    }
    return matrizRepository.listarAtivas(perfil.tipo(), perfil.id()).stream()
        .filter(linha -> linha.epiId().equals(epiId))
        .findFirst()
        .orElse(null);
  }

  private static Baixa baixa(ItemPronto pronto, Trabalhador trabalhador, LocalDateTime instante) {
    return new Baixa(
        pronto.gravado().loteId(),
        pronto.gravado().quantidade(),
        pronto.custoCentavos(),
        trabalhador.unidade(),
        trabalhador.setor(),
        trabalhador.funcao(),
        pronto.gravado().epiId(),
        instante);
  }

  public record Pedido(Long employeeId, boolean termoAceito, List<ItemParaRegistrar> itens) {}

  public record ItemParaRegistrar(
      Long epiId,
      Long loteId,
      int quantidade,
      MotivoFornecimento motivo,
      String motivoTexto,
      boolean ciencia,
      LocalDate dataTreinamento,
      String excecaoTexto,
      List<String> casAdicionais,
      Long reservaId,
      Long pedidoId) {}

  private record ItemPronto(ItemGravado gravado, List<String> cas, Integer custoCentavos) {}
}
