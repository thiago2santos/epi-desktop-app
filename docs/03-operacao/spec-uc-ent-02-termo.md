# Spec curta - UC-ENT-02 (Aceite do termo de responsabilidade)

## Identificacao

- ID: `UC-ENT-02`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-CORE`
- Responsavel: Time Easy NR6
- Status: especificado; ocorre dentro da confirmacao do `UC-ENT-01`
- Norma: NR-6 `6.6.1`

## 1) Contexto

- Problema real:
  - a ficha sem o aceite do termo nao cumpre o registro de responsabilidade do trabalhador.
- Ator principal: `Almoxarife`, `SESMT` ou `Admin`, registrando o que o trabalhador aceitou no balcao.
- Impacto se nao resolver:
  - o fornecimento fecha sem a declaracao do item `6.6.1`.

## 2) Escopo

- Comportamento no escopo:
  - apresentar o texto padrao, com o nome do trabalhador;
  - gravar, na mesma transacao da ficha, versao do texto, instante, metodo e o usuario que registrou;
  - recusar a conclusao se o aceite nao estiver marcado.
- Fora de escopo:
  - biometria e assinatura digital ICP-Brasil;
  - termo avulso, depois da ficha, ou termo editavel pelo operador;
  - login do trabalhador.

## 3) Regras de negocio

1. O aceite e obrigatorio para o `UC-ENT-01` concluir. Nao existe ficha sem termo.
2. Metodo desta versao: `ASSINATURA_MANUAL`. O operador registra que o trabalhador assinou no balcao. Biometria e assinatura digital ficam fora.
3. Texto padrao, versao `TERMO-NR6-01`:

   "Eu, [Nome do Funcionario], declaro que recebi gratuitamente os EPIs constantes nesta ficha, adequados ao risco da minha atividade e em perfeito estado de conservacao. Comprometo-me a utiliza-los estritamente para a finalidade a que se destinam, responsabilizando-me por sua guarda e conservacao, e a comunicar imediatamente a empresa qualquer dano, extravio ou alteracao que os torne improprios para uso, conforme determina a NR-6."

4. O nome exibido e o do trabalhador da ficha. A versao gravada e `TERMO-NR6-01`, para uma troca futura de texto nao reescrever fichas antigas.
5. Desmarcar ou voltar nao grava. O codigo de recusa na confirmacao e `ENT-009`.
6. O termo nao tem auditoria separada. Ele entra no evento `FORNECIMENTO_REGISTRADO`. Se a transacao falha, o termo nao fica orfao.

## 4) Catalogo de erros

Usa `ENT-009` do `UC-ENT-01`.

## 5) Criterios de aceite

- `CA-01`: com o aceite marcado, a ficha nasce com `TERMO-NR6-01`, metodo `ASSINATURA_MANUAL`, instante e usuario operador.
- `CA-02`: sem o aceite, a confirmacao recusa com `ENT-009` e nao grava ficha, movimento nem termo.
- `CA-03`: o texto mostrado contem o nome do trabalhador da ficha.
- `CA-04`: falha posterior na mesma transacao remove o termo junto com a ficha.

## 6) Tela

Passo "Ciencia e termo" do registrar fornecimento, nao um destino proprio.

- O texto aparece por extenso, com o nome ja substituido.
- Uma caixa: "Trabalhador aceitou o termo de responsabilidade (assinatura no balcao)."
- Confirmar fornecimento permanece desabilitado enquanto a caixa estiver desmarcada.
- A frase de recusa, se a confirmacao for tentada sem a caixa, e a do `ENT-009`, sem o codigo.

## 7) Seguranca e auditoria

- Login/senha: nao.
- Evento: o do fornecimento.
- Risco: concluir a ficha com a caixa desmarcada, ou guardar um texto diferente do que foi mostrado.

## 8) Documentacao impactada

- `docs/03-operacao/spec-uc-ent-01-fornecimento.md`
- `docs/03-operacao/matriz-testes-uc-ent-02.md`
- `docs/01-negocio/modelagem-entrega-epi.md`
- `docs/04-uml/entrega/UC-ENT-02 — Registrar aceite do termo.md`
