package br.com.easynr6.gestaoepi.ui.operacao;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.issuance.application.HistoricoManagementService;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.HistoricoRepository.Linha;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.HistoricoRepository.TrabalhadorBusca;
import br.com.easynr6.gestaoepi.modules.issuance.domain.HistoricoPolicy;
import br.com.easynr6.gestaoepi.shared.audit.LogTroubleshooting;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** UC-ENT-03. Lê fornecimento, devolução e estorno. Pedido não entra. */
public final class HistoricoView {

  private final UsuarioAutenticado usuario;
  private final HistoricoManagementService historico;
  private final Consumer<Long> aoDevolver;
  private final Consumer<Long> aoEstornar;
  private final VBox corpo = new VBox(12);
  private final Label feedback = new Label();
  private final TableView<Linha> tabela = new TableView<>();
  private final HBox acoes = new HBox(8);
  private final Node root;
  private TrabalhadorBusca trabalhador;
  private LocalDate inicio = LocalDate.now().minusMonths(1);
  private LocalDate fim = LocalDate.now();

  public HistoricoView(
      UsuarioAutenticado usuario,
      HistoricoManagementService historico,
      Consumer<Long> aoDevolver,
      Consumer<Long> aoEstornar) {
    this.usuario = usuario;
    this.historico = historico;
    this.aoDevolver = aoDevolver;
    this.aoEstornar = aoEstornar;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-ENT-03",
            "Histórico por trabalhador",
            "Fornecimento, devolução e estorno do período. Pedido não aparece.");
    page.section(corpo);
    this.root = ReferenciaPage.scroll(page);
    configurarTabela();
    mostrar();
  }

  public Node root() {
    return root;
  }

  private void configurarTabela() {
    tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabela.setPlaceholder(new Label(MensagensHistorico.VAZIO));
    tabela
        .getColumns()
        .setAll(
            coluna("Data", linha -> linha.data().toString()),
            coluna("EPI", Linha::epi),
            coluna("CA", Linha::ca),
            coluna("Lote", Linha::lote),
            coluna("Qtd", linha -> String.valueOf(linha.quantidade())),
            coluna("Motivo", Linha::motivo),
            coluna("Situação", Linha::situacao));
    tabela
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((obs, anterior, linha) -> atualizarAcoes(linha));
  }

  private void mostrar() {
    feedback.setWrapText(true);
    corpo.getChildren().setAll(busca(), periodo(), cartao(), tabela, acoes, feedback);
  }

  private Node busca() {
    TextField campo = new TextField();
    campo.setPromptText("Matrícula ou nome");
    VBox resultados = new VBox(6);
    Button botao = new Button("Buscar");
    botao.getStyleClass().add(Styles.ACCENT);
    botao.setOnAction(event -> buscar(campo.getText(), resultados));
    return new VBox(8, new Label("Buscar trabalhador"), campo, botao, resultados);
  }

  private void buscar(String texto, VBox resultados) {
    try {
      List<TrabalhadorBusca> encontrados = historico.buscar(usuario.id(), texto);
      resultados.getChildren().clear();
      if (encontrados.isEmpty()) {
        resultados.getChildren().add(new Label(MensagensHistorico.BUSCA));
        return;
      }
      for (TrabalhadorBusca candidato : encontrados) {
        Button escolher = new Button(candidato.nome() + " · " + candidato.matricula());
        escolher.setOnAction(
            event -> {
              trabalhador = candidato;
              limparFeedback();
              mostrar();
              consultar();
            });
        resultados.getChildren().add(escolher);
      }
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("BUSCAR_HISTORICO", usuario.id(), "-", ex);
      avisar(MensagensHistorico.erro(ex));
    }
  }

  private Node periodo() {
    DatePicker de = new DatePicker(inicio);
    DatePicker ate = new DatePicker(fim);
    de.valueProperty().addListener((obs, anterior, novo) -> inicio = novo);
    ate.valueProperty().addListener((obs, anterior, novo) -> fim = novo);
    Button consultar = new Button("Consultar");
    consultar.getStyleClass().add(Styles.ACCENT);
    consultar.setOnAction(event -> consultar());
    return new HBox(8, new Label("De"), de, new Label("até"), ate, consultar);
  }

  private Node cartao() {
    if (trabalhador == null) {
      return new Label("");
    }
    String ativo = trabalhador.ativo() ? "Ativo" : "Inativo";
    return new Label(
        trabalhador.nome()
            + " · "
            + trabalhador.matricula()
            + " · "
            + trabalhador.setor()
            + " · "
            + trabalhador.funcao()
            + " · "
            + ativo);
  }

  private void atualizarAcoes(Linha linha) {
    acoes.getChildren().clear();
    if (linha == null || !"FORNECIMENTO".equals(linha.fato())) {
      return;
    }
    if (!HistoricoPolicy.FORNECIDO.equals(linha.situacao())) {
      return;
    }
    if (HistoricoAcoes.devolver(usuario) && aoDevolver != null) {
      Button devolver = new Button("Registrar devolução");
      devolver.setOnAction(event -> aoDevolver.accept(linha.itemId()));
      acoes.getChildren().add(devolver);
    }
    if (HistoricoAcoes.estornar(usuario) && aoEstornar != null) {
      Button estornar = new Button("Estornar");
      estornar.setOnAction(event -> aoEstornar.accept(linha.itemId()));
      acoes.getChildren().add(estornar);
    }
  }

  private void consultar() {
    if (trabalhador == null) {
      return;
    }
    if (!HistoricoPeriodo.valido(inicio, fim)) {
      tabela.getItems().clear();
      avisar(MensagensHistorico.PERIODO);
      return;
    }
    try {
      List<Linha> linhas = historico.listar(usuario.id(), trabalhador.id(), inicio, fim);
      tabela.getItems().setAll(linhas);
      if (linhas.isEmpty()) {
        avisar(MensagensHistorico.VAZIO);
        return;
      }
      limparFeedback();
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar(
          "CONSULTAR_HISTORICO", usuario.id(), String.valueOf(trabalhador.id()), ex);
      avisar(MensagensHistorico.erro(ex));
    }
  }

  private static TableColumn<Linha, String> coluna(String titulo, Function<Linha, String> valor) {
    TableColumn<Linha, String> coluna = new TableColumn<>(titulo);
    coluna.setCellValueFactory(dados -> new SimpleStringProperty(valor.apply(dados.getValue())));
    return coluna;
  }

  private void avisar(String texto) {
    feedback.setText(texto);
    feedback.getStyleClass().setAll(Enr6Styles.FEEDBACK_DANGER);
  }

  private void limparFeedback() {
    feedback.setText("");
    feedback.getStyleClass().clear();
  }
}
