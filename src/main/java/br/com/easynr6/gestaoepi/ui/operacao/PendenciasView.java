package br.com.easynr6.gestaoepi.ui.operacao;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.UnitOption;
import br.com.easynr6.gestaoepi.modules.issuance.application.PendenciaManagementService;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.PendenciaRepository.Pendencia;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** UC-POS-03. Lista a peça individual de quem saiu. A consulta não grava. */
public final class PendenciasView {

  private final UsuarioAutenticado usuario;
  private final PendenciaManagementService pendencias;
  private final EmployeeManagementService estrutura;
  private final Consumer<Long> aoDevolver;
  private final VBox corpo = new VBox(12);
  private final Label feedback = new Label();
  private final TableView<Pendencia> tabela = new TableView<>();
  private final Node root;
  private UnitOption unidade;
  private LocalDate inicio;
  private LocalDate fim;
  private String trabalhador = "";

  public PendenciasView(
      UsuarioAutenticado usuario,
      PendenciaManagementService pendencias,
      EmployeeManagementService estrutura,
      Consumer<Long> aoDevolver) {
    this.usuario = usuario;
    this.pendencias = pendencias;
    this.estrutura = estrutura;
    this.aoDevolver = aoDevolver;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-POS-03",
            "Pendências",
            "Peça individual que ainda está com o trabalhador desligado.");
    page.section(corpo);
    this.root = ReferenciaPage.scroll(page);
    configurarTabela();
    mostrar();
    consultar();
  }

  public Node root() {
    return root;
  }

  private void configurarTabela() {
    tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabela.setPlaceholder(new Label(MensagensPendencia.VAZIO));
    TableColumn<Pendencia, Void> acao = new TableColumn<>("");
    acao.setCellFactory(coluna -> new CelulaDevolucao());
    tabela
        .getColumns()
        .setAll(
            coluna("Trabalhador", Pendencia::nome),
            coluna("Matrícula", Pendencia::matricula),
            coluna("EPI", Pendencia::epi),
            coluna("Fornecimento", linha -> linha.fornecimento().toString()),
            coluna("Qtd", linha -> String.valueOf(linha.quantidade())),
            acao);
  }

  private void mostrar() {
    feedback.setWrapText(true);
    corpo.getChildren().setAll(filtros(), tabela, feedback);
  }

  private Node filtros() {
    ComboBox<UnitOption> unidades = new ComboBox<>();
    unidades.getItems().add(null);
    try {
      unidades.getItems().addAll(estrutura.listActiveUnits());
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("LISTAR_UNIDADES_PENDENCIA", usuario.id(), "-", ex);
    }
    unidades.setConverter(nomes(unidade -> unidade == null ? "Todas as unidades" : unidade.name()));
    unidades.setValue(unidade);
    unidades.setOnAction(event -> unidade = unidades.getValue());
    DatePicker de = new DatePicker(inicio);
    DatePicker ate = new DatePicker(fim);
    de.valueProperty().addListener((obs, anterior, novo) -> inicio = novo);
    ate.valueProperty().addListener((obs, anterior, novo) -> fim = novo);
    TextField nome = new TextField(trabalhador);
    nome.setPromptText("Trabalhador");
    nome.textProperty().addListener((obs, anterior, novo) -> trabalhador = novo);
    Button consultar = new Button("Consultar");
    consultar.getStyleClass().add(Styles.ACCENT);
    consultar.setOnAction(event -> consultar());
    return new HBox(8, unidades, new Label("De"), de, new Label("até"), ate, nome, consultar);
  }

  private void consultar() {
    try {
      Long unitId = unidade == null ? null : unidade.id();
      List<Pendencia> linhas =
          pendencias.listar(usuario.id(), unitId, inicio, fim, null).stream()
              .filter(this::doTrabalhador)
              .toList();
      tabela.getItems().setAll(linhas);
      if (linhas.isEmpty()) {
        avisar(MensagensPendencia.VAZIO);
        return;
      }
      limparFeedback();
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("CONSULTAR_PENDENCIAS", usuario.id(), "-", ex);
      avisar(MensagensPendencia.erro(ex));
    }
  }

  private boolean doTrabalhador(Pendencia linha) {
    String termo = trabalhador == null ? "" : trabalhador.trim().toLowerCase();
    if (termo.isEmpty()) {
      return true;
    }
    return linha.nome().toLowerCase().contains(termo)
        || linha.matricula().toLowerCase().contains(termo);
  }

  private static TableColumn<Pendencia, String> coluna(
      String titulo, Function<Pendencia, String> valor) {
    TableColumn<Pendencia, String> coluna = new TableColumn<>(titulo);
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

  private static <T> StringConverter<T> nomes(Function<T, String> nome) {
    return new StringConverter<>() {
      @Override
      public String toString(T valor) {
        return nome.apply(valor);
      }

      @Override
      public T fromString(String value) {
        return null;
      }
    };
  }

  private final class CelulaDevolucao extends TableCell<Pendencia, Void> {
    private final Button botao = new Button("Registrar devolução");

    private CelulaDevolucao() {
      botao.getStyleClass().add(Styles.ACCENT);
      botao.setOnAction(
          event -> {
            Pendencia linha = getTableView().getItems().get(getIndex());
            if (aoDevolver != null && linha != null) {
              aoDevolver.accept(linha.itemId());
            }
          });
    }

    @Override
    protected void updateItem(Void item, boolean vazio) {
      super.updateItem(item, vazio);
      setGraphic(vazio ? null : botao);
    }
  }
}
