# Spec curta - UC-ADM-03 (Parametros da instalacao)

## Identificacao

- ID: `UC-ADM-03`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-ADM`
- Responsavel: Time Easy NR6
- Status: especificado; implementacao nao iniciada

## 1) Contexto

- Problema real:
  - a barra da janela e os filtros precisam abrir na unidade desta instalacao, em vez de um nome fixo na tela.
- Ator principal: `Admin`.
- Impacto se nao resolver:
  - o operador le "Itupeva" numa base que esta em outra planta.

## 2) Escopo

- Comportamento no escopo:
  - escolher a unidade padrao dentre as unidades ativas;
  - mostrar, so leitura, a razao social e o CNPJ da empresa do seed;
  - mostrar, so leitura, o horario da carga CAEPI ja fechado no `UC-CAE-01`.
- Fora de escopo:
  - editar razao social, CNPJ ou criar empresa (`UC-CAD-01`);
  - trocar o modo demonstracao por cliente-servidor num combo;
  - mudar o horario da CAEPI, o fuso, os motivos de fornecimento, o metodo de assinatura ou o texto do termo;
  - mover estoque ou reescrever ficha ao trocar a unidade padrao.

## 3) Regras de negocio

1. A unidade padrao e uma unidade ativa. Inativa ou inexistente recusa.
2. Trocar a unidade padrao nao altera lote, ficha, matriz nem trabalhador. So muda o valor inicial dos filtros e o texto da barra de status.
3. Sem unidade padrao gravada, a barra diz "Nenhuma unidade padrao." e as telas operacionais abrem sem unidade selecionada.
4. Razao social e CNPJ raiz aparecem como estao na empresa. O CNPJ da unidade padrao aparece formatado, tambem so leitura.
5. O horario exibido da CAEPI e 06:00 em `America/Sao_Paulo`, como decisao do `UC-CAE-01`. O campo nao edita.
6. So `Admin` grava. Os demais recebem `AUTH-004`.
7. Auditoria: `PARAMETRO_ALTERADO`, entidade `PARAMETRO`, com a unidade anterior e a nova.

## 4) Catalogo de erros

| Codigo | Quando | Texto de tela |
|---|---|---|
| `ADM-001` | Unidade ausente ou inativa | Escolha uma unidade ativa. |
| `AUTH-004` | Papel sem permissao | Voce nao tem permissao para alterar parametros. |

## 5) Criterios de aceite

- `CA-01`: Admin grava uma unidade ativa. A barra passa a mostrar o nome dela. O filtro de lotes abre nessa unidade.
- `CA-02`: unidade inativa recusa com `ADM-001` e a padrao anterior permanece.
- `CA-03`: razao social, CNPJ e horario CAEPI nao sao editaveis.
- `CA-04`: trocar a unidade padrao nao muda o saldo de nenhum lote.
- `CA-05`: sem padrao gravado, a barra diz "Nenhuma unidade padrao."
- `CA-06`: `SESMT` recebe `AUTH-004`.
- `CA-07`: a auditoria registra a unidade anterior e a nova.

## 6) Tela

Destino ja previsto: "Parametros". Titulo sem codigo de caso de uso.

- Bloco de leitura: razao social, CNPJ da empresa, horario da CAEPI.
- Um campo editavel: unidade padrao, combo das unidades ativas, rotulo com o nome. Se houver homonimo, o rotulo inclui o CNPJ formatado.
- Salvar desabilitado ate haver uma unidade escolhida.
- Sucesso: "Unidade padrao salva." Faixa na propria tela.
- A barra de status usa esse nome. Nao ha frase fixa de planta nem de carga CAEPI inventada: o estado da CAEPI continua o banner do `UC-CAE-01`.

## 7) Seguranca e auditoria

- Login/senha: nao.
- Evento: `PARAMETRO_ALTERADO`.
- Risco: deixar a barra mentir a planta, ou abrir mao de motivo e termo para texto livre.

## 8) Documentacao impactada

- `docs/03-operacao/spec-uc-cad-01-unidade.md`
- `docs/03-operacao/spec-uc-cae-01-importar-base-caepi.md`
- `docs/03-operacao/matriz-testes-uc-adm-03.md`
- `docs/04-uml/casos-de-uso-uml.md`
