package br.com.easynr6.gestaoepi.ui.cadastro;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentOption;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.DepartmentSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.JobRoleSummary;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.UnitOption;
import br.com.easynr6.gestaoepi.modules.employee.domain.UnitPolicy;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.MensagemTemporaria;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** UC-CAD-02. Mantém setor por unidade e função por setor, sem apagar histórico. */
public final class OrgStructureManagementView {

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
  private final EmployeeManagementService estrutura;
  private final TextField filtroSetor = new TextField();
  private final TextField nomeSetor = new TextField();
  private final ComboBox<UnitOption> unidadeCombo = new ComboBox<>();
  private final ComboBox<StatusOpcao> statusSetor = new ComboBox<>();
  private final TableView<DepartmentSummary> tabelaSetor = new TableView<>();
  private final Button salvarSetor = new Button("Salvar");
  private final Button inativarSetor = new Button("Inativar");
  private final Button reativarSetor = new Button("Reativar");
  private final Label feedbackSetor = new Label();
  private final Label vazioSetor = new Label("Nenhum setor cadastrado.");
  private final TextField filtroFuncao = new TextField();
  private final TextField nomeFuncao = new TextField();
  private final ComboBox<DepartmentOption> setorDaFuncao = new ComboBox<>();
  private final ComboBox<StatusOpcao> statusFuncao = new ComboBox<>();
  private final TableView<JobRoleSummary> tabelaFuncao = new TableView<>();
  private final Button salvarFuncao = new Button("Salvar");
  private final Button inativarFuncao = new Button("Inativar");
  private final Button reativarFuncao = new Button("Reativar");
  private final Label feedbackFuncao = new Label();
  private final Label vazioFuncao = new Label("Nenhuma função cadastrada.");
  private final Label avisoFuncao = new Label();
  private final Node root;
  private DepartmentSummary editandoSetor;
  private JobRoleSummary editandoFuncao;
  private boolean sincronizandoSetor;
  private boolean sincronizandoFuncao;
  private boolean recarregandoSetor;
  private boolean recarregandoFuncao;
  private final MensagemTemporaria mensagens = new MensagemTemporaria();

  public OrgStructureManagementView(
      UsuarioAutenticado usuario, EmployeeManagementService estrutura) {
    this.usuario = usuario;
    this.estrutura = estrutura;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-CAD-02",
            "Setores e funções",
            "O setor pertence a uma unidade. A função pertence a um setor ativo.");
    TabPane abas = new TabPane();
    abas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    abas.getTabs().add(new Tab("Setores", montarSetores()));
    abas.getTabs().add(new Tab("Funções", montarFuncoes()));
    abas.getSelectionModel()
        .selectedIndexProperty()
        .addListener(
            (obs, anterior, atual) -> {
              if (atual != null && atual.intValue() == 1) {
                carregarSetoresDaFuncao(editandoFuncao);
                if (editandoFuncao != null) {
                  selecionarSetorDaFuncao(editandoFuncao.departmentId());
                }
              }
            });
    page.section(abas);
    this.root = ReferenciaPage.scroll(page);
    carregarSetores();
    prepararNovoSetor();
    carregarFuncoes();
    prepararNovaFuncao();
  }

  public Node root() {
    return root;
  }

  static boolean setorProntoParaSalvar(boolean unidadeSelecionada, String nome) {
    return unidadeSelecionada && textoPreenchido(nome);
  }

  static boolean funcaoProntaParaSalvar(boolean setorSelecionado, String nome) {
    return setorSelecionado && textoPreenchido(nome);
  }

  static String rotuloUnidade(String nome, String cnpj) {
    String documento = formatarCnpj(cnpj);
    if (documento.isBlank()) {
      return nome == null ? "" : nome;
    }
    return nome + " · " + documento;
  }

  static String formatarCnpj(String cnpj) {
    return UnitPolicy.formatarCnpj(cnpj);
  }

  private Node montarSetores() {
    configurarTabelaSetor();
    filtroSetor.setPromptText("Nome do setor");
    filtroSetor.textProperty().addListener((obs, anterior, atual) -> carregarSetores());
    unidadeCombo.setConverter(nomes(item -> rotuloUnidade(item.name(), item.cnpj())));
    unidadeCombo.setMaxWidth(Double.MAX_VALUE);
    unidadeCombo.setPromptText("Selecione a unidade");
    unidadeCombo.valueProperty().addListener((obs, anterior, atual) -> atualizarSalvarSetor());
    nomeSetor.setPromptText("Ex.: Guarda");
    nomeSetor.textProperty().addListener((obs, anterior, atual) -> atualizarSalvarSetor());
    prepararStatus(statusSetor);
    prepararAcao(salvarSetor, inativarSetor, reativarSetor);
    salvarSetor.setOnAction(event -> salvarSetor());
    inativarSetor.setOnAction(event -> inativarSetor());
    reativarSetor.setOnAction(event -> reativarSetor());
    Button novo = new Button("Novo setor");
    novo.getStyleClass().add(Styles.ACCENT);
    novo.setOnAction(event -> novoSetor());
    Button limpar = new Button("Limpar");
    limpar.setOnAction(event -> novoSetor());
    feedbackSetor.setWrapText(true);
    feedbackSetor.setMaxWidth(Double.MAX_VALUE);

    VBox lista = painel("Lista", filtroSetor, novo, tabelaSetor);
    HBox.setHgrow(lista, Priority.ALWAYS);
    VBox.setVgrow(tabelaSetor, Priority.ALWAYS);
    tabelaSetor.setPrefHeight(420);
    VBox formulario =
        painel(
            "Setor",
            campo("Unidade", unidadeCombo),
            campo("Nome", nomeSetor),
            campo("Status inicial", statusSetor),
            feedbackSetor,
            new HBox(8, salvarSetor, inativarSetor, reativarSetor, limpar));
    formulario.setPrefWidth(360);
    formulario.setMinWidth(300);
    return new HBox(12, lista, formulario);
  }

  private Node montarFuncoes() {
    configurarTabelaFuncao();
    filtroFuncao.setPromptText("Função ou setor");
    filtroFuncao.textProperty().addListener((obs, anterior, atual) -> carregarFuncoes());
    setorDaFuncao.setConverter(nomes(item -> item.unitName() + " · " + item.name()));
    setorDaFuncao.setMaxWidth(Double.MAX_VALUE);
    setorDaFuncao.setPromptText("Selecione o setor");
    setorDaFuncao.valueProperty().addListener((obs, anterior, atual) -> atualizarSalvarFuncao());
    nomeFuncao.setPromptText("Ex.: Auxiliar de guarda");
    nomeFuncao.textProperty().addListener((obs, anterior, atual) -> atualizarSalvarFuncao());
    prepararStatus(statusFuncao);
    prepararAcao(salvarFuncao, inativarFuncao, reativarFuncao);
    salvarFuncao.setOnAction(event -> salvarFuncao());
    inativarFuncao.setOnAction(event -> inativarFuncao());
    reativarFuncao.setOnAction(event -> reativarFuncao());
    avisoFuncao.setWrapText(true);
    avisoFuncao.getStyleClass().add(Enr6Styles.BANNER_INFO);
    avisoFuncao.setText("Cadastre um setor ativo antes de incluir uma função.");
    Button novo = new Button("Nova função");
    novo.getStyleClass().add(Styles.ACCENT);
    novo.setOnAction(event -> novaFuncao());
    Button limpar = new Button("Limpar");
    limpar.setOnAction(event -> novaFuncao());
    feedbackFuncao.setWrapText(true);
    feedbackFuncao.setMaxWidth(Double.MAX_VALUE);

    VBox lista = painel("Lista", filtroFuncao, novo, tabelaFuncao);
    HBox.setHgrow(lista, Priority.ALWAYS);
    VBox.setVgrow(tabelaFuncao, Priority.ALWAYS);
    tabelaFuncao.setPrefHeight(420);
    VBox formulario =
        painel(
            "Função",
            avisoFuncao,
            campo("Setor", setorDaFuncao),
            campo("Nome", nomeFuncao),
            campo("Status inicial", statusFuncao),
            feedbackFuncao,
            new HBox(8, salvarFuncao, inativarFuncao, reativarFuncao, limpar));
    formulario.setPrefWidth(360);
    formulario.setMinWidth(300);
    return new HBox(12, lista, formulario);
  }

  private void configurarTabelaSetor() {
    tabelaSetor.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabelaSetor.setPlaceholder(vazioSetor);
    tabelaSetor
        .getColumns()
        .setAll(
            List.of(
                colunaSetor("Unidade", DepartmentSummary::unitName),
                colunaSetor("Setor", DepartmentSummary::name),
                colunaSetor("Status", item -> item.active() ? "Ativo" : "Inativo")));
    tabelaSetor
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, anterior, atual) -> {
              if (atual != null && !recarregandoSetor) {
                preencherSetor(atual, true);
              }
            });
  }

  private void configurarTabelaFuncao() {
    tabelaFuncao.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabelaFuncao.setPlaceholder(vazioFuncao);
    tabelaFuncao
        .getColumns()
        .setAll(
            List.of(
                colunaFuncao("Setor", item -> item.unitName() + " · " + item.departmentName()),
                colunaFuncao("Função", JobRoleSummary::name),
                colunaFuncao("Status", item -> item.active() ? "Ativo" : "Inativo")));
    tabelaFuncao
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, anterior, atual) -> {
              if (atual != null && !recarregandoFuncao) {
                preencherFuncao(atual, true);
              }
            });
  }

  private void novoSetor() {
    tabelaSetor.getSelectionModel().clearSelection();
    prepararNovoSetor();
    feedback(feedbackSetor, "", Tom.AVISO);
  }

  private void prepararNovoSetor() {
    editandoSetor = null;
    sincronizandoSetor = true;
    try {
      nomeSetor.clear();
      carregarUnidades(null);
      unidadeCombo.getSelectionModel().clearSelection();
      unidadeCombo.setDisable(false);
      statusSetor.setValue(StatusOpcao.ATIVO);
      statusSetor.setDisable(false);
      limparMarcacaoSetor();
    } finally {
      sincronizandoSetor = false;
    }
    atualizarSalvarSetor();
  }

  private void preencherSetor(DepartmentSummary setor, boolean limparAviso) {
    editandoSetor = setor;
    sincronizandoSetor = true;
    try {
      nomeSetor.setText(setor.name());
      carregarUnidades(setor);
      selecionarUnidade(setor.unitId());
      unidadeCombo.setDisable(true);
      statusSetor.setValue(setor.active() ? StatusOpcao.ATIVO : StatusOpcao.INATIVO);
      statusSetor.setDisable(true);
      limparMarcacaoSetor();
      if (limparAviso) {
        feedback(feedbackSetor, "", Tom.AVISO);
      }
    } finally {
      sincronizandoSetor = false;
    }
    atualizarSalvarSetor();
  }

  private void salvarSetor() {
    limparMarcacaoSetor();
    UnitOption unidade = unidadeCombo.getValue();
    if (unidade == null || !setorProntoParaSalvar(true, nomeSetor.getText())) {
      feedback(feedbackSetor, "Informe a unidade e o nome do setor.", Tom.ERRO);
      marcar(unidadeCombo, unidade == null);
      Enr6Styles.markFieldInvalid(nomeSetor, !textoPreenchido(nomeSetor.getText()));
      return;
    }
    DepartmentSummary atual = editandoSetor;
    boolean novo = atual == null;
    if (!novo && nomeSetor.getText().trim().equals(atual.name().trim())) {
      feedback(feedbackSetor, "Nenhuma alteração para salvar.", Tom.AVISO);
      return;
    }
    try {
      if (novo) {
        estrutura.createDepartment(
            usuario.id(), nomeSetor.getText(), unidade.id(), statusEscolhido(statusSetor));
      } else {
        estrutura.updateDepartment(usuario.id(), atual.id(), nomeSetor.getText(), atual.active());
      }
      carregarSetores();
      prepararNovoSetor();
      tabelaSetor.getSelectionModel().clearSelection();
      feedback(feedbackSetor, novo ? "Setor cadastrado." : "Alterações salvas.", Tom.OK);
    } catch (RuntimeException ex) {
      feedback(feedbackSetor, MensagensCadastroOrganizacional.erro(ex), Tom.ERRO);
    }
  }

  private void inativarSetor() {
    DepartmentSummary atual = editandoSetor;
    if (atual == null || !atual.active()) {
      return;
    }
    if (!confirmar(
        "Inativar setor",
        "Inativar " + atual.unitName() + " · " + atual.name() + "?",
        "Funções ativas impedem a inativação. O histórico permanece.")) {
      return;
    }
    alterarStatusSetor(atual, false, "Setor inativado. O histórico permanece.");
  }

  private void reativarSetor() {
    DepartmentSummary atual = editandoSetor;
    if (atual == null || atual.active()) {
      return;
    }
    alterarStatusSetor(atual, true, "Setor reativado.");
  }

  private void alterarStatusSetor(DepartmentSummary atual, boolean ativo, String sucesso) {
    try {
      estrutura.setDepartmentStatus(usuario.id(), atual.id(), ativo);
      carregarSetores();
      religarSetor(atual.id());
      feedback(feedbackSetor, sucesso, Tom.OK);
    } catch (RuntimeException ex) {
      feedback(feedbackSetor, MensagensCadastroOrganizacional.erro(ex), Tom.ERRO);
    }
  }

  private void carregarSetores() {
    recarregandoSetor = true;
    try {
      Long manter = editandoSetor == null ? null : editandoSetor.id();
      tabelaSetor.getItems().setAll(estrutura.listDepartments(usuario.id(), filtroSetor.getText()));
      if (manter != null) {
        tabelaSetor.getItems().stream()
            .filter(item -> item.id().equals(manter))
            .findFirst()
            .ifPresent(item -> tabelaSetor.getSelectionModel().select(item));
      }
      boolean filtrando = textoPreenchido(filtroSetor.getText());
      vazioSetor.setText(filtrando ? "Nenhum setor com esse filtro." : "Nenhum setor cadastrado.");
    } catch (RuntimeException ex) {
      tabelaSetor.getItems().clear();
      feedback(feedbackSetor, MensagensCadastroOrganizacional.erro(ex), Tom.ERRO);
    } finally {
      recarregandoSetor = false;
    }
  }

  private void religarSetor(Long id) {
    tabelaSetor.getItems().stream()
        .filter(item -> item.id().equals(id))
        .findFirst()
        .ifPresent(item -> preencherSetor(item, false));
  }

  private void carregarUnidades(DepartmentSummary atual) {
    List<UnitOption> itens = new ArrayList<>(estrutura.listActiveUnits());
    if (atual != null && itens.stream().noneMatch(item -> item.id().equals(atual.unitId()))) {
      itens.add(new UnitOption(atual.unitId(), atual.unitName(), ""));
    }
    unidadeCombo.getItems().setAll(itens);
  }

  private void selecionarUnidade(Long unitId) {
    unidadeCombo.getItems().stream()
        .filter(item -> item.id().equals(unitId))
        .findFirst()
        .ifPresent(unidadeCombo::setValue);
  }

  private void atualizarSalvarSetor() {
    if (sincronizandoSetor) {
      return;
    }
    salvarSetor.setDisable(
        !setorProntoParaSalvar(unidadeCombo.getValue() != null, nomeSetor.getText()));
    boolean ativo = editandoSetor != null && editandoSetor.active();
    inativarSetor.setVisible(ativo);
    inativarSetor.setManaged(ativo);
    reativarSetor.setVisible(editandoSetor != null && !ativo);
    reativarSetor.setManaged(editandoSetor != null && !ativo);
  }

  private void limparMarcacaoSetor() {
    Enr6Styles.markFieldInvalid(nomeSetor, false);
    marcar(unidadeCombo, false);
  }

  private void novaFuncao() {
    tabelaFuncao.getSelectionModel().clearSelection();
    prepararNovaFuncao();
    feedback(feedbackFuncao, "", Tom.AVISO);
  }

  private void prepararNovaFuncao() {
    editandoFuncao = null;
    sincronizandoFuncao = true;
    try {
      nomeFuncao.clear();
      carregarSetoresDaFuncao(null);
      setorDaFuncao.getSelectionModel().clearSelection();
      statusFuncao.setValue(StatusOpcao.ATIVO);
      statusFuncao.setDisable(false);
      limparMarcacaoFuncao();
    } finally {
      sincronizandoFuncao = false;
    }
    atualizarSalvarFuncao();
  }

  private void preencherFuncao(JobRoleSummary funcao, boolean limparAviso) {
    editandoFuncao = funcao;
    sincronizandoFuncao = true;
    try {
      nomeFuncao.setText(funcao.name());
      carregarSetoresDaFuncao(funcao);
      selecionarSetorDaFuncao(funcao.departmentId());
      statusFuncao.setValue(funcao.active() ? StatusOpcao.ATIVO : StatusOpcao.INATIVO);
      statusFuncao.setDisable(true);
      limparMarcacaoFuncao();
      if (limparAviso) {
        feedback(feedbackFuncao, "", Tom.AVISO);
      }
    } finally {
      sincronizandoFuncao = false;
    }
    atualizarSalvarFuncao();
  }

  private void salvarFuncao() {
    limparMarcacaoFuncao();
    DepartmentOption setor = setorDaFuncao.getValue();
    if (setor == null || !funcaoProntaParaSalvar(true, nomeFuncao.getText())) {
      feedback(feedbackFuncao, "Informe o setor e o nome da função.", Tom.ERRO);
      marcar(setorDaFuncao, setor == null);
      Enr6Styles.markFieldInvalid(nomeFuncao, !textoPreenchido(nomeFuncao.getText()));
      return;
    }
    JobRoleSummary atual = editandoFuncao;
    boolean novo = atual == null;
    boolean mesmoNome = !novo && nomeFuncao.getText().trim().equals(atual.name().trim());
    boolean mesmoSetor = !novo && setor.id().equals(atual.departmentId());
    if (mesmoNome && mesmoSetor) {
      feedback(feedbackFuncao, "Nenhuma alteração para salvar.", Tom.AVISO);
      return;
    }
    try {
      if (novo) {
        estrutura.createJobRole(
            usuario.id(), nomeFuncao.getText(), setor.id(), statusEscolhido(statusFuncao));
      } else {
        estrutura.updateJobRole(
            usuario.id(), atual.id(), nomeFuncao.getText(), setor.id(), atual.active());
      }
      carregarFuncoes();
      prepararNovaFuncao();
      tabelaFuncao.getSelectionModel().clearSelection();
      feedback(feedbackFuncao, novo ? "Função cadastrada." : "Alterações salvas.", Tom.OK);
    } catch (RuntimeException ex) {
      feedback(feedbackFuncao, MensagensCadastroOrganizacional.erro(ex), Tom.ERRO);
    }
  }

  private void inativarFuncao() {
    JobRoleSummary atual = editandoFuncao;
    if (atual == null || !atual.active()) {
      return;
    }
    if (!confirmar(
        "Inativar função",
        "Inativar " + atual.name() + "?",
        "Trabalhadores ativos impedem a inativação. O histórico permanece.")) {
      return;
    }
    alterarStatusFuncao(atual, false, "Função inativada. O histórico permanece.");
  }

  private void reativarFuncao() {
    JobRoleSummary atual = editandoFuncao;
    if (atual == null || atual.active()) {
      return;
    }
    alterarStatusFuncao(atual, true, "Função reativada.");
  }

  private void alterarStatusFuncao(JobRoleSummary atual, boolean ativo, String sucesso) {
    try {
      estrutura.setJobRoleStatus(usuario.id(), atual.id(), ativo);
      carregarFuncoes();
      religarFuncao(atual.id());
      feedback(feedbackFuncao, sucesso, Tom.OK);
    } catch (RuntimeException ex) {
      feedback(feedbackFuncao, MensagensCadastroOrganizacional.erro(ex), Tom.ERRO);
    }
  }

  private void carregarFuncoes() {
    recarregandoFuncao = true;
    try {
      Long manter = editandoFuncao == null ? null : editandoFuncao.id();
      tabelaFuncao.getItems().setAll(estrutura.listJobRoles(usuario.id(), filtroFuncao.getText()));
      if (manter != null) {
        tabelaFuncao.getItems().stream()
            .filter(item -> item.id().equals(manter))
            .findFirst()
            .ifPresent(item -> tabelaFuncao.getSelectionModel().select(item));
      }
      boolean filtrando = textoPreenchido(filtroFuncao.getText());
      vazioFuncao.setText(
          filtrando ? "Nenhuma função com esse filtro." : "Nenhuma função cadastrada.");
    } catch (RuntimeException ex) {
      tabelaFuncao.getItems().clear();
      feedback(feedbackFuncao, MensagensCadastroOrganizacional.erro(ex), Tom.ERRO);
    } finally {
      recarregandoFuncao = false;
    }
  }

  private void religarFuncao(Long id) {
    tabelaFuncao.getItems().stream()
        .filter(item -> item.id().equals(id))
        .findFirst()
        .ifPresent(item -> preencherFuncao(item, false));
  }

  private void carregarSetoresDaFuncao(JobRoleSummary atual) {
    List<DepartmentOption> itens = new ArrayList<>(estrutura.listActiveDepartments());
    boolean semSetor = itens.isEmpty();
    avisoFuncao.setVisible(semSetor);
    avisoFuncao.setManaged(semSetor);
    if (atual != null && itens.stream().noneMatch(item -> item.id().equals(atual.departmentId()))) {
      itens.add(
          new DepartmentOption(
              atual.departmentId(),
              atual.departmentName() + " (inativo)",
              null,
              atual.unitName(),
              false));
    }
    setorDaFuncao.getItems().setAll(itens);
  }

  private void selecionarSetorDaFuncao(Long departmentId) {
    setorDaFuncao.getItems().stream()
        .filter(item -> item.id().equals(departmentId))
        .findFirst()
        .ifPresent(setorDaFuncao::setValue);
  }

  private void atualizarSalvarFuncao() {
    if (sincronizandoFuncao) {
      return;
    }
    salvarFuncao.setDisable(
        !funcaoProntaParaSalvar(setorDaFuncao.getValue() != null, nomeFuncao.getText()));
    boolean ativo = editandoFuncao != null && editandoFuncao.active();
    inativarFuncao.setVisible(ativo);
    inativarFuncao.setManaged(ativo);
    reativarFuncao.setVisible(editandoFuncao != null && !ativo);
    reativarFuncao.setManaged(editandoFuncao != null && !ativo);
  }

  private void limparMarcacaoFuncao() {
    Enr6Styles.markFieldInvalid(nomeFuncao, false);
    marcar(setorDaFuncao, false);
  }

  private static void prepararStatus(ComboBox<StatusOpcao> combo) {
    combo.getItems().setAll(StatusOpcao.values());
    combo.setValue(StatusOpcao.ATIVO);
    combo.setMaxWidth(Double.MAX_VALUE);
  }

  private static void prepararAcao(Button salvar, Button inativar, Button reativar) {
    salvar.getStyleClass().add(Styles.ACCENT);
    salvar.setDisable(true);
    inativar.getStyleClass().add(Styles.DANGER);
    inativar.setVisible(false);
    inativar.setManaged(false);
    reativar.setVisible(false);
    reativar.setManaged(false);
  }

  private static boolean statusEscolhido(ComboBox<StatusOpcao> combo) {
    return combo.getValue() != null && combo.getValue().ativo();
  }

  private static boolean confirmar(String titulo, String cabecalho, String conteudo) {
    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
    alerta.setTitle(titulo);
    alerta.setHeaderText(cabecalho);
    alerta.setContentText(conteudo);
    ButtonType seguir = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
    alerta.getButtonTypes().setAll(seguir, cancelar);
    Optional<ButtonType> resposta = alerta.showAndWait();
    return resposta.isPresent() && resposta.get() == seguir;
  }

  private void feedback(Label label, String texto, Tom tom) {
    String visivel = texto == null ? "" : texto;
    label.setText(visivel);
    switch (tom) {
      case OK -> Enr6Styles.markOk(label);
      case ERRO -> Enr6Styles.markDanger(label);
      case AVISO -> Enr6Styles.markWarn(label);
    }
    if (visivel.isBlank()) {
      label
          .getStyleClass()
          .removeAll(Enr6Styles.FEEDBACK_OK, Enr6Styles.FEEDBACK_DANGER, Enr6Styles.FEEDBACK_WARN);
    }
    mensagens.agendar(label, visivel);
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

  private static boolean textoPreenchido(String valor) {
    return valor != null && !valor.isBlank();
  }

  private static TableColumn<DepartmentSummary, String> colunaSetor(
      String titulo, Function<DepartmentSummary, String> valor) {
    TableColumn<DepartmentSummary, String> column = new TableColumn<>(titulo);
    column.setCellValueFactory(data -> new SimpleStringProperty(valor.apply(data.getValue())));
    return column;
  }

  private static TableColumn<JobRoleSummary, String> colunaFuncao(
      String titulo, Function<JobRoleSummary, String> valor) {
    TableColumn<JobRoleSummary, String> column = new TableColumn<>(titulo);
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
