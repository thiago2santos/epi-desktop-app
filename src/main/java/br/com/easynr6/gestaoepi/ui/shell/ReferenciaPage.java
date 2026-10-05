package br.com.easynr6.gestaoepi.ui.shell;

import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Página padrão das telas do shell. */
public final class ReferenciaPage extends VBox {

  private final VBox body = new VBox(12);

  private ReferenciaPage(String uc, String titulo, String subtitulo) {
    setSpacing(12);
    setFillWidth(true);
    getStyleClass().add(Enr6Styles.PAGE);
    Label tag = new Label(uc);
    tag.getStyleClass().add(Enr6Styles.UC_TAG);
    Label title = new Label(titulo);
    title.getStyleClass().add(Enr6Styles.PAGE_TITLE);
    Label sub = new Label(subtitulo);
    sub.getStyleClass().add(Enr6Styles.PAGE_DESCRIPTION);
    sub.setWrapText(true);
    getChildren().addAll(tag, title, sub, body);
  }

  public static ReferenciaPage of(String uc, String titulo, String subtitulo) {
    return new ReferenciaPage(uc, titulo, subtitulo);
  }

  public ReferenciaPage section(Node node) {
    body.getChildren().add(node);
    return this;
  }

  /** A seção ocupa o espaço restante. A página não entra num scroll externo. */
  public ReferenciaPage preencher(Node node) {
    setMaxHeight(Double.MAX_VALUE);
    setVgrow(body, Priority.ALWAYS);
    setVgrow(node, Priority.ALWAYS);
    body.getChildren().add(node);
    return this;
  }

  public ReferenciaPage table(String[] colunas, String[][] linhas) {
    TableView<String[]> table = new TableView<>();
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    table.setPrefHeight(280);
    for (int i = 0; i < colunas.length; i++) {
      final int index = i;
      TableColumn<String[], String> column = new TableColumn<>(colunas[i]);
      column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[index]));
      table.getColumns().add(column);
    }
    table.getItems().addAll(List.of(linhas));
    setVgrow(table, Priority.ALWAYS);
    body.getChildren().add(table);
    return this;
  }

  public ReferenciaPage legal(String texto) {
    Label bar = new Label(texto);
    bar.getStyleClass().add(Enr6Styles.LEGAL_BAR);
    bar.setWrapText(true);
    bar.setMaxWidth(Double.MAX_VALUE);
    body.getChildren().add(bar);
    return this;
  }

  public static ScrollPane scroll(Node content) {
    ScrollPane scroll = new ScrollPane(content);
    scroll.getStyleClass().add(Enr6Styles.PAGE);
    scroll.setFitToWidth(true);
    scroll.setFitToHeight(true);
    if (content instanceof VBox box) {
      box.setPadding(new Insets(4, 8, 16, 4));
    }
    return scroll;
  }
}
