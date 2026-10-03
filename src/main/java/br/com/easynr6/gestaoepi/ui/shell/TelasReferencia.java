package br.com.easynr6.gestaoepi.ui.shell;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Telas do shell Easy NR6. */
public final class TelasReferencia {

  private TelasReferencia() {}

  public static Node criar(Destino destino, Consumer<Destino> navegar) {
    return switch (destino) {
      case DASHBOARD -> dashboard(navegar);
      case ENTREGA -> entrega();
      case DEVOLUCAO -> devolucao();
      case ESTORNO -> estorno();
      case HISTORICO -> historico();
      case SOLICITAR -> solicitar();
      case FILA -> fila();
      case TRABALHADORES -> trabalhadores();
      case SETORES -> setores();
      case EPI -> catalogoEpi(navegar);
      case CA -> caPorEpi(navegar);
      case LOTES -> lotes();
      case MATRIZ -> matriz();
      case PERIODICIDADE -> periodicidade();
      case RELATORIOS -> relatorios(navegar);
      case COBERTURA -> cobertura();
      case PENDENCIAS -> pendencias();
      case AUDITORIA -> auditoria();
      case USUARIOS -> usuarios();
      case PARAMETROS -> parametros();
      case CAEPI -> caepi();
    };
  }

  private static Node dashboard(Consumer<Destino> navegar) {
    ReferenciaPage page =
        ReferenciaPage.of("FEAT · home por papel", "Início", "Prioridades operacionais de hoje");
    FlowPane metrics = new FlowPane(12, 12);
    metrics
        .getChildren()
        .addAll(
            metric("3", "Entregas pendentes", Destino.ENTREGA, navegar),
            metric("1", "Fora da matriz", Destino.FILA, navegar),
            metric("2", "Lotes a vencer", Destino.LOTES, navegar),
            metric("CAEPI", "Carga válida", Destino.CAEPI, navegar));
    HBox atalhos =
        new HBox(
            8,
            link("Registrar fornecimento", Destino.ENTREGA, navegar),
            link("Devolução", Destino.DEVOLUCAO, navegar),
            link("Lotes", Destino.LOTES, navegar),
            link("Cobertura", Destino.COBERTURA, navegar));
    page.section(metrics).section(panel("Atalhos", atalhos));
    return ReferenciaPage.scroll(page);
  }

  private static Node entrega() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-ENT-01", "Registrar fornecimento de EPI", "Passo 1 de 5 — Trabalhador");
    HBox passos =
        new HBox(
            8,
            passo("1 · Trabalhador", true),
            passo("2 · Itens da matriz", false),
            passo("3 · Lote e quantidade", false),
            passo("4 · Ciência e termo", false),
            passo("5 · Revisão", false));
    page.section(passos)
        .section(
            split(
                panel(
                    "Buscar trabalhador ativo",
                    campo("Matrícula ou nome", "000123"),
                    botao("Buscar", false)),
                panel(
                    "Cobertura (matriz × vigente)",
                    new Label("Busque um trabalhador para ver EPI exigido e situação."))))
        .legal(
            "Registro legal: ao confirmar, grava fornecimento imutável. Correções apenas por estorno.");
    return ReferenciaPage.scroll(page);
  }

  private static Node trabalhadores() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-CAD-03",
            "Trabalhadores",
            "Matrícula, setor e função — dados compartilhados com cadastros organizacionais");
    page.section(
        split(
            panel("Lista", campo("Filtrar", ""), botao("Novo trabalhador", true)),
            panel(
                "Novo trabalhador",
                campo("Matrícula", ""),
                campo("Nome completo", ""),
                campo("Setor", "Produção"),
                campo("Função", "Operador de produção"),
                botao("Salvar", true))));
    page.table(
        new String[] {"Matrícula", "Nome", "Setor", "Função", "Status"},
        new String[][] {
          {"000123", "Ana Souza", "Produção", "Operador de produção", "Ativo"},
          {"000124", "Carlos Lima", "Manutenção", "Eletricista", "Ativo"}
        });
    return ReferenciaPage.scroll(page);
  }

  private static Node setores() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-CAD-02",
            "Setores e funções",
            "Estrutura organizacional para empregados e matriz de EPI. GHE fica na matriz.");
    Label hint =
        new Label(
            "1. Setores: nome e Cadastrar. 2. Funções: setor ativo e nome da função. 3. Inativar em vez de apagar.");
    hint.setWrapText(true);
    hint.getStyleClass().add(Enr6Styles.BANNER_INFO);
    TabPane abas = new TabPane();
    abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    abas.getTabs()
        .add(
            new Tab(
                "Setores", panel("Setor", campo("Nome", "Produção"), botao("Cadastrar", true))));
    abas.getTabs()
        .add(
            new Tab(
                "Funções",
                panel(
                    "Função",
                    campo("Setor ativo", "Produção"),
                    campo("Nome", "Operador de produção"),
                    botao("Cadastrar", true))));
    page.section(hint).section(abas);
    page.table(
        new String[] {"Setor", "Função", "Status"},
        new String[][] {
          {"Produção", "Operador de produção", "Ativo"},
          {"Manutenção", "Eletricista", "Ativo"}
        });
    return ReferenciaPage.scroll(page);
  }

  private static Node catalogoEpi(Consumer<Destino> navegar) {
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-CAD-04 · Anexo I",
            "Catálogo de EPI",
            "Classificação Anexo I, fabricante e status operacional. Ativar exige CA ativo.");
    page.section(
            split(
                panel("Lista", campo("Buscar", ""), botao("Novo EPI", true)),
                panel(
                    "Novo EPI",
                    campo("Código", ""),
                    campo("Descrição", "Luva nitrílica"),
                    campo("Grupo Anexo I", "A"),
                    campo("Fabricante", ""),
                    botao("Salvar", true),
                    link("Gerenciar CA", Destino.CA, navegar))))
        .table(
            new String[] {"Código", "Descrição", "Anexo", "CA ativo", "Status"},
            new String[][] {
              {"LUV-NIT", "Luva nitrílica", "A", "12345", "Ativo"},
              {"CAP-B", "Capacete classe B", "B", "99881", "Ativo"}
            });
    return ReferenciaPage.scroll(page);
  }

  private static Node caPorEpi(Consumer<Destino> navegar) {
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-CAD-05",
            "Vínculo de CA por EPI",
            "Situação, vigência e evidência da consulta oficial (CAEPI).");
    Label hint =
        new Label(
            "Selecione o EPI, informe o CA, a situação, a vigência e a evidência da consulta. Vincular não apaga o histórico.");
    hint.setWrapText(true);
    hint.getStyleClass().add(Enr6Styles.BANNER_INFO);
    page.section(hint)
        .section(link("Ver importação CAEPI", Destino.CAEPI, navegar))
        .section(
            panel(
                "Vínculo",
                campo("EPI", "LUV-NIT — Luva nitrílica"),
                campo("Número do CA", "12345"),
                campo("Situação", "Válido"),
                campo("Evidência", "Carga CAEPI 28/09/2026 · CA válido"),
                botao("Vincular", true)))
        .table(
            new String[] {"CA", "Situação", "Vigência", "Evidência"},
            new String[][] {{"12345", "Válido", "01/01/2026 — 01/01/2028", "Consulta CAEPI"}});
    return ReferenciaPage.scroll(page);
  }

  private static Node auditoria() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "Append-only", "Trilha de auditoria", "Eventos sensíveis — somente consulta");
    page.section(
            new HBox(
                8,
                campo("De", ""),
                campo("Até", ""),
                campo("Ação", "Todas"),
                botao("Filtrar", true),
                botao("Exportar", false)))
        .table(
            new String[] {"Instante", "Usuário", "Ação", "Entidade", "ID", "Detalhes"},
            new String[][] {
              {"28/09/2026 09:12", "admin", "LOGIN", "USUARIO", "1", "Login realizado"},
              {"28/09/2026 09:40", "sesmt", "ACESSO_MODULO", "MODULO", "EPI", "Catálogo EPI"}
            });
    return ReferenciaPage.scroll(page);
  }

  private static Node usuarios() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-ADM-01/02", "Usuários e papéis", "RBAC, credencial e troca obrigatória.");
    page.section(
            split(
                panel("Lista", botao("Novo usuário", true)),
                panel(
                    "Selecione um usuário",
                    campo("Nome exibido", "Administrador"),
                    campo("Papel", "Admin"),
                    botao("Salvar", true),
                    botao("Reset senha", false))))
        .table(
            new String[] {"Login", "Nome", "Papel", "Status"},
            new String[][] {
              {"admin", "Administrador", "Admin", "Ativo"},
              {"sesmt", "SESMT", "SESMT", "Ativo"},
              {"almox", "Almoxarife", "Almoxarife", "Ativo"}
            });
    return ReferenciaPage.scroll(page);
  }

  private static Node devolucao() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "Pós-entrega",
            "Registrar devolução ou descarte",
            "Vinculado a item entregue — registro imutável");
    page.section(
            panel(
                "Buscar entrega",
                campo("Trabalhador ou entrega", "000123 ou ENT-2026-8840"),
                botao("Buscar itens entregues", false)))
        .table(
            new String[] {"Data", "EPI", "Qtd", "Status"},
            new String[][] {
              {"12/09/2026", "Luva nitrílica", "1", "Entregue"},
              {"02/08/2026", "Capacete classe B", "1", "Entregue"}
            })
        .legal("Devolução gera novo evento; não apaga a entrega original.");
    return ReferenciaPage.scroll(page);
  }

  private static Node estorno() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "Estorno formal",
            "Estornar registro de fornecimento",
            "Novo evento. A entrega original permanece na trilha.");
    page.section(
            panel(
                "Estorno",
                campo("Entrega", "ENT-2026-8840"),
                campo("Motivo", "Registro lançado no trabalhador errado"),
                botao("Registrar estorno", true)))
        .legal("Estorno não edita nem apaga o fornecimento.");
    return ReferenciaPage.scroll(page);
  }

  private static Node historico() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "Histórico por trabalhador",
            "Histórico de fornecimento",
            "Entregas efetivas, separadas de solicitações em aberto.");
    page.section(campo("Matrícula", "000123"))
        .table(
            new String[] {"Data", "EPI", "CA", "Lote", "Evento"},
            new String[][] {
              {"12/09/2026", "Luva nitrílica", "12345", "A-19", "Fornecimento"},
              {"02/08/2026", "Capacete classe B", "99881", "C-02", "Fornecimento"}
            });
    return ReferenciaPage.scroll(page);
  }

  private static Node solicitar() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-SOL-01 · Gestor",
            "Solicitar EPI para trabalhador",
            "Solicitação registrada; isto não é entrega nem reserva de estoque.");
    page.section(
        panel(
            "Pedido",
            campo("Trabalhador do escopo", "000123 · Ana Souza"),
            campo("EPI", "Protetor auricular"),
            campo("Quantidade", "1"),
            campo("Motivo", "Reposição por dano"),
            botao("Enviar solicitação", true)));
    return ReferenciaPage.scroll(page);
  }

  private static Node fila() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-SOL-01 · Fila",
            "Fila de solicitações",
            "Demanda central. Atender abre o fornecimento; não baixa estoque aqui.");
    page.table(
        new String[] {"Pedido", "Trabalhador", "EPI", "Qtd", "Estado"},
        new String[][] {
          {"SOL-104", "Ana Souza", "Protetor auricular", "1", "Aberta"},
          {"SOL-098", "Carlos Lima", "Luva nitrílica", "2", "Fora da matriz · SESMT"}
        });
    return ReferenciaPage.scroll(page);
  }

  private static Node lotes() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-LOT-01/02 · Lotes", "Lotes e saldos", "CA na compra · validade da peça · saldo");
    GridPane form = new GridPane();
    form.setHgap(12);
    form.setVgap(8);
    form.add(new Label("EPI"), 0, 0);
    ComboBox<String> epi = new ComboBox<>();
    epi.getItems().addAll("LUV-NIT — Luva nitrílica", "CAP-B — Capacete classe B");
    epi.getSelectionModel().selectFirst();
    form.add(epi, 1, 0);
    form.add(new Label("Nº CA"), 0, 1);
    form.add(new TextField("12345"), 1, 1);
    form.add(new Label("Validade da peça"), 0, 2);
    form.add(new DatePicker(), 1, 2);
    form.add(botao("Registrar lote", true), 1, 3);
    page.section(panel("Registrar recebimento", form))
        .table(
            new String[] {"Lote", "EPI", "Saldo", "Validade da peça", "Alerta"},
            new String[][] {
              {"A-19", "Luva nitrílica", "40", "15/03/2027", "OK"},
              {"C-02", "Capacete classe B", "6", "20/10/2026", "30 dias"}
            });
    return ReferenciaPage.scroll(page);
  }

  private static Node matriz() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-MAT-01 · Matriz",
            "Matriz função / GHE × EPI",
            "EPI exigido pela função vigente. Fora da matriz segue para o SESMT.");
    page.table(
        new String[] {"Função", "EPI", "Obrigatório", "Vigência"},
        new String[][] {
          {"Operador de produção", "Protetor auricular", "Sim", "01/01/2026 —"},
          {"Eletricista", "Capacete classe B", "Sim", "01/01/2026 —"},
          {"Eletricista", "Luva isolante", "Sim", "01/01/2026 —"}
        });
    return ReferenciaPage.scroll(page);
  }

  private static Node periodicidade() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "Regras · Periodicidade",
            "Periodicidade de troca",
            "Prazo de substituição por função e EPI. Não prova uso efetivo.");
    page.table(
        new String[] {"Função", "EPI", "Prazo", "Unidade"},
        new String[][] {
          {"Operador de produção", "Protetor auricular", "180", "dias"},
          {"Eletricista", "Luva isolante", "365", "dias"}
        });
    return ReferenciaPage.scroll(page);
  }

  private static Node relatorios(Consumer<Destino> navegar) {
    ReferenciaPage page =
        ReferenciaPage.of(
            "M3 · Hub relatórios",
            "Relatórios",
            "Ficha de fornecimento e consultas para fiscalização.");
    page.section(
        new HBox(
            8,
            link("Cobertura", Destino.COBERTURA, navegar),
            link("Pendências", Destino.PENDENCIAS, navegar),
            link("Histórico", Destino.HISTORICO, navegar)));
    return ReferenciaPage.scroll(page);
  }

  private static Node cobertura() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "Cobertura matriz × vigente",
            "Cobertura de EPI",
            "Quem está com o EPI exigido da função ainda vigente.");
    page.table(
        new String[] {"Trabalhador", "Função", "EPI exigido", "Situação"},
        new String[][] {
          {"Ana Souza", "Operador de produção", "Protetor auricular", "Coberto"},
          {"Carlos Lima", "Eletricista", "Luva isolante", "Descoberto"}
        });
    return ReferenciaPage.scroll(page);
  }

  private static Node pendencias() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "Pendências operacionais",
            "Pendências",
            "Trocas vencidas, exceções e solicitações paradas.");
    page.table(
        new String[] {"Tipo", "Quem", "Detalhe", "Desde"},
        new String[][] {
          {"Troca", "Carlos Lima", "Luva isolante vencida", "01/09/2026"},
          {"Exceção", "SOL-098", "Fora da matriz, aguardando SESMT", "28/09/2026"}
        });
    return ReferenciaPage.scroll(page);
  }

  private static Node parametros() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-ADM-03", "Parâmetros do sistema", "Unidade, política de senha e modo de operação.");
    page.section(
        panel(
            "Unidade local",
            campo("Nome da unidade", "Itupeva"),
            campo("Modo de implantação", "Cliente-servidor"),
            botao("Salvar parâmetros", true)));
    return ReferenciaPage.scroll(page);
  }

  private static Node caepi() {
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-CAE-01",
            "Base oficial CAEPI",
            "Carga em background, publicação atômica e barra de status.");
    page.section(
            panel(
                "Última carga",
                new Label("28/09/2026 · 03:12 · 184.320 registros · operação normal"),
                botao("Nova tentativa", true)))
        .table(
            new String[] {"Fase", "Estado"},
            new String[][] {
              {"Download", "Concluída"},
              {"Validação", "Concluída"},
              {"Publicação", "Concluída"}
            });
    return ReferenciaPage.scroll(page);
  }

  private static VBox metric(
      String valor, String legenda, Destino destino, Consumer<Destino> navegar) {
    Label value = new Label(valor);
    value.getStyleClass().add(Enr6Styles.METRIC_VALUE);
    Label caption = new Label(legenda);
    caption.getStyleClass().add(Enr6Styles.PAGE_DESCRIPTION);
    caption.setWrapText(true);
    VBox card = new VBox(4, value, caption);
    card.getStyleClass().add(Enr6Styles.METRIC);
    card.setOnMouseClicked(event -> navegar.accept(destino));
    return card;
  }

  private static Button link(String texto, Destino destino, Consumer<Destino> navegar) {
    Button button = new Button(texto);
    button.getStyleClass().add(Styles.ACCENT);
    button.setOnAction(event -> navegar.accept(destino));
    return button;
  }

  private static Label passo(String texto, boolean atual) {
    Label label = new Label(texto);
    label.getStyleClass().add(atual ? Enr6Styles.EMPHASIS : Enr6Styles.PAGE_DESCRIPTION);
    return label;
  }

  private static HBox split(Node esquerda, Node direita) {
    HBox.setHgrow(esquerda, Priority.ALWAYS);
    HBox.setHgrow(direita, Priority.ALWAYS);
    HBox linha = new HBox(12, esquerda, direita);
    linha.setFillHeight(true);
    return linha;
  }

  private static VBox panel(String titulo, Node... filhos) {
    Label title = new Label(titulo);
    title.getStyleClass().add(Enr6Styles.EMPHASIS);
    VBox box = new VBox(8);
    box.getStyleClass().add(Enr6Styles.PANEL);
    box.getChildren().add(title);
    box.getChildren().addAll(filhos);
    return box;
  }

  private static VBox campo(String rotulo, String valor) {
    TextField field = new TextField(valor);
    field.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(field, Priority.ALWAYS);
    VBox box = new VBox(4, new Label(rotulo), field);
    box.setAlignment(Pos.CENTER_LEFT);
    box.setPadding(new Insets(0, 0, 4, 0));
    return box;
  }

  private static Button botao(String texto, boolean primario) {
    Button button = new Button(texto);
    if (primario) {
      button.getStyleClass().add(Styles.ACCENT);
    }
    return button;
  }
}
