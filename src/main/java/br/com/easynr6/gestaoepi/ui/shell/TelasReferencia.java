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
      case TRABALHADORES ->
          throw new IllegalStateException("UC-CAD-03 abre pelo shell, com o cadastro real.");
      case SETORES ->
          throw new IllegalStateException("UC-CAD-02 abre pelo shell, com o cadastro real.");
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
                    campo("Matrícula ou nome", "4418"),
                    botao("Buscar", false)),
                panel(
                    "Cobertura (matriz × vigente)",
                    new Label("Busque um trabalhador para ver EPI exigido e situação."))))
        .legal(
            "Registro legal: ao confirmar, grava fornecimento imutável. Correções apenas por estorno.");
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
                    campo("Descrição", "Luva de vaqueta"),
                    campo("Grupo Anexo I", "Mãos"),
                    campo("Fabricante", ""),
                    botao("Salvar", true),
                    link("Gerenciar CA", Destino.CA, navegar))))
        .table(
            new String[] {"Código", "Descrição", "Anexo", "CA ativo", "Status"},
            new String[][] {
              {"LUV-VAQ", "Luva de vaqueta", "Mãos", "28941", "Ativo"},
              {"BOT-BIQ", "Bota de segurança com biqueira", "Pés", "35602", "Ativo"},
              {"AUR-CON", "Protetor auricular tipo concha", "Audição", "41287", "Ativo"},
              {"CAP-JUG", "Capacete com jugular", "Cabeça", "19844", "Ativo"}
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
                campo("EPI", "LUV-VAQ — Luva de vaqueta"),
                campo("Número do CA", "28941"),
                campo("Situação", "Válido"),
                campo("Evidência", "Carga CAEPI 28/09/2026 · CA válido"),
                botao("Vincular", true)))
        .table(
            new String[] {"CA", "Situação", "Vigência", "Evidência"},
            new String[][] {
              {"28941", "Válido", "12/03/2025 — 12/03/2030", "Carga CAEPI 28/09/2026"}
            });
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
              {
                "03/10/2026 08:14",
                "juliana.andrade",
                "LOGIN",
                "USUARIO",
                "juliana.andrade",
                "Login realizado"
              },
              {
                "03/10/2026 08:22",
                "paulo.lima",
                "ACESSO_MODULO",
                "MODULO",
                "LOTES",
                "Lotes e saldos"
              }
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
                    campo("Nome exibido", "Juliana Cristina de Andrade de Oliveira"),
                    campo("Papel", "SESMT"),
                    botao("Salvar", true),
                    botao("Reset senha", false))))
        .table(
            new String[] {"Login", "Nome", "Papel", "Status"},
            new String[][] {
              {"juliana.andrade", "Juliana Cristina de Andrade de Oliveira", "SESMT", "Ativo"},
              {"paulo.lima", "Paulo Sergio Lima dos Santos", "Almoxarife", "Ativo"},
              {"eduardo.regis", "Eduardo Regis Ferreira Teixeira", "Admin", "Ativo"}
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
                campo("Trabalhador ou entrega", "4418 ou ITU-2026-04418"),
                botao("Buscar itens entregues", false)))
        .table(
            new String[] {"Data", "EPI", "Qtd", "Status"},
            new String[][] {
              {"12/09/2026", "Luva de vaqueta", "1", "Entregue"},
              {"02/08/2026", "Bota de segurança com biqueira", "1", "Entregue"}
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
                campo("Entrega", "ITU-2026-04418"),
                campo("Motivo", "Luva lançada na matrícula 3902 em vez da 4418"),
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
    page.section(campo("Matrícula", "4418"))
        .table(
            new String[] {"Data", "EPI", "CA", "Lote", "Evento"},
            new String[][] {
              {"12/09/2026", "Luva de vaqueta", "28941", "VG-26-0418", "Fornecimento"},
              {
                "02/08/2026",
                "Bota de segurança com biqueira",
                "35602",
                "BT-25-1102",
                "Fornecimento"
              }
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
            campo("Trabalhador do escopo", "4418 · Adalto Candido Alves da Silva"),
            campo("EPI", "Protetor auricular tipo concha"),
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
          {
            "SOL-1044",
            "Adalto Candido Alves da Silva",
            "Protetor auricular tipo concha",
            "1",
            "Aberta"
          },
          {
            "SOL-1038",
            "Eduardo Gomes dos Santos",
            "Luva isolante classe 00",
            "1",
            "Fora da matriz · SESMT"
          }
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
    epi.getItems()
        .addAll(
            "LUV-VAQ — Luva de vaqueta",
            "BOT-BIQ — Bota de segurança com biqueira",
            "AUR-CON — Protetor auricular tipo concha",
            "CAP-JUG — Capacete com jugular");
    epi.getSelectionModel().selectFirst();
    form.add(epi, 1, 0);
    form.add(new Label("Nº CA"), 0, 1);
    form.add(new TextField("28941"), 1, 1);
    form.add(new Label("Validade da peça"), 0, 2);
    form.add(new DatePicker(), 1, 2);
    form.add(botao("Registrar lote", true), 1, 3);
    page.section(panel("Registrar recebimento", form))
        .table(
            new String[] {"Lote", "EPI", "Saldo", "Validade da peça", "Alerta"},
            new String[][] {
              {"VG-26-0418", "Luva de vaqueta", "40", "15/03/2027", "OK"},
              {"BT-25-1102", "Bota de segurança com biqueira", "18", "20/11/2027", "OK"},
              {"CP-24-0088", "Capacete com jugular", "6", "20/10/2026", "30 dias"}
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
          {"Operador de empilhadeira", "Protetor auricular tipo concha", "Sim", "01/03/2026 —"},
          {"Operador de empilhadeira", "Bota de segurança com biqueira", "Sim", "01/03/2026 —"},
          {"Auxiliar de guarda", "Luva de vaqueta", "Sim", "01/03/2026 —"},
          {"Eletricista", "Luva isolante classe 00", "Sim", "01/03/2026 —"}
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
          {"Operador de empilhadeira", "Protetor auricular tipo concha", "180", "dias"},
          {"Auxiliar de guarda", "Luva de vaqueta", "60", "dias"},
          {"Eletricista", "Luva isolante classe 00", "365", "dias"}
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
          {
            "Adalto Candido Alves da Silva",
            "Operador de empilhadeira",
            "Protetor auricular tipo concha",
            "Coberto"
          },
          {"Camila Gomes Pinto", "Auxiliar de guarda", "Luva de vaqueta", "Coberto"},
          {"Eduardo Gomes dos Santos", "Eletricista", "Luva isolante classe 00", "Descoberto"}
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
          {"Troca", "Eduardo Gomes dos Santos", "Luva isolante classe 00 vencida", "01/09/2026"},
          {"Exceção", "SOL-1038", "Fora da matriz, aguardando SESMT", "28/09/2026"}
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
            campo("Nome da unidade", "Itupeva · 22.755.266/0002-68"),
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
