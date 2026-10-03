package br.com.easynr6.gestaoepi.ui.cadastro;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository.EmployeeSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.JobRoleOption;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.cadastro.PlanoCadastroTrabalhador.Passo;
import br.com.easynr6.gestaoepi.ui.shell.Destino;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** UC-CAD-03. Lista e mantém trabalhador apto a receber EPI. */
public final class EmployeeManagementView {

  private enum StatusOpcao {
    ATIVO("Ativo", true),
    INATIVO("Inativo", false);

    private final String rotulo;
    private final boolean ativo;

    StatusOpcao(String rotulo, boolean ativo) {
      this.rotulo = rotulo;
      this.ativo = ativo;
    }

    private boolean ativo() {
      return ativo;
    }

    @Override
    public String toString() {
      return rotulo;
    }
  }

  private enum Tom {
    OK,
    ERRO,
    AVISO
  }

  private final UsuarioAutenticado usuario;
  private final EmployeeManagementService empregados;
  private final TextField filtro = new TextField();
  private final TextField matriculaField = new TextField();
  private final TextField nomeField = new TextField();
  private final ComboBox<DepartmentOption> setorCombo = new ComboBox<>();
  private final ComboBox<JobRoleOption> funcaoCombo = new ComboBox<>();
  private final ComboBox<EmployeeOption> gestorCombo = new ComboBox<>();
  private final ComboBox<StatusOpcao> statusCombo = new ComboBox<>();
  private final Button salvar = new Button("Salvar");
  private final Button inativar = new Button("Inativar");
  private final Button reativar = new Button("Reativar");
  private final TableView<EmployeeSummary> tabela = new TableView<>();
  private final Label tituloForm = new Label("Novo trabalhador");
  private final Label feedbackLabel = new Label();
  private final Label vazio = new Label("Nenhum trabalhador cadastrado.");
  private final Label banner = new Label();
  private final HBox avisoSetor = new HBox(8);
  private final Node root;
  private EmployeeSummary editando;
  private boolean sincronizando;
  private boolean recarregando;

  public EmployeeManagementView(
      UsuarioAutenticado usuario, EmployeeManagementService empregados, Consumer<Destino> navegar) {
    this.usuario = usuario;
    this.empregados = empregados;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-CAD-03",
            "Trabalhadores",
            "Matrícula, setor, função e gestor. A unidade vem do setor.");
    page.section(montarAviso(navegar)).section(montarCorpo());
    this.root = ReferenciaPage.scroll(page);
    carregarTela();
  }

  public Node root() {
    return root;
  }

  private Node montarCorpo() {
    configurarTabela();
    avisoSetor.setAlignment(Pos.CENTER_LEFT);
    filtro.setPromptText("Matrícula ou nome");
    filtro.textProperty().addListener((obs, anterior, atual) -> recarregarLista());
    Button novo = new Button("Novo trabalhador");
    novo.getStyleClass().add(Styles.ACCENT);
    novo.setOnAction(event -> novoTrabalhador());

    VBox lista = painel("Lista", filtro, novo, tabela);
    HBox.setHgrow(lista, Priority.ALWAYS);
    VBox.setVgrow(tabela, Priority.ALWAYS);
    tabela.setPrefHeight(420);

    VBox formulario = montarFormulario();
    formulario.setPrefWidth(380);
    formulario.setMinWidth(320);
    HBox linha = new HBox(12, lista, formulario);
    linha.setFillHeight(true);
    linha.setAlignment(Pos.TOP_LEFT);
    return linha;
  }

  private VBox montarFormulario() {
    setorCombo.setConverter(nomes(item -> item.unitName() + " · " + item.name()));
    setorCombo.setMaxWidth(Double.MAX_VALUE);
    setorCombo.setPromptText("Selecione o setor");
    setorCombo
        .valueProperty()
        .addListener(
            (obs, anterior, atual) -> {
              if (!sincronizando) {
                carregarFuncoes(atual == null ? null : atual.id(), null, null);
                Long unitId = atual == null ? null : atual.unitId();
                Long unitAnterior = anterior == null ? null : anterior.unitId();
                boolean mesmaUnidade = Objects.equals(unitId, unitAnterior);
                EmployeeOption gestor = mesmaUnidade ? gestorCombo.getValue() : null;
                carregarGestores(
                    unitId,
                    editando == null ? null : editando.id(),
                    gestor == null ? null : gestor.id(),
                    gestor == null ? null : gestor.fullName());
                atualizarBotaoSalvar();
              }
            });
    funcaoCombo.setConverter(nomes(JobRoleOption::name));
    funcaoCombo.setMaxWidth(Double.MAX_VALUE);
    funcaoCombo.setPromptText("Selecione a função");
    gestorCombo.setConverter(
        nomes(
            item ->
                item.employeeCode() == null || item.employeeCode().isBlank()
                    ? item.fullName()
                    : item.fullName() + " · " + item.employeeCode()));
    gestorCombo.setMaxWidth(Double.MAX_VALUE);
    gestorCombo.setPromptText("Sem gestor");
    statusCombo.getItems().setAll(StatusOpcao.values());
    statusCombo.setValue(StatusOpcao.ATIVO);
    statusCombo.setMaxWidth(Double.MAX_VALUE);
    matriculaField.setPromptText("Ex.: 4418");
    nomeField.setPromptText("Nome completo");
    matriculaField.textProperty().addListener((obs, anterior, atual) -> atualizarBotaoSalvar());
    nomeField.textProperty().addListener((obs, anterior, atual) -> atualizarBotaoSalvar());
    funcaoCombo.valueProperty().addListener((obs, anterior, atual) -> atualizarBotaoSalvar());

    salvar.getStyleClass().add(Styles.ACCENT);
    salvar.setDisable(true);
    salvar.setOnAction(event -> salvar());
    inativar.getStyleClass().add(Styles.DANGER);
    inativar.setOnAction(event -> inativarSelecionado());
    inativar.setVisible(false);
    inativar.setManaged(false);
    reativar.setOnAction(event -> reativarSelecionado());
    reativar.setVisible(false);
    reativar.setManaged(false);
    Button limpar = new Button("Limpar");
    limpar.setOnAction(event -> novoTrabalhador());
    feedbackLabel.setWrapText(true);
    feedbackLabel.setMaxWidth(Double.MAX_VALUE);
    tituloForm.getStyleClass().add(Enr6Styles.EMPHASIS);

    VBox box = new VBox(8);
    box.getStyleClass().add(Enr6Styles.PANEL);
    box.getChildren()
        .addAll(
            tituloForm,
            campo("Matrícula", matriculaField),
            campo("Nome completo", nomeField),
            campo("Setor", setorCombo),
            campo("Função", funcaoCombo),
            campo("Gestor", gestorCombo),
            campo("Status", statusCombo),
            feedbackLabel,
            new HBox(8, salvar, inativar, reativar, limpar));
    return box;
  }

  private void configurarTabela() {
    tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabela.setPlaceholder(vazio);
    tabela
        .getColumns()
        .setAll(
            List.of(
                coluna("Matrícula", EmployeeSummary::employeeCode),
                coluna("Nome", EmployeeSummary::fullName),
                coluna("Setor", item -> item.unitName() + " · " + item.departmentName()),
                coluna("Função", EmployeeSummary::jobRoleName),
                coluna("Gestor", item -> item.managerName() == null ? "" : item.managerName()),
                coluna("Status", item -> item.active() ? "Ativo" : "Inativo")));
    tabela
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, anterior, atual) -> {
              if (atual != null && !recarregando) {
                preencher(atual, true);
              }
            });
  }

  private Node montarAviso(Consumer<Destino> navegar) {
    banner.setText("Cadastre um setor e uma função ativos antes de incluir um trabalhador.");
    banner.setWrapText(true);
    banner.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(banner, Priority.ALWAYS);
    avisoSetor.getStyleClass().add(Enr6Styles.BANNER_INFO);
    Button ir = new Button("Ir para setores e funções");
    ir.setOnAction(event -> navegar.accept(Destino.SETORES));
    avisoSetor.getChildren().addAll(banner, ir);
    avisoSetor.setVisible(false);
    avisoSetor.setManaged(false);
    return avisoSetor;
  }

  private void carregarTela() {
    try {
      atualizarAvisoSetor();
      recarregarLista();
      prepararNovo();
    } catch (RuntimeException ex) {
      feedback(MensagensCadastroTrabalhador.erro(ex), Tom.ERRO);
    }
  }

  private void novoTrabalhador() {
    tabela.getSelectionModel().clearSelection();
    prepararNovo();
    feedback("", Tom.AVISO);
  }

  private void prepararNovo() {
    editando = null;
    sincronizando = true;
    try {
      tituloForm.setText("Novo trabalhador");
      matriculaField.clear();
      matriculaField.setEditable(true);
      nomeField.clear();
      carregarSetores(null);
      setorCombo.getSelectionModel().clearSelection();
      funcaoCombo.getItems().clear();
      funcaoCombo.getSelectionModel().clearSelection();
      carregarGestores(null, null, null, null);
      statusCombo.setValue(StatusOpcao.ATIVO);
      limparMarcacoes();
    } finally {
      sincronizando = false;
    }
    atualizarBotaoSalvar();
  }

  private void preencher(EmployeeSummary empregado, boolean limparAviso) {
    editando = empregado;
    sincronizando = true;
    try {
      tituloForm.setText("Editar · " + empregado.employeeCode());
      matriculaField.setText(empregado.employeeCode());
      matriculaField.setEditable(false);
      nomeField.setText(empregado.fullName());
      carregarSetores(empregado);
      selecionarSetor(empregado.departmentId());
      carregarFuncoes(empregado.departmentId(), empregado.jobRoleId(), empregado.jobRoleName());
      carregarGestores(
          empregado.unitId(), empregado.id(), empregado.managerId(), empregado.managerName());
      statusCombo.setValue(empregado.active() ? StatusOpcao.ATIVO : StatusOpcao.INATIVO);
      limparMarcacoes();
      if (limparAviso) {
        feedback("", Tom.AVISO);
      }
    } finally {
      sincronizando = false;
    }
    atualizarBotaoSalvar();
  }

  private void salvar() {
    limparMarcacoes();
    if (setorSemFuncao()) {
      feedback(
          "CAD-002 Este setor não tem função ativa. Cadastre a função em Setores e funções.",
          Tom.ERRO);
      marcar(funcaoCombo, true);
      return;
    }
    if (!obrigatoriosPreenchidos()) {
      feedback("CAD-004 Informe matrícula, nome, setor e função.", Tom.ERRO);
      return;
    }
    EmployeeSummary atual = editando;
    boolean novo = atual == null;
    boolean ficaAtivo = statusEscolhido();
    boolean estavaAtivo = atual == null || atual.active();
    PlanoCadastroTrabalhador plano =
        PlanoCadastroTrabalhador.de(novo, estavaAtivo, ficaAtivo, novo || dadosAlterados(atual));
    if (plano.vazio()) {
      feedback("Nenhuma alteração para salvar.", Tom.AVISO);
      return;
    }
    if (plano.confirmarInativacao() && atual != null && !confirmarInativacao(atual)) {
      return;
    }
    try {
      executar(plano, atual, ficaAtivo);
      editando = null;
      recarregarLista();
      prepararNovo();
      tabela.getSelectionModel().clearSelection();
      feedback(novo ? "Trabalhador cadastrado." : "Alterações salvas.", Tom.OK);
    } catch (RuntimeException ex) {
      feedback(MensagensCadastroTrabalhador.erro(ex), Tom.ERRO);
      recarregarLista();
      if (atual != null) {
        religar(atual.id());
      }
    }
  }

  private void executar(PlanoCadastroTrabalhador plano, EmployeeSummary atual, boolean ficaAtivo) {
    DepartmentOption setor = setorCombo.getValue();
    JobRoleOption funcao = funcaoCombo.getValue();
    if (setor == null || funcao == null) {
      throw new IllegalArgumentException("CAD-004 Campos obrigatorios ausentes.");
    }
    Long id = atual == null ? null : atual.id();
    boolean ativoNaEdicao = atual != null && atual.active();
    for (Passo passo : plano.passos()) {
      aplicar(passo, id, setor.id(), funcao.id(), gestorId(), ficaAtivo, ativoNaEdicao);
    }
  }

  private void aplicar(
      Passo passo,
      Long id,
      Long departmentId,
      Long jobRoleId,
      Long managerId,
      boolean ficaAtivo,
      boolean ativoNaEdicao) {
    switch (passo) {
      case CRIAR ->
          empregados.createEmployee(
              usuario.id(),
              matriculaField.getText(),
              nomeField.getText(),
              departmentId,
              jobRoleId,
              managerId,
              ficaAtivo);
      case ATUALIZAR ->
          empregados.updateEmployee(
              usuario.id(),
              id,
              nomeField.getText(),
              departmentId,
              jobRoleId,
              managerId,
              ativoNaEdicao);
      case INATIVAR -> empregados.setEmployeeStatus(usuario.id(), id, false);
      case REATIVAR -> empregados.setEmployeeStatus(usuario.id(), id, true);
    }
  }

  private boolean statusEscolhido() {
    return statusCombo.getValue() != null && statusCombo.getValue().ativo();
  }

  private void inativarSelecionado() {
    EmployeeSummary atual = editando;
    if (atual == null || !atual.active() || !confirmarInativacao(atual)) {
      return;
    }
    alterarStatus(atual, false, "Trabalhador inativado. O histórico permanece.");
  }

  private void reativarSelecionado() {
    EmployeeSummary atual = editando;
    if (atual == null || atual.active()) {
      return;
    }
    if (dadosAlterados(atual) && !confirmarDescarte(atual, "Reativar")) {
      return;
    }
    alterarStatus(atual, true, "Trabalhador reativado.");
  }

  private void alterarStatus(EmployeeSummary atual, boolean ativo, String sucesso) {
    try {
      empregados.setEmployeeStatus(usuario.id(), atual.id(), ativo);
      recarregarLista();
      religar(atual.id());
      feedback(sucesso, Tom.OK);
    } catch (RuntimeException ex) {
      feedback(MensagensCadastroTrabalhador.erro(ex), Tom.ERRO);
    }
  }

  private boolean confirmarInativacao(EmployeeSummary atual) {
    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
    alerta.setTitle("Inativar trabalhador");
    alerta.setHeaderText("Inativar " + atual.employeeCode() + " · " + nomeInformado() + "?");
    String impacto = "Esta pessoa deixa de receber EPI. O histórico permanece.";
    if (dadosAlterados(atual)) {
      impacto = impacto + " Alterações ainda não salvas neste formulário serão descartadas.";
    }
    alerta.setContentText(impacto);
    ButtonType confirmar = new ButtonType("Inativar", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
    alerta.getButtonTypes().setAll(confirmar, cancelar);
    Optional<ButtonType> resposta = alerta.showAndWait();
    return resposta.isPresent() && resposta.get() == confirmar;
  }

  private boolean confirmarDescarte(EmployeeSummary atual, String acao) {
    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
    alerta.setTitle(acao + " trabalhador");
    alerta.setHeaderText(acao + " " + atual.employeeCode() + " · " + nomeInformado() + "?");
    alerta.setContentText("Alterações ainda não salvas neste formulário serão descartadas.");
    ButtonType seguir = new ButtonType(acao, ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
    alerta.getButtonTypes().setAll(seguir, cancelar);
    Optional<ButtonType> resposta = alerta.showAndWait();
    return resposta.isPresent() && resposta.get() == seguir;
  }

  private void recarregarLista() {
    recarregando = true;
    try {
      Long manter = editando == null ? null : editando.id();
      tabela.getItems().setAll(empregados.listEmployees(usuario.id(), filtro.getText()));
      if (manter != null) {
        tabela.getItems().stream()
            .filter(item -> item.id().equals(manter))
            .findFirst()
            .ifPresent(item -> tabela.getSelectionModel().select(item));
      }
      atualizarVazio();
    } catch (RuntimeException ex) {
      tabela.getItems().clear();
      feedback(MensagensCadastroTrabalhador.erro(ex), Tom.ERRO);
    } finally {
      recarregando = false;
    }
  }

  private void religar(Long id) {
    tabela.getItems().stream()
        .filter(item -> item.id().equals(id))
        .findFirst()
        .ifPresent(item -> preencher(item, false));
  }

  private void atualizarAvisoSetor() {
    boolean semSetor = empregados.listActiveDepartments().isEmpty();
    avisoSetor.setVisible(semSetor);
    avisoSetor.setManaged(semSetor);
  }

  private void atualizarVazio() {
    boolean filtrando = filtro.getText() != null && !filtro.getText().isBlank();
    vazio.setText(
        filtrando ? "Nenhum trabalhador com esse filtro." : "Nenhum trabalhador cadastrado.");
  }

  private void carregarSetores(EmployeeSummary atual) {
    List<DepartmentOption> itens = new ArrayList<>(empregados.listActiveDepartments());
    if (atual != null && itens.stream().noneMatch(item -> item.id().equals(atual.departmentId()))) {
      itens.add(
          new DepartmentOption(
              atual.departmentId(),
              atual.departmentName() + " (inativo)",
              atual.unitId(),
              atual.unitName(),
              false));
    }
    setorCombo.getItems().setAll(itens);
  }

  private void carregarFuncoes(Long departmentId, Long jobRoleId, String jobRoleName) {
    List<JobRoleOption> itens = new ArrayList<>();
    if (departmentId != null) {
      itens.addAll(empregados.listActiveJobRolesByDepartment(departmentId));
    }
    if (jobRoleId != null && itens.stream().noneMatch(item -> item.id().equals(jobRoleId))) {
      itens.add(new JobRoleOption(jobRoleId, jobRoleName + " (inativo)", departmentId, false));
    }
    funcaoCombo.getItems().setAll(itens);
    if (jobRoleId == null) {
      funcaoCombo.getSelectionModel().clearSelection();
      return;
    }
    funcaoCombo.getItems().stream()
        .filter(item -> item.id().equals(jobRoleId))
        .findFirst()
        .ifPresent(funcaoCombo::setValue);
  }

  private void carregarGestores(
      Long unitId, Long excludeEmployeeId, Long managerId, String managerName) {
    List<EmployeeOption> itens = new ArrayList<>(empregados.listActiveEmployeesByUnit(unitId));
    itens.removeIf(item -> item.id().equals(excludeEmployeeId));
    if (managerId != null && itens.stream().noneMatch(item -> item.id().equals(managerId))) {
      itens.add(
          new EmployeeOption(managerId, "", managerName == null ? "Gestor" : managerName, false));
    }
    gestorCombo.getItems().setAll(itens);
    if (managerId == null) {
      gestorCombo.getSelectionModel().clearSelection();
      return;
    }
    gestorCombo.getItems().stream()
        .filter(item -> item.id().equals(managerId))
        .findFirst()
        .ifPresent(gestorCombo::setValue);
  }

  private Long gestorId() {
    EmployeeOption gestor = gestorCombo.getValue();
    return gestor == null ? null : gestor.id();
  }

  private void selecionarSetor(Long departmentId) {
    setorCombo.getItems().stream()
        .filter(item -> item.id().equals(departmentId))
        .findFirst()
        .ifPresent(setorCombo::setValue);
  }

  private boolean setorSemFuncao() {
    return setorCombo.getValue() != null && funcaoCombo.getItems().isEmpty();
  }

  private void atualizarBotaoSalvar() {
    if (sincronizando) {
      return;
    }
    salvar.setDisable(
        !cadastroProntoParaSalvar(
            matriculaField.getText(),
            nomeField.getText(),
            setorCombo.getValue() != null,
            funcaoCombo.getValue() != null));
    boolean pessoaAtiva = editando != null && editando.active();
    inativar.setVisible(pessoaAtiva);
    inativar.setManaged(pessoaAtiva);
    reativar.setVisible(editando != null && !pessoaAtiva);
    reativar.setManaged(editando != null && !pessoaAtiva);
  }

  static boolean cadastroProntoParaSalvar(
      String matricula, String nome, boolean setorSelecionado, boolean funcaoSelecionada) {
    return textoPreenchido(matricula)
        && textoPreenchido(nome)
        && setorSelecionado
        && funcaoSelecionada;
  }

  private static boolean textoPreenchido(String valor) {
    return valor != null && !valor.isBlank();
  }

  private boolean obrigatoriosPreenchidos() {
    boolean matriculaOk = !criando() || !texto(matriculaField).isBlank();
    boolean nomeOk = !texto(nomeField).isBlank();
    boolean setorOk = setorCombo.getValue() != null;
    boolean funcaoOk = funcaoCombo.getValue() != null;
    Enr6Styles.markFieldInvalid(matriculaField, criando() && !matriculaOk);
    Enr6Styles.markFieldInvalid(nomeField, !nomeOk);
    marcar(setorCombo, !setorOk);
    marcar(funcaoCombo, !funcaoOk);
    return matriculaOk && nomeOk && setorOk && funcaoOk;
  }

  private boolean criando() {
    return editando == null;
  }

  private boolean dadosAlterados(EmployeeSummary atual) {
    DepartmentOption setor = setorCombo.getValue();
    JobRoleOption funcao = funcaoCombo.getValue();
    return !texto(nomeField).equals(atual.fullName().trim())
        || setor == null
        || !setor.id().equals(atual.departmentId())
        || funcao == null
        || !funcao.id().equals(atual.jobRoleId())
        || !Objects.equals(gestorId(), atual.managerId());
  }

  private String nomeInformado() {
    String nome = texto(nomeField);
    return nome.isBlank() ? editando.fullName() : nome;
  }

  private static String texto(TextField field) {
    return field.getText() == null ? "" : field.getText().trim();
  }

  private void limparMarcacoes() {
    Enr6Styles.markFieldInvalid(matriculaField, false);
    Enr6Styles.markFieldInvalid(nomeField, false);
    marcar(setorCombo, false);
    marcar(funcaoCombo, false);
  }

  private void feedback(String texto, Tom tom) {
    feedbackLabel.setText(texto == null ? "" : texto);
    switch (tom) {
      case OK -> Enr6Styles.markOk(feedbackLabel);
      case ERRO -> Enr6Styles.markDanger(feedbackLabel);
      case AVISO -> Enr6Styles.markWarn(feedbackLabel);
    }
    if (feedbackLabel.getText().isBlank()) {
      feedbackLabel
          .getStyleClass()
          .removeAll(Enr6Styles.FEEDBACK_OK, Enr6Styles.FEEDBACK_DANGER, Enr6Styles.FEEDBACK_WARN);
    }
  }

  private static void marcar(Node node, boolean invalido) {
    if (invalido) {
      if (!node.getStyleClass().contains(Enr6Styles.FIELD_INVALID)) {
        node.getStyleClass().add(Enr6Styles.FIELD_INVALID);
      }
      return;
    }
    node.getStyleClass().remove(Enr6Styles.FIELD_INVALID);
  }

  private static TableColumn<EmployeeSummary, String> coluna(
      String titulo, Function<EmployeeSummary, String> valor) {
    TableColumn<EmployeeSummary, String> column = new TableColumn<>(titulo);
    column.setCellValueFactory(data -> new SimpleStringProperty(valor.apply(data.getValue())));
    return column;
  }

  private static VBox campo(String rotulo, Node editor) {
    if (editor instanceof TextInputControl input) {
      input.setMaxWidth(Double.MAX_VALUE);
    }
    VBox box = new VBox(4, new Label(rotulo), editor);
    box.setAlignment(Pos.CENTER_LEFT);
    return box;
  }

  private static VBox painel(String titulo, Node... filhos) {
    Label title = new Label(titulo);
    title.getStyleClass().add(Enr6Styles.EMPHASIS);
    VBox box = new VBox(8);
    box.getStyleClass().add(Enr6Styles.PANEL);
    box.getChildren().add(title);
    box.getChildren().addAll(filhos);
    return box;
  }

  private static <T> StringConverter<T> nomes(Function<T, String> nome) {
    return new StringConverter<>() {
      @Override
      public String toString(T item) {
        return item == null ? "" : nome.apply(item);
      }

      @Override
      public T fromString(String value) {
        return null;
      }
    };
  }
}
