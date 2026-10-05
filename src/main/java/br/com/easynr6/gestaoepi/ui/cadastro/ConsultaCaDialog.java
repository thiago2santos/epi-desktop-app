package br.com.easynr6.gestaoepi.ui.cadastro;

import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog.Linha;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/** Lista os CAs da última carga. A busca é por número, equipamento ou razão social. */
public final class ConsultaCaDialog {

  private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  @FunctionalInterface
  public interface Busca {
    List<Linha> aplicar(
        String termo, boolean ativos, boolean suspensos, boolean cancelados, boolean expirados);
  }

  private ConsultaCaDialog() {}

  public static Optional<Linha> escolher(Window owner, Busca buscar) {
    Dialog<Linha> dialog = new Dialog<>();
    dialog.setTitle("CAs da base");
    dialog.initOwner(owner);
    ButtonType usar = new ButtonType("Usar este CA", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(usar, ButtonType.CANCEL);
    Button usarBtn = (Button) dialog.getDialogPane().lookupButton(usar);
    usarBtn.setDefaultButton(false);
    usarBtn.setDisable(true);

    TextField termo = new TextField();
    termo.setPromptText("Número, equipamento ou razão social");
    CheckBox ativos = new CheckBox("Ativos");
    CheckBox suspensos = new CheckBox("Suspensos");
    CheckBox cancelados = new CheckBox("Cancelados");
    CheckBox expirados = new CheckBox("Expirados");
    ativos.setSelected(true);
    suspensos.setSelected(true);
    cancelados.setSelected(true);
    expirados.setSelected(true);
    TableView<Linha> tabela = new TableView<>();
    tabela.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    tabela.setPrefHeight(420);
    tabela.getColumns().add(coluna("CA", 110, Linha::caNumber));
    tabela.getColumns().add(coluna("Equipamento", 480, Linha::equipment));
    tabela.getColumns().add(coluna("Razão social", 360, Linha::manufacturer));
    tabela.getColumns().add(coluna("Situação", 140, linha -> rotulo(linha.status())));
    tabela
        .getColumns()
        .add(
            coluna(
                "Validade",
                120,
                linha -> linha.validUntil() == null ? "" : linha.validUntil().format(DATA)));
    Label vazio = new Label("Busque pelo número, equipamento ou razão social.");
    tabela.setPlaceholder(vazio);
    Button buscarBtn = new Button("Buscar");
    buscarBtn.setDefaultButton(true);
    boolean[] jaBuscou = {false};
    Runnable executar =
        () -> {
          tabela.getSelectionModel().clearSelection();
          tabela
              .getItems()
              .setAll(
                  buscar.aplicar(
                      termo.getText(),
                      ativos.isSelected(),
                      suspensos.isSelected(),
                      cancelados.isSelected(),
                      expirados.isSelected()));
          jaBuscou[0] = true;
        };
    buscarBtn.setOnAction(event -> executar.run());
    termo.setOnAction(
        event -> {
          if (tabela.getSelectionModel().getSelectedItem() == null) {
            buscarBtn.fire();
          } else {
            usarBtn.fire();
          }
        });
    termo
        .textProperty()
        .addListener((obs, anterior, atual) -> tabela.getSelectionModel().clearSelection());
    aoMudar(ativos, jaBuscou, executar);
    aoMudar(suspensos, jaBuscou, executar);
    aoMudar(cancelados, jaBuscou, executar);
    aoMudar(expirados, jaBuscou, executar);
    tabela
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, anterior, atual) -> {
              boolean escolhida = atual != null;
              usarBtn.setDisable(!escolhida);
              usarBtn.setDefaultButton(escolhida);
              buscarBtn.setDefaultButton(!escolhida);
            });

    HBox busca = new HBox(8, termo, buscarBtn);
    HBox.setHgrow(termo, Priority.ALWAYS);
    busca.setAlignment(Pos.CENTER_LEFT);
    HBox filtros = new HBox(16, ativos, suspensos, cancelados, expirados);
    filtros.setAlignment(Pos.CENTER_LEFT);
    VBox corpo = new VBox(8, busca, filtros, tabela);
    VBox.setVgrow(tabela, Priority.ALWAYS);
    double largura = larguraDoDialogo(owner);
    dialog.setResizable(true);
    dialog.getDialogPane().setPrefSize(largura, 640);
    dialog.getDialogPane().setMinSize(880, 500);
    dialog.getDialogPane().setContent(corpo);
    dialog.setOnShown(event -> Platform.runLater(termo::requestFocus));
    dialog.setResultConverter(
        botao -> botao == usar ? tabela.getSelectionModel().getSelectedItem() : null);
    return dialog.showAndWait().filter(linha -> linha != null);
  }

  private static void aoMudar(CheckBox filtro, boolean[] jaBuscou, Runnable executar) {
    filtro
        .selectedProperty()
        .addListener((obs, anterior, atual) -> repetirBusca(jaBuscou, executar));
  }

  private static void repetirBusca(boolean[] jaBuscou, Runnable executar) {
    if (jaBuscou[0]) {
      executar.run();
    }
  }

  private static String rotulo(String status) {
    try {
      return CaStatus.valueOf(status).displayLabelPtBr();
    } catch (IllegalArgumentException ex) {
      return status;
    }
  }

  private static double larguraDoDialogo(Window owner) {
    double dono = owner == null ? 0 : owner.getWidth();
    if (dono < 200) {
      return 1180;
    }
    return Math.min(Math.max(dono - 80, 960), 1280);
  }

  private static TableColumn<Linha, String> coluna(
      String titulo, double largura, Function<Linha, String> valor) {
    TableColumn<Linha, String> column = new TableColumn<>(titulo);
    column.setPrefWidth(largura);
    column.setMinWidth(80);
    column.setCellValueFactory(data -> new SimpleStringProperty(valor.apply(data.getValue())));
    return column;
  }
}
