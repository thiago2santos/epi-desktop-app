package br.com.easynr6.gestaoepi.ui.operacao;

import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import java.time.LocalDate;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class EntregaWizardView extends BorderPane {

  private final UsuarioAutenticado usuario;
  private final AuditTrail auditTrail;
  private final EntregaWizardState state;

  private final Label tituloPasso;
  private final Label feedbackLabel;
  private final VBox formContainer;
  private final Button anteriorButton;
  private final Button proximoButton;
  private final Button finalizarButton;

  private final TextField trabalhadorField = new TextField();
  private final TextField epiField = new TextField();
  private final TextField loteField = new TextField();
  private final DatePicker validadePicker = new DatePicker(LocalDate.now().plusDays(30));
  private final TextField saldoField = new TextField("10");
  private final TextField quantidadeField = new TextField("1");
  private final CheckBox validacaoResponsavelCheck =
      new CheckBox("Confirmo que validacoes obrigatorias foram revisadas.");
  private final CheckBox confirmacaoCheck =
      new CheckBox("Confirmo a intencao de registrar a entrega.");

  public EntregaWizardView(UsuarioAutenticado usuario, AuditTrail auditTrail) {
    this.usuario = usuario;
    this.auditTrail = auditTrail;
    this.state = new EntregaWizardState();

    setPadding(new Insets(16));

    tituloPasso = new Label();
    tituloPasso.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    feedbackLabel = new Label();
    feedbackLabel.setStyle("-fx-text-fill: #b91c1c;");

    formContainer = new VBox(8);
    formContainer.setPadding(new Insets(8, 0, 8, 0));

    anteriorButton = new Button("Anterior");
    anteriorButton.setOnAction(event -> navegarAnterior());
    proximoButton = new Button("Proximo");
    proximoButton.setOnAction(event -> navegarProximo());
    finalizarButton = new Button("Finalizar entrega");
    finalizarButton.setOnAction(event -> finalizarEntrega());

    HBox acoes = new HBox(8, anteriorButton, proximoButton, finalizarButton);
    VBox content = new VBox(10, tituloPasso, formContainer, feedbackLabel, acoes);
    setCenter(content);

    atualizarTela();
  }

  private void navegarAnterior() {
    feedbackLabel.setText("");
    state.goBack();
    atualizarTela();
  }

  private void navegarProximo() {
    feedbackLabel.setText("");
    syncFormToState();
    if (!state.canGoNext()) {
      feedbackLabel.setText("Preencha os campos obrigatorios do passo atual para avancar.");
      return;
    }
    state.goNext();
    atualizarTela();
  }

  private void finalizarEntrega() {
    feedbackLabel.setText("");
    syncFormToState();
    if (!state.canFinalize()) {
      feedbackLabel.setText("Confirme a operacao para concluir o wizard.");
      return;
    }
    state.finalizar();
    auditTrail.registrarEventoCritico(
        usuario.id(),
        "ENTREGA_REGISTRADA",
        "ENTREGA",
        "WIZARD",
        "Entrega finalizada pelo wizard operacional");
    atualizarTela();
  }

  private void atualizarTela() {
    tituloPasso.setText("Wizard de Entrega - " + state.currentStepLabel());
    formContainer.getChildren().setAll(buildStepContent());
    anteriorButton.setDisable(!state.canGoBack());
    proximoButton.setDisable(!state.canGoNext());
    finalizarButton.setDisable(!state.canFinalize());
  }

  private VBox buildStepContent() {
    VBox box = new VBox(8);
    int step = state.currentStepNumber();
    if (state.isFinalizado()) {
      Label sucesso = new Label("Entrega registrada com sucesso (simulacao M1).");
      sucesso.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold;");
      box.getChildren().add(sucesso);
      return box;
    }

    switch (step) {
      case 1 -> {
        trabalhadorField.setPromptText("Nome do trabalhador");
        box.getChildren().addAll(new Label("Trabalhador"), trabalhadorField);
      }
      case 2 -> {
        epiField.setPromptText("Codigo ou descricao do EPI");
        loteField.setPromptText("Lote");
        saldoField.setPromptText("Saldo atual");
        quantidadeField.setPromptText("Quantidade solicitada");
        box.getChildren()
            .addAll(
                new Label("EPI"),
                epiField,
                new Label("Lote"),
                loteField,
                new Label("Validade"),
                validadePicker,
                new Label("Saldo atual"),
                saldoField,
                new Label("Quantidade"),
                quantidadeField);
      }
      case 3 -> {
        Label info =
            new Label(
                "Validacoes obrigatorias: lote valido, saldo suficiente e dados obrigatorios.");
        info.setWrapText(true);
        box.getChildren().addAll(info, validacaoResponsavelCheck);
      }
      case 4 -> {
        Label resumo = new Label(buildResumoConfirmacao());
        resumo.setWrapText(true);
        box.getChildren().addAll(new Label("Resumo para confirmacao"), resumo, confirmacaoCheck);
      }
      case 5 -> {
        Label comprovante = new Label(buildResumoComprovante());
        comprovante.setWrapText(true);
        box.getChildren().add(comprovante);
      }
      default -> throw new IllegalStateException("Passo invalido.");
    }

    return box;
  }

  private String buildResumoConfirmacao() {
    return "Trabalhador: "
        + valorOuPendente(state.getTrabalhador())
        + "\nEPI: "
        + valorOuPendente(state.getEpi())
        + "\nLote: "
        + valorOuPendente(state.getLote())
        + "\nQuantidade: "
        + state.getQuantidadeSolicitada();
  }

  private String buildResumoComprovante() {
    return "Comprovante preliminar\nTrabalhador: "
        + valorOuPendente(state.getTrabalhador())
        + "\nEPI: "
        + valorOuPendente(state.getEpi())
        + "\nLote: "
        + valorOuPendente(state.getLote())
        + "\nData prevista: "
        + LocalDate.now();
  }

  private static String valorOuPendente(String valor) {
    return (valor == null || valor.isBlank()) ? "pendente" : valor;
  }

  private void syncFormToState() {
    state.setTrabalhador(trabalhadorField.getText());
    state.setEpi(epiField.getText());
    state.setLote(loteField.getText());
    state.setValidadeLote(validadePicker.getValue());
    state.setSaldoAtual(parseIntOrZero(saldoField.getText()));
    state.setQuantidadeSolicitada(parseIntOrZero(quantidadeField.getText()));
    state.setValidacaoResponsavel(validacaoResponsavelCheck.isSelected());
    state.setConfirmacaoOperacao(confirmacaoCheck.isSelected());
  }

  private static int parseIntOrZero(String valor) {
    try {
      return Integer.parseInt(valor);
    } catch (NumberFormatException ex) {
      return 0;
    }
  }
}
