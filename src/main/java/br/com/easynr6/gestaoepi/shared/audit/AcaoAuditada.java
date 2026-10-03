package br.com.easynr6.gestaoepi.shared.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca a ação cujo resultado entra na auditoria quando a chamada é recusada ou falha.
 *
 * <p>O sucesso continua sendo gravado pelo próprio caso de uso, na mesma transação da mudança. A
 * falha é gravada por fora, depois do rollback.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AcaoAuditada {

  /** Ação fixa. Vazio quando o nome depende do boolean de ativar ou inativar. */
  String acao() default "";

  String acaoQuandoAtivo() default "";

  String acaoQuandoInativo() default "";

  String entidade();

  /** Índice do argumento com o id do autor. -1 quando ainda não há usuário autenticado. */
  int ator() default 0;

  /** Índice do argumento com o id do alvo. -1 quando a falha ocorre antes de existir id. */
  int alvo() default -1;
}
