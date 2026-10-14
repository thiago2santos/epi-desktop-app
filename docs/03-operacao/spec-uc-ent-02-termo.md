# Spec curta - UC-ENT-02 (Aceite do termo de responsabilidade)

## Identificacao

- ID: `UC-ENT-02`
- Tipo: `UC`
- Iniciativa/Epico: `INI-01` / `EP-CORE`
- Responsavel: Time Easy NR6
- Status: implementado no passo Ciencia e termo do `UC-ENT-01`. A confirmacao confere o hash e grava o metodo `SENHA`.
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
  - receber a senha de recebimento do trabalhador e conferir com o hash do `UC-CAD-03`;
  - gravar, na mesma transacao da ficha, versao do texto, instante, metodo `SENHA` e o usuario operador.
- Fora de escopo:
  - biometria, facial e assinatura digital ICP-Brasil;
  - termo avulso, depois da ficha, ou termo editavel pelo operador;
  - login do trabalhador. A senha so autentica este recebimento.

## 3) Regras de negocio

1. O aceite e obrigatorio para o `UC-ENT-01` concluir. Nao existe ficha sem termo.
2. Metodo desta versao: `SENHA`. O trabalhador digita a senha de recebimento no balcao. O operador nao marca aceite no lugar dele. Biometria e assinatura digital ficam fora. `ASSINATURA_MANUAL` deixa de ser o metodo gravado.
3. Texto padrao, versao `TERMO-NR6-01`:

   "Eu, [Nome do Funcionario], declaro que recebi gratuitamente os EPIs constantes nesta ficha, adequados ao risco da minha atividade e em perfeito estado de conservacao. Comprometo-me a utiliza-los estritamente para a finalidade a que se destinam, responsabilizando-me por sua guarda e conservacao, e a comunicar imediatamente a empresa qualquer dano, extravio ou alteracao que os torne improprios para uso, conforme determina a NR-6."

4. O nome exibido e o do trabalhador da ficha. A versao gravada e `TERMO-NR6-01`, para uma troca futura de texto nao reescrever fichas antigas.
5. Voltar nao grava. Sem hash no trabalhador, a confirmacao recusa com `ENT-014`. Senha em branco ou diferente do hash recusa com `ENT-015`. Nada da ficha permanece.
6. O termo nao tem auditoria separada. Ele entra no evento `FORNECIMENTO_REGISTRADO`. Se a transacao falha, o termo nao fica orfao.

## 4) Catalogo de erros

Usa `ENT-014` e `ENT-015` do `UC-ENT-01`. `ENT-009` permanece para confirmacao sem o fluxo do termo.

## 5) Criterios de aceite

- `CA-01`: com a senha certa, a ficha nasce com `TERMO-NR6-01`, metodo `SENHA`, instante e usuario operador. A senha em claro nao fica no termo.
- `CA-02`: senha em branco ou diferente do hash recusa com `ENT-015` e nao grava ficha, movimento nem termo.
- `CA-05`: trabalhador sem hash recusa com `ENT-014` e nao grava ficha, movimento nem termo.
- `CA-03`: o texto mostrado contem o nome do trabalhador da ficha.
- `CA-04`: falha posterior na mesma transacao remove o termo junto com a ficha.

## 6) Tela

Passo "Ciencia e termo" do registrar fornecimento, nao um destino proprio.

- O texto aparece por extenso, com o nome ja substituido.
- Campo mascarado: "Senha de recebimento". Tooltip: "O trabalhador digita a senha definida pelo SESMT. Ela nao abre o sistema."
- Nao ha caixa de assinatura no balcao. Proximo e Confirmar fornecimento permanecem desabilitados enquanto o campo estiver vazio.
- Senha errada, na confirmacao, mostra a frase do `ENT-015`, sem o codigo. Trabalhador sem senha no cadastro mostra a frase do `ENT-014`.

## 7) Seguranca e auditoria

- Login/senha: o trabalhador nao tem login. A senha de recebimento so confere esta ficha.
- Evento: o do fornecimento.
- Risco: concluir a ficha com a senha do operador no lugar da do trabalhador, ou guardar um texto diferente do que foi mostrado.

## 8) Documentacao impactada

- `docs/03-operacao/spec-uc-ent-01-fornecimento.md`
- `docs/03-operacao/spec-uc-cad-03-senha-recebimento.md`
- `docs/03-operacao/matriz-testes-uc-ent-02.md`
- `docs/01-negocio/modelagem-entrega-epi.md`
- `docs/04-uml/entrega/UC-ENT-02 — Registrar aceite do termo.md`
