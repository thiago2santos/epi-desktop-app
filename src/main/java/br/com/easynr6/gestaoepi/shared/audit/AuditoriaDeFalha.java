package br.com.easynr6.gestaoepi.shared.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Grava a recusa depois que a transação do caso de uso já desfez a mudança.
 *
 * <p>Fica por fora do {@code @Transactional}. Se a linha fosse inserida na mesma transação, o
 * rollback apagaria a evidência da tentativa.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuditoriaDeFalha {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuditoriaDeFalha.class);

  private final AuditTrail auditTrail;

  public AuditoriaDeFalha(AuditTrail auditTrail) {
    this.auditTrail = auditTrail;
  }

  @Around("@annotation(acao)")
  public Object aoRedor(ProceedingJoinPoint ponto, AcaoAuditada acao) throws Throwable {
    boolean abertaAqui = Correlacao.abrir();
    try {
      return ponto.proceed();
    } catch (RuntimeException ex) {
      registrarFalha(ponto, acao, ex);
      throw ex;
    } finally {
      Correlacao.fechar(abertaAqui);
    }
  }

  private void registrarFalha(ProceedingJoinPoint ponto, AcaoAuditada acao, RuntimeException ex) {
    try {
      Object[] args = ponto.getArgs();
      String codigo = CodigoRegra.extrair(ex.getMessage());
      auditTrail.registrarResultado(
          ator(acao, args),
          nomeAcao(acao, args),
          acao.entidade(),
          alvo(acao, args),
          ResultadoAuditoria.FALHA,
          codigo,
          detalhes(ex));
      if (codigo == null) {
        LOGGER.error("Acao rejeitada sem codigo de regra. correlacao={}", Correlacao.atual(), ex);
      }
    } catch (RuntimeException falhaAoAuditar) {
      LOGGER.error(
          "Nao foi possivel registrar a falha na auditoria. correlacao={}",
          Correlacao.atual(),
          falhaAoAuditar);
    }
  }

  private static String nomeAcao(AcaoAuditada acao, Object[] args) {
    if (!acao.acao().isBlank()) {
      return acao.acao();
    }
    boolean ativo = false;
    for (Object arg : args) {
      if (arg instanceof Boolean valor) {
        ativo = valor;
      }
    }
    if (ativo && !acao.acaoQuandoAtivo().isBlank()) {
      return acao.acaoQuandoAtivo();
    }
    if (!ativo && !acao.acaoQuandoInativo().isBlank()) {
      return acao.acaoQuandoInativo();
    }
    return "ACAO_NEGADA";
  }

  private static Long ator(AcaoAuditada acao, Object[] args) {
    int indice = acao.ator();
    if (indice < 0 || indice >= args.length || !(args[indice] instanceof Long id)) {
      return null;
    }
    return id;
  }

  private static String alvo(AcaoAuditada acao, Object[] args) {
    int indice = acao.alvo();
    if (indice < 0 || indice >= args.length || args[indice] == null) {
      return "-";
    }
    return String.valueOf(args[indice]);
  }

  private static String detalhes(RuntimeException ex) {
    String mensagem = ex.getMessage();
    if (mensagem == null || mensagem.isBlank()) {
      return ex.getClass().getSimpleName();
    }
    return mensagem.trim();
  }
}
