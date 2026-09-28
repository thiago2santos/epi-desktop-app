# Feature em estudo — Dossiê jurídico e auditoria

## Status

Em estudo/avaliação.

## Contexto

Além da operação diária de EPI, existe uma necessidade potencial de resposta rápida em:

- processos trabalhistas;
- fiscalizações;
- auditorias internas/externas.

A ideia é permitir extração de um "dossiê" com poucos cliques, reunindo evidências completas de fornecimento e controle.

## Hipótese de valor

Se o sistema gerar um dossiê estruturado, com visão jurídica e visão de auditoria, a empresa reduz tempo de resposta, melhora qualidade da defesa e aumenta confiança nas evidências apresentadas.

## Perfis de acesso sugeridos

1. **Jurídico** (novo papel em estudo)
   - acesso de leitura a registros e evidências;
   - geração de dossiê com foco em defesa da empresa.

2. **Auditoria** (evolução do papel de consulta)
   - acesso de leitura ampliada para análise de conformidade;
   - geração de dossiê técnico de verificação de registros.

## Conceito da feature

## Dossiê jurídico (visão defesa)

Pacote com recorte por trabalhador/período/processo contendo:

- dados do trabalhador e função no período;
- histórico de entregas, devoluções e estornos;
- vínculo de EPI com CA e lote;
- termo de responsabilidade e validações de recebimento;
- trilha cronológica de eventos e responsáveis;
- relatórios exportáveis em PDF.

## Dossiê de auditoria (visão conformidade)

Pacote com recorte por unidade/setor/período contendo:

- cobertura por trabalhador ativo (matriz x vigente);
- pendências de devolução;
- itens fora da matriz com justificativa;
- consistência de CA/lote/validade;
- exceções operacionais e frequência;
- indicadores de integridade de registro.

## Escopo inicial sugerido (MVP da feature)

1. Tela "Gerar dossiê" com dois modos:
   - Jurídico
   - Auditoria
2. Filtros mínimos:
   - unidade
   - trabalhador (quando aplicável)
   - período
3. Saída:
   - PDF consolidado
   - (opcional) pacote ZIP com anexos/relatórios

## Regras e cuidados

- Somente leitura; sem permissão de alterar registros.
- Log obrigatório de geração do dossiê (quem gerou, quando e qual filtro).
- Garantir rastreabilidade do recorte aplicado na extração.
- Revisar políticas de LGPD para acesso a dados pessoais.

## Dependências para viabilização

- maturidade dos relatórios base (ficha, histórico, cobertura, pendências);
- organização da trilha de auditoria para exportação;
- definição final de perfis e matriz de permissões.

## Perguntas abertas para validação

1. O termo "dossiê" é adequado para os usuários ou preferem "pacote de evidências"?
2. Jurídico terá usuário próprio no sistema ou operação via Admin/Consulta avançada?
3. Quais documentos mínimos o jurídico considera indispensáveis numa defesa?
4. Qual periodicidade de auditoria exige geração desse pacote?
5. Deve existir assinatura/carimbo de integridade do arquivo exportado?

## Próximo passo recomendado

Levar esta proposta para conversa com key user, jurídico e (se possível) auditoria interna para validar:

- prioridade de negócio;
- escopo mínimo viável;
- critérios de aceite da feature.

