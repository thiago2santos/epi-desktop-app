package br.com.easynr6.gestaoepi.ui.auditoria;

import br.com.easynr6.gestaoepi.shared.audit.AuditQueryService;
import br.com.easynr6.gestaoepi.shared.audit.AuditQueryService.AuditEventSummary;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.MensagemTemporaria;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** UC-AUD-01. Consulta a trilha com o filtro livre que o serviço já executa. */
public final class AuditTrailView {

  private final AuditQueryService consulta;
  private final TextField filtro = new TextField();
  private final TableView<AuditEventSummary> tabela = new TableView<>();
  private final Label feedback = new Label();
  private final Label vazio = new Label("Nenhum evento na trilha.");
  private final Node root;
  private final MensagemTemporaria mensagens = new MensagemTemporaria();

  public AuditTrailView(AuditQueryService consulta) {
    this.consulta = consulta;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-AUD-01",
            "Trilha de auditoria",
            "Eventos sensíveis, do mais recente ao mais antigo. A consulta é só leitura.");
    configurarTabela();
    filtro.setPromptText("Ação, usuário, entidade, código ou detalhe");
    filtro.textProperty().addListener((obs, anterior, atual) -> carregar());
    feedback.setWrapText(true);
    VBox lista = new VBox(8, filtro, feedback, tabela);
    lista.getStyleClass().add(Enr6Styles.PANEL);
    VBox.setVgrow(tabela, Priority.ALWAYS);
    tabela.setPrefHeight(480);
    page.section(lista);
    this.root = ReferenciaPage.scroll(page);
    carregar();
  }

  public Node root() {
    return root;
  }

  static String termoAuditoria(String texto) {
    return texto == null ? "" : texto.trim();
  }

  private void configurarTabela() {
    tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabela.setPlaceholder(vazio);
    tabela
        .getColumns()
        .setAll(
            coluna("Instante", AuditEventSummary::timestamp),
            coluna("Usuário", item -> item.userLogin() == null ? "" : item.userLogin()),
            coluna("Ação", AuditEventSummary::action),
            coluna("Entidade", AuditEventSummary::entity),
            coluna("ID", AuditEventSummary::entityId),
            coluna("Resultado", item -> item.resultado() == null ? "" : item.resultado()),
            coluna("Código", item -> item.codigo() == null ? "" : item.codigo()),
            coluna("Correlação", item -> item.correlacao() == null ? "" : item.correlacao()),
            coluna("Detalhes", item -> item.details() == null ? "" : item.details()));
  }

  private void carregar() {
    try {
      String termo = termoAuditoria(filtro.getText());
      tabela.getItems().setAll(consulta.listEvents(termo, 200));
      vazio.setText(
          termo.isEmpty() ? "Nenhum evento na trilha." : "Nenhum evento com esse filtro.");
      feedback.setText("");
    } catch (RuntimeException ex) {
      tabela.getItems().clear();
      feedback.setText("Não foi possível consultar a auditoria.");
      Enr6Styles.markDanger(feedback);
      mensagens.agendar(feedback, feedback.getText());
    }
  }

  private static TableColumn<AuditEventSummary, String> coluna(
      String titulo, Function<AuditEventSummary, String> valor) {
    TableColumn<AuditEventSummary, String> column = new TableColumn<>(titulo);
    column.setCellValueFactory(
        data -> new SimpleStringProperty(texto(valor.apply(data.getValue()))));
    return column;
  }

  private static String texto(String valor) {
    return valor == null ? "" : valor;
  }
}
