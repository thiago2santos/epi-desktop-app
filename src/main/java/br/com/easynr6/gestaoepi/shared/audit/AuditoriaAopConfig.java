package br.com.easynr6.gestaoepi.shared.audit;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/** Liga o aspecto que grava a falha fora da transação do caso de uso. */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AuditoriaAopConfig {}
