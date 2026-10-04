package br.com.easynr6.gestaoepi.ui.cadastro;

import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog.Linha;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/** Lista os CAs da última carga. O fabricante do EPI filtra a abertura; a busca abre o resto. */
public final class ConsultaCaDialog {

  private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private ConsultaCaDialog() {}

  public static Optional<Linha> escolher(
      Window owner, String fabricante, Function<String, List<Linha>> buscar) {
    Dialog<Linha> dialog = new Dialog<>();
    dialog.setTitle("CAs da base");
    dialog.initOwner(owner);
    ButtonType usar = new ButtonType("Usar este CA", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(usar, ButtonType.CANCEL);

    TextField termo = new TextField();
    termo.setPromptText("Número, equipamento ou razão social");
    TableView<Linha> tabela = new TableView<>();
    tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabela.setPrefHeight(320);
    tabela.getColumns().add(coluna("CA", Linha::caNumber));
    tabela.getColumns().add(coluna("Equipamento", Linha::equipment));
    tabela.getColumns().add(coluna("Razão social", Linha::manufacturer));
    tabela.getColumns().add(coluna("Situação", linha -> rotulo(linha.status())));
    tabela
        .getColumns()
        .add(
            coluna(
                "Validade",
                linha -> linha.validUntil() == null ? "" : linha.validUntil().format(DATA)));
    Label vazio = new Label("Nenhum CA desse fabricante. Busque pelo número.");
    tabela.setPlaceholder(vazio);
    Button buscarBtn = new Button("Buscar");
    buscarBtn.setOnAction(event -> tabela.getItems().setAll(buscar.apply(termo.getText())));
    tabela.getItems().setAll(buscar.apply(""));

    HBox busca = new HBox(8, termo, buscarBtn);
    HBox.setHgrow(termo, Priority.ALWAYS);
    VBox corpo =
        new VBox(
            8,
            new Label("Fabricante do EPI: " + (fabricante == null ? "" : fabricante)),
            busca,
            tabela);
    dialog.getDialogPane().setContent(corpo);
    dialog.setResultConverter(
        botao -> botao == usar ? tabela.getSelectionModel().getSelectedItem() : null);
    return dialog.showAndWait().filter(linha -> linha != null);
  }

  private static String rotulo(String status) {
    try {
      return CaStatus.valueOf(status).displayLabelPtBr();
    } catch (IllegalArgumentException ex) {
      return status;
    }
  }

  private static TableColumn<Linha, String> coluna(String titulo, Function<Linha, String> valor) {
    TableColumn<Linha, String> column = new TableColumn<>(titulo);
    column.setCellValueFactory(data -> new SimpleStringProperty(valor.apply(data.getValue())));
    return column;
  }
}
