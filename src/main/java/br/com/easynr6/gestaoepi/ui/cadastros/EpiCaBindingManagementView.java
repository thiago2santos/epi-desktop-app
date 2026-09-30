package br.com.easynr6.gestaoepi.ui.cadastros;

import br.com.easynr6.gestaoepi.modules.epi.application.EpiCatalogManagementService;
import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository.CaBindingSummary;
import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository.EpiSummary;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.UnaryOperator;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class EpiCaBindingManagementView extends VBox {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private final UsuarioAutenticado actor;
  private final EpiCatalogManagementService epiService;
  private final ObservableList<EpiSummary> epiOptions = FXCollections.observableArrayList();
  private final ObservableList<CaBindingSummary> caBindings = FXCollections.observableArrayList();
  private final ComboBox<EpiSummary> epiCombo = new ComboBox<>(epiOptions);
  private final TableView<CaBindingSummary> table = new TableView<>(caBindings);
  private final TextField caNumberField = new TextField();
  private final ComboBox<CaStatus> caStatusCombo = new ComboBox<>();
  private final DatePicker validFromPicker = new DatePicker();
  private final DatePicker validUntilPicker = new DatePicker();
  private final TextField officialCheckDateTimeField = new TextField();
  private final TextArea officialCheckNoteArea = new TextArea();
  private final CheckBox activeCheck = new CheckBox("Ativo");
  private final Label feedbackLabel = new Label();

  public EpiCaBindingManagementView(
      UsuarioAutenticado actor, EpiCatalogManagementService epiService) {
    this.actor = actor;
    this.epiService = epiService;
    setSpacing(10);
    setPadding(new Insets(10));
    getChildren()
        .addAll(buildHeader(), buildEpiSelector(), buildTable(), buildForm(), buildActions());
    setupBindings();
    setupFieldValidation();
    clearForm();
    refreshReferenceData();
  }

  private VBox buildHeader() {
    Label title = new Label("Vinculo de CA por EPI");
    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
    Label description =
        new Label("Gerencie CAs, vigencia e evidencia oficial de consulta por EPI.");
    description.setStyle("-fx-text-fill: #4b5563;");
    return new VBox(4, title, description);
  }

  private HBox buildEpiSelector() {
    epiCombo.setPromptText("Selecione o EPI");
    epiCombo.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(EpiSummary object) {
            if (object == null) {
              return "";
            }
            String code = object.epiCode() == null ? "" : object.epiCode() + " - ";
            return code + object.description();
          }

          @Override
          public EpiSummary fromString(String string) {
            return null;
          }
        });
    Button refreshButton = new Button("Atualizar EPIs");
    refreshButton.setOnAction(event -> refreshReferenceData());
    HBox box = new HBox(8, new Label("EPI"), epiCombo, refreshButton);
    HBox.setHgrow(epiCombo, Priority.ALWAYS);
    return box;
  }

  private TableView<CaBindingSummary> buildTable() {
    table.setPrefHeight(220);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    TableColumn<CaBindingSummary, String> caCol = new TableColumn<>("CA");
    caCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().caNumber()));
    TableColumn<CaBindingSummary, String> statusCol = new TableColumn<>("Situacao");
    statusCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(cell.getValue().caStatus().displayLabelPtBr()));
    TableColumn<CaBindingSummary, String> validityCol = new TableColumn<>("Vigencia");
    validityCol.setCellValueFactory(
        cell ->
            new ReadOnlyStringWrapper(
                (cell.getValue().validFrom() == null ? "-" : cell.getValue().validFrom())
                    + " ate "
                    + (cell.getValue().validUntil() == null ? "-" : cell.getValue().validUntil())));
    TableColumn<CaBindingSummary, String> activeCol = new TableColumn<>("Status");
    activeCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(cell.getValue().active() ? "ATIVO" : "INATIVO"));

    table.getColumns().setAll(caCol, statusCol, validityCol, activeCol);
    return table;
  }

  private GridPane buildForm() {
    caNumberField.setPromptText("Numero do CA");
    caStatusCombo.setPromptText("Situacao do CA");
    caStatusCombo.getItems().setAll(CaStatus.values());
    caStatusCombo.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(CaStatus object) {
            return object == null ? "" : object.displayLabelPtBr();
          }

          @Override
          public CaStatus fromString(String string) {
            return null;
          }
        });
    officialCheckDateTimeField.setPromptText("dd/MM/yyyy HH:mm");
    officialCheckNoteArea.setPromptText("Detalhe da evidencia/protocolo da consulta oficial");
    officialCheckNoteArea.setPrefRowCount(2);

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(8);
    form.addRow(0, new Label("Numero CA"), caNumberField);
    form.addRow(1, new Label("Situacao"), caStatusCombo);
    form.addRow(2, new Label("Valido de"), validFromPicker);
    form.addRow(3, new Label("Valido ate"), validUntilPicker);
    form.addRow(4, new Label("Consulta oficial"), officialCheckDateTimeField);
    form.addRow(5, new Label("Evidencia"), officialCheckNoteArea);
    form.add(activeCheck, 1, 6);
    return form;
  }

  private HBox buildActions() {
    Button createButton = new Button("Vincular");
    createButton.setOnAction(event -> createBinding());
    Button updateButton = new Button("Salvar edicao");
    updateButton.setOnAction(event -> updateBinding());
    Button deactivateButton = new Button("Inativar");
    deactivateButton.setOnAction(event -> setStatus(false));
    Button reactivateButton = new Button("Reativar");
    reactivateButton.setOnAction(event -> setStatus(true));
    Button clearButton = new Button("Limpar");
    clearButton.setOnAction(event -> clearForm());

    HBox actions =
        new HBox(
            8,
            createButton,
            updateButton,
            deactivateButton,
            reactivateButton,
            clearButton,
            feedbackLabel);
    actions.setAlignment(Pos.CENTER_LEFT);
    return actions;
  }

  private void setupBindings() {
    epiCombo
        .valueProperty()
        .addListener((obs, oldVal, selected) -> refreshBindingsForSelectedEpi());
    table
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldVal, selected) -> {
              if (selected == null) {
                return;
              }
              caNumberField.setText(selected.caNumber());
              caStatusCombo.getSelectionModel().select(selected.caStatus());
              validFromPicker.setValue(parseDate(selected.validFrom()));
              validUntilPicker.setValue(parseDate(selected.validUntil()));
              LocalDateTime checkAt = parseDateTime(selected.officialCheckAt());
              officialCheckDateTimeField.setText(
                  checkAt == null ? "" : DATE_TIME_FORMAT.format(checkAt));
              officialCheckNoteArea.setText(selected.officialCheckNote());
              activeCheck.setSelected(selected.active());
            });
  }

  private void setupFieldValidation() {
    configureDatePicker(validFromPicker, "validade inicial");
    configureDatePicker(validUntilPicker, "validade final");
    setupDateTimeMaskAndValidation();
  }

  private void configureDatePicker(DatePicker picker, String fieldName) {
    picker.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(LocalDate date) {
            return date == null ? "" : DATE_FORMAT.format(date);
          }

          @Override
          public LocalDate fromString(String text) {
            if (text == null || text.trim().isEmpty()) {
              return null;
            }
            return LocalDate.parse(text.trim(), DATE_FORMAT);
          }
        });
    UnaryOperator<TextFormatter.Change> dateMask =
        change -> {
          String newText = change.getControlNewText();
          if (newText.length() > 10) {
            return null;
          }
          if (!newText.matches("[0-9/]*")) {
            return null;
          }
          return change;
        };
    picker.getEditor().setTextFormatter(new TextFormatter<>(dateMask));
    picker
        .getEditor()
        .focusedProperty()
        .addListener(
            (obs, oldFocused, focused) -> {
              if (focused) {
                return;
              }
              String text =
                  picker.getEditor().getText() == null ? "" : picker.getEditor().getText().trim();
              if (text.isEmpty()) {
                picker.setValue(null);
                return;
              }
              try {
                LocalDate parsed = LocalDate.parse(text, DATE_FORMAT);
                picker.setValue(parsed);
                showFieldValid(picker.getEditor());
              } catch (DateTimeParseException ex) {
                showError("Formato invalido para " + fieldName + ". Use dd/MM/yyyy.");
                showFieldInvalid(picker.getEditor());
              }
            });
  }

  private void setupDateTimeMaskAndValidation() {
    UnaryOperator<TextFormatter.Change> dateTimeMask =
        change -> {
          String newText = change.getControlNewText();
          if (newText.length() > 16) {
            return null;
          }
          if (!newText.matches("[0-9/:\\s]*")) {
            return null;
          }
          return change;
        };
    officialCheckDateTimeField.setTextFormatter(new TextFormatter<>(dateTimeMask));
    officialCheckDateTimeField
        .focusedProperty()
        .addListener(
            (obs, oldFocused, focused) -> {
              if (focused) {
                String currentText =
                    officialCheckDateTimeField.getText() == null
                        ? ""
                        : officialCheckDateTimeField.getText().trim();
                if (currentText.isEmpty()) {
                  officialCheckDateTimeField.setText(
                      DATE_TIME_FORMAT.format(LocalDateTime.now().withSecond(0).withNano(0)));
                  showFieldValid(officialCheckDateTimeField);
                }
                return;
              }
              String text =
                  officialCheckDateTimeField.getText() == null
                      ? ""
                      : officialCheckDateTimeField.getText().trim();
              if (text.isEmpty()) {
                showError("Data/hora da consulta oficial obrigatoria. Use dd/MM/yyyy HH:mm.");
                showFieldInvalid(officialCheckDateTimeField);
                return;
              }
              try {
                LocalDateTime parsed = LocalDateTime.parse(text, DATE_TIME_FORMAT);
                officialCheckDateTimeField.setText(DATE_TIME_FORMAT.format(parsed));
                showFieldValid(officialCheckDateTimeField);
              } catch (DateTimeParseException ex) {
                showError("Formato invalido para consulta oficial. Use dd/MM/yyyy HH:mm.");
                showFieldInvalid(officialCheckDateTimeField);
              }
            });
  }

  public void refreshReferenceData() {
    epiOptions.setAll(epiService.listEpi(actor.id(), ""));
    if (!epiOptions.isEmpty() && epiCombo.getValue() == null) {
      epiCombo.getSelectionModel().selectFirst();
    }
    refreshBindingsForSelectedEpi();
  }

  private void refreshBindingsForSelectedEpi() {
    EpiSummary selectedEpi = epiCombo.getValue();
    if (selectedEpi == null) {
      caBindings.clear();
      return;
    }
    caBindings.setAll(epiService.listCaByEpi(actor.id(), selectedEpi.id()));
  }

  private void createBinding() {
    try {
      EpiSummary selectedEpi = requireSelectedEpi();
      LocalDateTime officialCheckAt = buildOfficialCheckAt();
      epiService.bindCaToEpi(
          actor.id(),
          selectedEpi.id(),
          caNumberField.getText(),
          caStatusCombo.getValue(),
          validFromPicker.getValue(),
          validUntilPicker.getValue(),
          officialCheckAt,
          officialCheckNoteArea.getText(),
          activeCheck.isSelected());
      showSuccess("CAD-350 CA vinculado com sucesso.");
      clearForm();
      refreshBindingsForSelectedEpi();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  private void updateBinding() {
    try {
      CaBindingSummary selected = requireSelectedBinding();
      LocalDateTime officialCheckAt = buildOfficialCheckAt();
      epiService.updateCaBinding(
          actor.id(),
          selected.id(),
          caNumberField.getText(),
          caStatusCombo.getValue(),
          validFromPicker.getValue(),
          validUntilPicker.getValue(),
          officialCheckAt,
          officialCheckNoteArea.getText(),
          activeCheck.isSelected());
      showSuccess("CAD-351 Vinculo de CA atualizado.");
      refreshBindingsForSelectedEpi();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  private void setStatus(boolean active) {
    try {
      CaBindingSummary selected = requireSelectedBinding();
      if (!active && !confirmDeactivate(selected.caNumber())) {
        return;
      }
      epiService.setCaBindingStatus(actor.id(), selected.id(), active);
      showSuccess(active ? "CAD-353 Vinculo de CA reativado." : "CAD-352 Vinculo de CA inativado.");
      refreshBindingsForSelectedEpi();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  private EpiSummary requireSelectedEpi() {
    EpiSummary selectedEpi = epiCombo.getValue();
    if (selectedEpi == null) {
      throw new IllegalArgumentException("CAD-039 Alvo de edicao/inativacao nao encontrado.");
    }
    return selectedEpi;
  }

  private CaBindingSummary requireSelectedBinding() {
    CaBindingSummary selected = table.getSelectionModel().getSelectedItem();
    if (selected == null) {
      throw new IllegalArgumentException("CAD-039 Alvo de edicao/inativacao nao encontrado.");
    }
    return selected;
  }

  private LocalDateTime buildOfficialCheckAt() {
    String text =
        officialCheckDateTimeField.getText() == null
            ? ""
            : officialCheckDateTimeField.getText().trim();
    if (text.isEmpty()) {
      return null;
    }
    try {
      return LocalDateTime.parse(text, DATE_TIME_FORMAT);
    } catch (DateTimeParseException ex) {
      throw new IllegalArgumentException("CAD-037 Evidencia de consulta oficial do CA ausente.");
    }
  }

  private LocalDate parseDate(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    return LocalDate.parse(text);
  }

  private LocalDateTime parseDateTime(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    try {
      return LocalDateTime.parse(text);
    } catch (DateTimeParseException ex) {
      return null;
    }
  }

  private void clearForm() {
    caNumberField.clear();
    caStatusCombo.getSelectionModel().clearSelection();
    validFromPicker.setValue(null);
    validUntilPicker.setValue(null);
    officialCheckDateTimeField.clear();
    officialCheckNoteArea.clear();
    activeCheck.setSelected(true);
    table.getSelectionModel().clearSelection();
    feedbackLabel.setText("");
    showFieldValid(officialCheckDateTimeField);
  }

  private void showSuccess(String message) {
    feedbackLabel.setText(message);
    feedbackLabel.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold;");
    PauseTransition pause = new PauseTransition(Duration.seconds(5));
    pause.setOnFinished(event -> feedbackLabel.setText(""));
    pause.play();
  }

  private void showError(String message) {
    feedbackLabel.setText(message == null ? "CAD-099 Falha ao processar operacao." : message);
    feedbackLabel.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
  }

  private boolean confirmDeactivate(String caNumber) {
    Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
    dialog.setTitle("Confirmar inativacao");
    dialog.setHeaderText("Inativar vinculo de CA");
    dialog.setContentText("Deseja inativar o vinculo do CA " + caNumber + "?");
    return dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
  }

  private void showFieldInvalid(TextField field) {
    field.setStyle("-fx-border-color: #b91c1c; -fx-border-width: 1; -fx-border-radius: 4;");
  }

  private void showFieldValid(TextField field) {
    field.setStyle("");
  }
}
