package br.com.easynr6.gestaoepi.ui.cadastros;

import br.com.easynr6.gestaoepi.modules.epi.application.EpiCatalogManagementService;
import br.com.easynr6.gestaoepi.modules.epi.application.port.EpiRepository.EpiSummary;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class EpiManagementView extends VBox {

  private final UsuarioAutenticado actor;
  private final EpiCatalogManagementService epiService;
  private final ObservableList<EpiSummary> epiItems = FXCollections.observableArrayList();
  private final TextField searchField = new TextField();
  private final TableView<EpiSummary> table = new TableView<>(epiItems);
  private final TextField epiCodeField = new TextField();
  private final TextField descriptionField = new TextField();
  private final ComboBox<AnnexGroup> annexGroupCombo = new ComboBox<>();
  private final TextField manufacturerField = new TextField();
  private final CheckBox activeCheck = new CheckBox("Ativo");
  private final Label feedbackLabel = new Label();

  public EpiManagementView(UsuarioAutenticado actor, EpiCatalogManagementService epiService) {
    this.actor = actor;
    this.epiService = epiService;
    setSpacing(10);
    setPadding(new Insets(10));
    getChildren()
        .addAll(buildHeader(), buildSearchBar(), buildTable(), buildForm(), buildActions());
    bindTableSelection();
    clearForm();
    refreshData();
  }

  private VBox buildHeader() {
    Label title = new Label("Cadastro de EPI");
    title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
    Label description = new Label("Gerencie catalogo mestre de EPI com classificacao do Anexo I.");
    description.setStyle("-fx-text-fill: #4b5563;");
    return new VBox(4, title, description);
  }

  private HBox buildSearchBar() {
    searchField.setPromptText("Buscar por codigo, descricao ou fabricante");
    Button searchButton = new Button("Buscar");
    searchButton.setOnAction(event -> refreshData());
    Button refreshButton = new Button("Atualizar");
    refreshButton.setOnAction(event -> refreshData());
    HBox box = new HBox(8, searchField, searchButton, refreshButton);
    HBox.setHgrow(searchField, Priority.ALWAYS);
    return box;
  }

  private TableView<EpiSummary> buildTable() {
    table.setPrefHeight(220);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    TableColumn<EpiSummary, String> codeCol = new TableColumn<>("Codigo");
    codeCol.setCellValueFactory(
        cell ->
            new ReadOnlyStringWrapper(
                cell.getValue().epiCode() == null ? "-" : cell.getValue().epiCode()));
    TableColumn<EpiSummary, String> descCol = new TableColumn<>("Descricao");
    descCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().description()));
    TableColumn<EpiSummary, String> annexCol = new TableColumn<>("Anexo");
    annexCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(cell.getValue().annexGroup().name()));
    TableColumn<EpiSummary, String> manufacturerCol = new TableColumn<>("Fabricante");
    manufacturerCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(cell.getValue().manufacturerName()));
    TableColumn<EpiSummary, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(cell.getValue().active() ? "ATIVO" : "INATIVO"));

    table.getColumns().setAll(codeCol, descCol, annexCol, manufacturerCol, statusCol);
    return table;
  }

  private GridPane buildForm() {
    epiCodeField.setPromptText("Codigo interno (opcional)");
    descriptionField.setPromptText("Descricao do EPI");
    annexGroupCombo.setPromptText("Selecione o grupo");
    annexGroupCombo.getItems().setAll(AnnexGroup.values());
    manufacturerField.setPromptText("Fabricante");

    GridPane form = new GridPane();
    form.setHgap(10);
    form.setVgap(8);
    form.addRow(0, new Label("Codigo"), epiCodeField);
    form.addRow(1, new Label("Descricao"), descriptionField);
    form.addRow(2, new Label("Grupo Anexo I"), annexGroupCombo);
    form.addRow(3, new Label("Fabricante"), manufacturerField);
    form.add(activeCheck, 1, 4);
    return form;
  }

  private HBox buildActions() {
    Button createButton = new Button("Cadastrar");
    createButton.setOnAction(event -> createEpi());
    Button updateButton = new Button("Salvar edicao");
    updateButton.setOnAction(event -> updateEpi());
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

  private void bindTableSelection() {
    table
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldVal, selected) -> {
              if (selected == null) {
                return;
              }
              epiCodeField.setText(selected.epiCode());
              descriptionField.setText(selected.description());
              annexGroupCombo.getSelectionModel().select(selected.annexGroup());
              manufacturerField.setText(selected.manufacturerName());
              activeCheck.setSelected(selected.active());
            });
  }

  private void createEpi() {
    try {
      epiService.createEpi(
          actor.id(),
          epiCodeField.getText(),
          descriptionField.getText(),
          annexGroupCombo.getValue(),
          manufacturerField.getText(),
          activeCheck.isSelected());
      showSuccess("CAD-340 EPI cadastrado com sucesso.");
      clearForm();
      refreshData();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  private void updateEpi() {
    try {
      EpiSummary selected = table.getSelectionModel().getSelectedItem();
      if (selected == null) {
        throw new IllegalArgumentException("CAD-039 Alvo de edicao/inativacao nao encontrado.");
      }
      epiService.updateEpi(
          actor.id(),
          selected.id(),
          epiCodeField.getText(),
          descriptionField.getText(),
          annexGroupCombo.getValue(),
          manufacturerField.getText(),
          activeCheck.isSelected());
      showSuccess("CAD-341 EPI atualizado com sucesso.");
      refreshData();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  private void setStatus(boolean active) {
    try {
      EpiSummary selected = table.getSelectionModel().getSelectedItem();
      if (selected == null) {
        throw new IllegalArgumentException("CAD-039 Alvo de edicao/inativacao nao encontrado.");
      }
      if (!active && !confirmDeactivate(selected.description())) {
        return;
      }
      epiService.setEpiStatus(actor.id(), selected.id(), active);
      showSuccess(active ? "CAD-343 EPI reativado." : "CAD-342 EPI inativado.");
      refreshData();
    } catch (RuntimeException ex) {
      showError(ex.getMessage());
    }
  }

  public void refreshData() {
    epiItems.setAll(epiService.listEpi(actor.id(), searchField.getText()));
  }

  private void clearForm() {
    epiCodeField.clear();
    descriptionField.clear();
    manufacturerField.clear();
    annexGroupCombo.getSelectionModel().clearSelection();
    activeCheck.setSelected(true);
    table.getSelectionModel().clearSelection();
    feedbackLabel.setText("");
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

  private boolean confirmDeactivate(String description) {
    Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
    dialog.setTitle("Confirmar inativacao");
    dialog.setHeaderText("Inativar EPI");
    dialog.setContentText("Deseja inativar o EPI " + description + "?");
    return dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
  }
}
