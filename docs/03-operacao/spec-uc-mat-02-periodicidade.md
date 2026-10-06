# Spec curta - UC-MAT-02 (Periodicidade de troca)

## Identificacao

- ID: `UC-MAT-02`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-CORE`
- Responsavel: Time Easy NR6
- Status: implementado na tela Periodicidade (`PeriodicidadeManagementView`). A data do fornecimento que conta sai da ficha do `UC-ENT-01`. Sem ficha, a leitura fica Pendente, Sem prazo ou Posto.

## 1) Contexto

- Problema real:
  - a NR-6 pede troca no prazo do uso. Sem o numero de dias, a cobertura nao distingue peca vigente de peca vencida no trabalhador.
- Ator principal: `SESMT`. Tambem `Admin`.
- Impacto se nao resolver:
  - o balcao troca no escuro ou a tela inventa um prazo.

## 2) Escopo

- Comportamento no escopo:
  - gravar, por EPI, a periodicidade em dias e o aviso antecipado em dias;
  - o mesmo par vale para toda funcao que tenha esse EPI na matriz ativa;
  - publicar a leitura de cobertura que o fornecimento mostra ao lado do trabalhador.
- Fora de escopo:
  - prazo de validade da peca (isso e do lote, `UC-LOT-01`);
  - prazo do certificado CA;
  - tela cheia de cobertura da planta (relatorio futuro). O wizard do `UC-ENT-01` consome a leitura definida aqui;
  - forecast e compra.

## 3) Regras de negocio

1. So entra EPI que tenha ao menos uma linha ativa na matriz. EPI sem linha nao aparece.
2. Periodicidade e inteira e maior que zero, em dias. Aviso antecipado e inteiro, maior ou igual a zero, e menor que a periodicidade.
3. Nao ha prazo implicito. Enquanto o SESMT nao salvar, a cobertura diz "Sem prazo". O fornecimento nao e bloqueado por isso.
4. Alterar o numero nao reescreve ficha antiga. O proximo vencimento usa a periodicidade vigente na consulta, contada da data do ultimo fornecimento que ainda conta.
5. Fornecimento que conta: item de ficha sem estorno e sem devolucao. Estorno e devolucao continuam nos casos `UC-POS-02` e `UC-POS-01`; ate eles existirem, todo item gravado conta.
6. Leitura, por trabalhador ativo e por linha ativa do perfil vigente dele, com o relogio da consulta. O perfil vigente e o da regra 9 do `UC-CAD-07`: GHE ativo da funcao, ou a propria funcao:
   - modo Posto: situacao "Posto". Nao vira pendencia pessoal;
   - sem periodicidade: "Sem prazo";
   - sem fornecimento que conte: "Pendente";
   - dias restantes maiores que o aviso: "Vigente";
   - dias restantes maiores que zero e ate o aviso: "Troca em N dias";
   - dias restantes zero ou menos: "Prazo vencido".
7. Dias restantes = periodicidade menos a idade inteira em dias desde a data do fornecimento que conta. A situacao e texto. Cor nao substitui o texto.
8. `Almoxarife` e `Consulta` nao gravam. Auditoria: `PERIODICIDADE_DEFINIDA`, entidade `PERIODICIDADE`.

## 4) Catalogo de erros

| Codigo | Quando | Texto de tela |
|---|---|---|
| `MAT-005` | Dias ausentes, zero, negativos ou nao inteiros | A periodicidade precisa ser um numero inteiro de dias, maior que zero. |
| `MAT-006` | Aviso negativo ou maior ou igual a periodicidade | O aviso antecipado precisa ser zero ou mais, e menor que a periodicidade. |
| `AUTH-004` | Papel sem permissao | Voce nao tem permissao para alterar a periodicidade. |

## 5) Criterios de aceite

- `CA-01`: EPI na matriz, 180 dias e aviso 15, salva e a leitura usa esses numeros.
- `CA-02`: aviso 180 com periodicidade 180 recusa com `MAT-006` e nao grava.
- `CA-03`: EPI sem linha de matriz nao aparece na grade.
- `CA-04`: sem periodicidade salva, a cobertura daquele EPI diz "Sem prazo".
- `CA-05`: ultimo fornecimento que conta ha 10 dias, prazo 180, aviso 15: "Vigente". Ha 170 dias: "Troca em 10 dias". Ha 180 dias: "Prazo vencido". Sem fornecimento: "Pendente".
- `CA-06`: linha em modo Posto aparece como "Posto" mesmo sem fornecimento pessoal.
- `CA-07`: mudar de 180 para 90 nao altera a data gravada na ficha; a consulta seguinte usa 90.

## 6) Tela

Destino ja previsto: "Periodicidade". Titulo sem codigo de caso de uso.

- Uma linha por EPI, nao uma linha repetida por funcao. Ao lado, "Usado em N funcoes".
- Campos: dias e aviso antecipado. Unidade fixa: dias.
- Salvar desabilitado enquanto alguma linha visivel estiver vazia ou invalida. O campo invalido e marcado e a frase diz o que corrigir.
- Vazio: "Nenhum EPI na matriz. Inclua o EPI na matriz do perfil primeiro."
- Sucesso: "Periodicidade salva." Faixa na propria tela.
- O wizard de fornecimento mostra, no painel do trabalhador, EPI e a situacao em texto definida na regra 6.

## 7) Seguranca e auditoria

- Login/senha: nao.
- Evento: `PERIODICIDADE_DEFINIDA`.
- Risco: um prazo silencioso de 180 dias fazer a cobertura mentir.

## 8) Documentacao impactada

- `docs/03-operacao/spec-uc-mat-01-matriz.md`
- `docs/03-operacao/spec-uc-ent-01-fornecimento.md`
- `docs/03-operacao/matriz-testes-uc-mat-02.md`
- `docs/04-uml/UC-MAT-02 — Definir periodicidade.md`
