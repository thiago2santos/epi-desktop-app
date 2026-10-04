package br.com.easynr6.gestaoepi.modules.caepi.application;

import br.com.easynr6.gestaoepi.modules.caepi.domain.CaepiParser.Inspecao;

/** Leitura do arquivo antes de gravar. Não publica carga nem auditoria. */
public record CaepiPrevia(String nome, int bytes, String sha256, Inspecao inspecao) {}
