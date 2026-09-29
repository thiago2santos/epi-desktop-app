package br.com.easynr6.gestaoepi.ui.admin;

import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.DuplicateLoginException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UserAdministrationService;
import br.com.easynr6.gestaoepi.shared.auth.UserAdministrationService.UsuarioAdminResumo;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.shared.auth.WeakPasswordException;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class UserAdministrationView extends BorderPane {

  private final UsuarioAutenticado admin;
  private final UserAdministrationService userAdministrationService;
  private final Label tituloLabel = new Label("Administracao de usuarios");
  private final Label subtituloLabel =
      new Label("Gestao de acesso com foco em seguranca e rastreabilidade.");

  public UserAdministrationView(
      UsuarioAutenticado admin, UserAdministrationService userAdministrationService) {
    this.admin = admin;
    this.userAdministrationService = userAdministrationService;

    setPadding(new Insets(16));

    tituloLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
    subtituloLabel.setStyle("-fx-text-fill: #4b5563;");

    VBox topo = new VBox(4, tituloLabel, subtituloLabel);
    topo.setPadding(new Insets(0, 0, 8, 0));

    setTop(topo);
    setCenter(buildAdminTabs());
  }

  private TabPane buildAdminTabs() {
    TabPane tabs = new TabPane();

    Tab hubTab = new Tab("Hub", buildHubBox());
    hubTab.setClosable(false);
    hubTab.setOnSelectionChanged(
        event -> {
          if (hubTab.isSelected()) {
            hubTab.setContent(buildHubBox());
          }
        });

    Tab cadastroTab = new Tab("Cadastrar usuario", buildCadastroBox());
    cadastroTab.setClosable(false);

    Tab papelTab = new Tab("Atribuir papel", buildAtribuicaoPapelBox());
    papelTab.setClosable(false);

    Tab resetTab = new Tab("Resetar credencial", buildResetCredencialBox());
    resetTab.setClosable(false);

    Tab gestaoTab = new Tab("Gerenciar usuarios", buildGestaoUsuariosBox());
    gestaoTab.setClosable(false);

    tabs.getTabs().addAll(hubTab, cadastroTab, papelTab, resetTab, gestaoTab);
    return tabs;
  }

  private VBox buildHubBox() {
    List<UsuarioAdminResumo> usuarios = userAdministrationService.listarUsuarios();
    long ativos = usuarios.stream().filter(UsuarioAdminResumo::ativo).count();
    long bloqueados = usuarios.size() - ativos;
    long trocaObrigatoria = usuarios.stream().filter(UsuarioAdminResumo::trocaObrigatoria).count();

    Label resumo =
        new Label(
            "Usuarios: "
                + usuarios.size()
                + " | Ativos: "
                + ativos
                + " | Bloqueados: "
                + bloqueados
                + " | Troca obrigatoria: "
                + trocaObrigatoria);
    resumo.setStyle("-fx-font-weight: bold;");

    Label orientacao =
        new Label(
            "Use as abas para acessar tarefas dedicadas por caso de uso (cadastro, papel, reset e gestao).");
    orientacao.setWrapText(true);

    return new VBox(12, new Label("Painel administrativo"), resumo, orientacao);
  }

  private VBox buildCadastroBox() {
    TextField nomeField = new TextField();
    nomeField.setPromptText("Nome completo");
    TextField loginField = new TextField();
    loginField.setPromptText("login");
    PasswordField senhaField = new PasswordField();
    senhaField.setPromptText("Senha inicial");
    CheckBox ativoCheck = new CheckBox("Ativo");
    ativoCheck.setSelected(true);
    CheckBox adminPapel = new CheckBox("ADMIN");
    CheckBox sesmtPapel = new CheckBox("SESMT");
    CheckBox almoxarifePapel = new CheckBox("ALMOXARIFE");
    CheckBox consultaPapel = new CheckBox("CONSULTA");
    sesmtPapel.setSelected(true);
    Label feedback = new Label();
    Label policyHint =
        new Label(
            "Politica de credencial: minimo 12 caracteres, 3 de 4 grupos (A-Z, a-z, 0-9, especial), sem login e sem senha trivial.");
    policyHint.setWrapText(true);
    policyHint.setStyle("-fx-text-fill: #4b5563;");

    GridPane grid = new GridPane();
    grid.setHgap(8);
    grid.setVgap(8);
    grid.addRow(0, new Label("Nome"), nomeField);
    grid.addRow(1, new Label("Login"), loginField);
    grid.addRow(2, new Label("Senha inicial"), senhaField);
    FlowPane papeisPane =
        new FlowPane(8, 8, adminPapel, sesmtPapel, almoxarifePapel, consultaPapel);
    grid.addRow(3, new Label("Papeis iniciais"), papeisPane);
    grid.add(ativoCheck, 1, 4);

    Button cadastrarButton = new Button("Cadastrar");
    Button limparButton = new Button("Limpar");
    limparButton.setOnAction(
        event -> {
          limpar(nomeField, loginField, senhaField);
          ativoCheck.setSelected(true);
          adminPapel.setSelected(false);
          sesmtPapel.setSelected(true);
          almoxarifePapel.setSelected(false);
          consultaPapel.setSelected(false);
          feedback.setText("");
        });
    cadastrarButton.setOnAction(
        event -> {
          try {
            Set<Papel> papeisSelecionados = EnumSet.noneOf(Papel.class);
            if (adminPapel.isSelected()) {
              papeisSelecionados.add(Papel.ADMIN);
            }
            if (sesmtPapel.isSelected()) {
              papeisSelecionados.add(Papel.SESMT);
            }
            if (almoxarifePapel.isSelected()) {
              papeisSelecionados.add(Papel.ALMOXARIFE);
            }
            if (consultaPapel.isSelected()) {
              papeisSelecionados.add(Papel.CONSULTA);
            }
            userAdministrationService.cadastrarUsuario(
                admin.id(),
                nomeField.getText(),
                loginField.getText(),
                senhaField.getText(),
                ativoCheck.isSelected(),
                papeisSelecionados);
            mostrarSucesso(feedback, "AUTH-100 Usuario cadastrado com sucesso.");
            limpar(nomeField, loginField, senhaField);
          } catch (WeakPasswordException
              | DuplicateLoginException
              | AuthorizationDeniedException ex) {
            mostrarErro(feedback, ex.getMessage());
          } catch (IllegalArgumentException ex) {
            mostrarErro(feedback, ex.getMessage());
          } catch (RuntimeException ex) {
            mostrarErro(feedback, "AUTH-099 Falha ao cadastrar usuario.");
          }
        });

    HBox acoes = new HBox(10, cadastrarButton, limparButton, feedback);
    acoes.setAlignment(Pos.CENTER_LEFT);
    return new VBox(10, new Label("Cadastro de usuario"), grid, policyHint, acoes);
  }

  private VBox buildAtribuicaoPapelBox() {
    ObservableList<UsuarioAdminResumo> usuarios =
        FXCollections.observableArrayList(userAdministrationService.listarUsuarios());
    ComboBox<UsuarioAdminResumo> usuarioCombo = new ComboBox<>(usuarios);
    usuarioCombo.setPrefWidth(320);
    usuarioCombo.setConverter(usuarioConverter());
    if (!usuarios.isEmpty()) {
      usuarioCombo.getSelectionModel().selectFirst();
    }

    Label ajuda =
        new Label("Use as listas para adicionar ou remover papeis (unitario ou em lote).");
    ajuda.setWrapText(true);

    ListView<Papel> disponiveisList = new ListView<>();
    ListView<Papel> atribuidosList = new ListView<>();
    disponiveisList.setPrefHeight(180);
    atribuidosList.setPrefHeight(180);
    disponiveisList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    atribuidosList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    atualizarListasPapeis(usuarioCombo.getValue(), disponiveisList, atribuidosList);
    usuarioCombo
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) ->
                atualizarListasPapeis(newVal, disponiveisList, atribuidosList));
    Label feedback = new Label();

    Button adicionarSelecionadosButton = new Button("Adicionar selecionados >");
    adicionarSelecionadosButton.setOnAction(
        event -> {
          try {
            UsuarioAdminResumo usuario = usuarioCombo.getValue();
            if (usuario == null) {
              throw new IllegalArgumentException("AUTH-011 Login alvo obrigatorio.");
            }
            List<Papel> selecionados =
                List.copyOf(disponiveisList.getSelectionModel().getSelectedItems());
            if (selecionados.isEmpty()) {
              throw new IllegalArgumentException(
                  "AUTH-018 Selecione ao menos um papel para adicionar.");
            }
            for (Papel papel : selecionados) {
              userAdministrationService.atribuirPapelPorLogin(admin.id(), usuario.login(), papel);
            }
            mostrarSucesso(feedback, "AUTH-101 Papel(is) atribuido(s) com sucesso.");
            recarregarUsuariosNoCombo(usuarioCombo);
            atualizarListasPapeis(usuarioCombo.getValue(), disponiveisList, atribuidosList);
          } catch (AuthorizationDeniedException | IllegalArgumentException ex) {
            mostrarErro(feedback, ex.getMessage());
          } catch (RuntimeException ex) {
            mostrarErro(feedback, "AUTH-099 Falha ao atribuir papel.");
          }
        });

    Button adicionarTodosButton = new Button("Adicionar todos >>");
    adicionarTodosButton.setOnAction(
        event -> {
          try {
            UsuarioAdminResumo usuario = usuarioCombo.getValue();
            if (usuario == null) {
              throw new IllegalArgumentException("AUTH-011 Login alvo obrigatorio.");
            }
            List<Papel> todos = List.copyOf(disponiveisList.getItems());
            if (todos.isEmpty()) {
              throw new IllegalArgumentException(
                  "AUTH-018 Nao ha papeis disponiveis para adicionar.");
            }
            for (Papel papel : todos) {
              userAdministrationService.atribuirPapelPorLogin(admin.id(), usuario.login(), papel);
            }
            mostrarSucesso(feedback, "AUTH-101 Papel(is) atribuido(s) com sucesso.");
            recarregarUsuariosNoCombo(usuarioCombo);
            atualizarListasPapeis(usuarioCombo.getValue(), disponiveisList, atribuidosList);
          } catch (AuthorizationDeniedException | IllegalArgumentException ex) {
            mostrarErro(feedback, ex.getMessage());
          } catch (RuntimeException ex) {
            mostrarErro(feedback, "AUTH-099 Falha ao atribuir papel.");
          }
        });

    Button removerSelecionadosButton = new Button("< Remover selecionados");
    removerSelecionadosButton.setOnAction(
        event -> {
          try {
            UsuarioAdminResumo usuario = usuarioCombo.getValue();
            if (usuario == null) {
              throw new IllegalArgumentException("AUTH-011 Login alvo obrigatorio.");
            }
            List<Papel> selecionados =
                List.copyOf(atribuidosList.getSelectionModel().getSelectedItems());
            if (selecionados.isEmpty()) {
              throw new IllegalArgumentException(
                  "AUTH-019 Selecione ao menos um papel para remover.");
            }
            if (!confirmarAcao(
                "Remover papeis",
                "Remover papel(is) selecionado(s) de " + usuario.login() + "?",
                "Remover")) {
              return;
            }
            for (Papel papel : selecionados) {
              userAdministrationService.removerPapelPorLogin(admin.id(), usuario.login(), papel);
            }
            mostrarSucesso(feedback, "AUTH-107 Papel(is) removido(s) com sucesso.");
            recarregarUsuariosNoCombo(usuarioCombo);
            atualizarListasPapeis(usuarioCombo.getValue(), disponiveisList, atribuidosList);
          } catch (AuthorizationDeniedException | IllegalArgumentException ex) {
            mostrarErro(feedback, ex.getMessage());
          } catch (RuntimeException ex) {
            mostrarErro(feedback, "AUTH-099 Falha ao remover papel.");
          }
        });

    Button removerTodosButton = new Button("<< Remover todos");
    removerTodosButton.setOnAction(
        event -> {
          try {
            UsuarioAdminResumo usuario = usuarioCombo.getValue();
            if (usuario == null) {
              throw new IllegalArgumentException("AUTH-011 Login alvo obrigatorio.");
            }
            List<Papel> todos = List.copyOf(atribuidosList.getItems());
            if (todos.isEmpty()) {
              throw new IllegalArgumentException("AUTH-019 Nao ha papeis atribuidos para remover.");
            }
            if (!confirmarAcao(
                "Remover todos os papeis",
                "Remover todos os papeis do usuario " + usuario.login() + "?",
                "Remover todos")) {
              return;
            }
            for (Papel papel : todos) {
              userAdministrationService.removerPapelPorLogin(admin.id(), usuario.login(), papel);
            }
            mostrarSucesso(feedback, "AUTH-107 Papel(is) removido(s) com sucesso.");
            recarregarUsuariosNoCombo(usuarioCombo);
            atualizarListasPapeis(usuarioCombo.getValue(), disponiveisList, atribuidosList);
          } catch (AuthorizationDeniedException | IllegalArgumentException ex) {
            mostrarErro(feedback, ex.getMessage());
          } catch (RuntimeException ex) {
            mostrarErro(feedback, "AUTH-099 Falha ao remover papel.");
          }
        });

    VBox controles =
        new VBox(
            8,
            adicionarSelecionadosButton,
            adicionarTodosButton,
            removerSelecionadosButton,
            removerTodosButton);
    controles.setAlignment(Pos.CENTER);

    HBox listas =
        new HBox(
            10,
            new VBox(6, new Label("Papeis nao atribuidos"), disponiveisList),
            controles,
            new VBox(6, new Label("Papeis atribuidos"), atribuidosList));
    HBox.setHgrow(disponiveisList, Priority.ALWAYS);
    HBox.setHgrow(atribuidosList, Priority.ALWAYS);

    HBox acoes = new HBox(10, feedback);
    acoes.setAlignment(Pos.CENTER_LEFT);
    return new VBox(
        10,
        new Label("Atribuicao de papeis"),
        ajuda,
        new Label("Usuario"),
        usuarioCombo,
        listas,
        acoes);
  }

  private VBox buildResetCredencialBox() {
    ObservableList<UsuarioAdminResumo> usuarios =
        FXCollections.observableArrayList(userAdministrationService.listarUsuarios());
    ComboBox<UsuarioAdminResumo> usuarioCombo = new ComboBox<>(usuarios);
    usuarioCombo.setPrefWidth(320);
    usuarioCombo.setConverter(usuarioConverter());
    if (!usuarios.isEmpty()) {
      usuarioCombo.getSelectionModel().selectFirst();
    }
    PasswordField novaSenhaField = new PasswordField();
    novaSenhaField.setPromptText("Nova credencial");
    PasswordField confirmarSenhaField = new PasswordField();
    confirmarSenhaField.setPromptText("Confirmar nova credencial");
    Label alerta =
        new Label(
            "Impacto: a credencial anterior sera invalidada imediatamente e o usuario devera trocar no primeiro login.");
    alerta.setWrapText(true);
    alerta.setStyle("-fx-text-fill: #92400e;");
    Label feedback = new Label();
    Button resetButton = new Button("Resetar");
    resetButton.setOnAction(
        event -> {
          try {
            UsuarioAdminResumo usuario = usuarioCombo.getValue();
            if (usuario == null) {
              throw new IllegalArgumentException("AUTH-011 Login alvo obrigatorio.");
            }
            if (!novaSenhaField.getText().equals(confirmarSenhaField.getText())) {
              throw new IllegalArgumentException(
                  "AUTH-014 As credenciais informadas nao conferem.");
            }
            if (!confirmarAcao(
                "Confirmar reset de credencial",
                "Deseja resetar a credencial do usuario " + usuario.login() + "?",
                "Resetar")) {
              return;
            }
            userAdministrationService.resetarCredencialPorLogin(
                admin.id(), usuario.login(), novaSenhaField.getText());
            mostrarSucesso(feedback, "AUTH-102 Credencial resetada com sucesso.");
            limpar(novaSenhaField, confirmarSenhaField);
          } catch (WeakPasswordException
              | AuthorizationDeniedException
              | IllegalArgumentException ex) {
            mostrarErro(feedback, ex.getMessage());
          } catch (RuntimeException ex) {
            mostrarErro(feedback, "AUTH-099 Falha ao resetar credencial.");
          }
        });
    HBox acoes = new HBox(10, resetButton, feedback);
    acoes.setAlignment(Pos.CENTER_LEFT);
    return new VBox(
        10,
        new Label("Reset de credencial"),
        new Label("Usuario"),
        usuarioCombo,
        new Label("Nova credencial"),
        novaSenhaField,
        new Label("Confirmar nova credencial"),
        confirmarSenhaField,
        alerta,
        acoes);
  }

  private VBox buildGestaoUsuariosBox() {
    ObservableList<UsuarioAdminResumo> usuarios =
        FXCollections.observableArrayList(userAdministrationService.listarUsuarios());
    FilteredList<UsuarioAdminResumo> filtrados = new FilteredList<>(usuarios, usuario -> true);

    TextField filtroField = new TextField();
    filtroField.setPromptText("Filtrar por login ou nome");
    filtroField
        .textProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              String termo = newVal == null ? "" : newVal.trim().toLowerCase(Locale.ROOT);
              filtrados.setPredicate(
                  usuario ->
                      termo.isBlank()
                          || usuario.login().toLowerCase(Locale.ROOT).contains(termo)
                          || usuario.nome().toLowerCase(Locale.ROOT).contains(termo));
            });

    TableView<UsuarioAdminResumo> tabela = new TableView<>(filtrados);
    tabela.setPrefHeight(260);
    tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    TableColumn<UsuarioAdminResumo, Long> idCol = new TableColumn<>("ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

    TableColumn<UsuarioAdminResumo, String> loginCol = new TableColumn<>("Login");
    loginCol.setCellValueFactory(new PropertyValueFactory<>("login"));

    TableColumn<UsuarioAdminResumo, String> nomeCol = new TableColumn<>("Nome");
    nomeCol.setCellValueFactory(new PropertyValueFactory<>("nome"));

    TableColumn<UsuarioAdminResumo, String> statusCol = new TableColumn<>("Status");
    statusCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(cell.getValue().ativo() ? "ATIVO" : "BLOQUEADO"));

    TableColumn<UsuarioAdminResumo, String> trocaCol = new TableColumn<>("Troca obrigatoria");
    trocaCol.setCellValueFactory(
        cell -> new ReadOnlyStringWrapper(cell.getValue().trocaObrigatoria() ? "SIM" : "NAO"));

    TableColumn<UsuarioAdminResumo, String> papeisCol = new TableColumn<>("Papeis");
    papeisCol.setCellValueFactory(new PropertyValueFactory<>("papeis"));

    tabela.getColumns().setAll(idCol, loginCol, nomeCol, statusCol, trocaCol, papeisCol);

    TextField idAlvoField = new TextField();
    idAlvoField.setPromptText("ID");
    idAlvoField.setDisable(true);
    idAlvoField.setEditable(false);
    idAlvoField.setFocusTraversable(false);

    TextField novoNomeField = new TextField();
    novoNomeField.setPromptText("Novo nome");
    TextField novoLoginField = new TextField();
    novoLoginField.setPromptText("Novo login");
    CheckBox ativoCheck = new CheckBox("Ativo");
    Label feedback = new Label();

    tabela
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldValue, selected) -> {
              if (selected != null) {
                idAlvoField.setText(String.valueOf(selected.id()));
                novoNomeField.setText(selected.nome());
                novoLoginField.setText(selected.login());
                ativoCheck.setSelected(selected.ativo());
              }
            });

    Button atualizarButton = new Button("Salvar edicao");
    atualizarButton.setOnAction(
        event -> {
          try {
            UsuarioAdminResumo usuario = tabela.getSelectionModel().getSelectedItem();
            if (usuario == null) {
              throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
            }
            userAdministrationService.editarUsuarioPorLogin(
                admin.id(),
                usuario.login(),
                novoNomeField.getText(),
                novoLoginField.getText(),
                ativoCheck.isSelected());
            mostrarSucesso(feedback, "AUTH-103 Usuario atualizado com sucesso.");
            atualizarListaGestao(usuarios, tabela);
          } catch (RuntimeException ex) {
            mostrarErro(feedback, ex.getMessage());
          }
        });

    Button bloquearButton = new Button("Bloquear");
    bloquearButton.setOnAction(
        event -> {
          try {
            UsuarioAdminResumo usuario = tabela.getSelectionModel().getSelectedItem();
            if (usuario == null) {
              throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
            }
            if (!confirmarAcao(
                "Bloquear usuario",
                "Deseja bloquear o usuario " + usuario.login() + " para autenticacao?",
                "Bloquear")) {
              return;
            }
            userAdministrationService.bloquearUsuarioPorLogin(admin.id(), usuario.login());
            mostrarSucesso(feedback, "AUTH-104 Usuario bloqueado com sucesso.");
            atualizarListaGestao(usuarios, tabela);
          } catch (RuntimeException ex) {
            mostrarErro(feedback, ex.getMessage());
          }
        });

    Button reativarButton = new Button("Reativar");
    reativarButton.setOnAction(
        event -> {
          try {
            UsuarioAdminResumo usuario = tabela.getSelectionModel().getSelectedItem();
            if (usuario == null) {
              throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
            }
            if (!confirmarAcao(
                "Reativar usuario",
                "Deseja reativar o usuario " + usuario.login() + "?",
                "Reativar")) {
              return;
            }
            userAdministrationService.reativarUsuarioPorLogin(admin.id(), usuario.login());
            mostrarSucesso(feedback, "AUTH-105 Usuario reativado com sucesso.");
            atualizarListaGestao(usuarios, tabela);
          } catch (RuntimeException ex) {
            mostrarErro(feedback, ex.getMessage());
          }
        });

    Button excluirButton = new Button("Excluir");
    excluirButton.setOnAction(
        event -> {
          try {
            UsuarioAdminResumo usuario = tabela.getSelectionModel().getSelectedItem();
            if (usuario == null) {
              throw new IllegalArgumentException("AUTH-009 Usuario alvo inexistente.");
            }
            if (!confirmarExclusao(usuario.login())) {
              return;
            }
            userAdministrationService.excluirUsuarioPorLogin(admin.id(), usuario.login());
            mostrarSucesso(feedback, "AUTH-106 Usuario excluido com sucesso.");
            limpar(idAlvoField, novoNomeField, novoLoginField);
            ativoCheck.setSelected(false);
            atualizarListaGestao(usuarios, tabela);
          } catch (RuntimeException ex) {
            mostrarErro(feedback, ex.getMessage());
          }
        });

    HBox acoes =
        new HBox(8, atualizarButton, bloquearButton, reativarButton, excluirButton, feedback);
    acoes.setAlignment(Pos.CENTER_LEFT);
    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);
    VBox box =
        new VBox(
            10,
            new Label("Selecione um usuario para editar, bloquear, reativar ou excluir."),
            filtroField,
            tabela,
            new Label("ID selecionado"),
            idAlvoField,
            new Label("Novo nome"),
            novoNomeField,
            new Label("Novo login"),
            novoLoginField,
            ativoCheck,
            spacer,
            acoes);
    return box;
  }

  private void atualizarListaGestao(
      ObservableList<UsuarioAdminResumo> usuarios, TableView<UsuarioAdminResumo> tabela) {
    UsuarioAdminResumo selecionado = tabela.getSelectionModel().getSelectedItem();
    usuarios.setAll(userAdministrationService.listarUsuarios());
    if (selecionado != null) {
      tabela.getItems().stream()
          .filter(item -> item.id().equals(selecionado.id()))
          .findFirst()
          .ifPresent(item -> tabela.getSelectionModel().select(item));
    }
  }

  private StringConverter<UsuarioAdminResumo> usuarioConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(UsuarioAdminResumo usuario) {
        if (usuario == null) {
          return "";
        }
        return usuario.login() + " - " + usuario.nome();
      }

      @Override
      public UsuarioAdminResumo fromString(String string) {
        return null;
      }
    };
  }

  private void atualizarListasPapeis(
      UsuarioAdminResumo usuario, ListView<Papel> disponiveis, ListView<Papel> atribuidos) {
    EnumSet<Papel> papeisAtribuidos = EnumSet.noneOf(Papel.class);
    if (usuario != null
        && usuario.papeis() != null
        && !usuario.papeis().isBlank()
        && !"-".equals(usuario.papeis())) {
      for (String codigo : usuario.papeis().split(",\\s*")) {
        try {
          papeisAtribuidos.add(Papel.valueOf(codigo.trim()));
        } catch (IllegalArgumentException ignored) {
          // ignora codigos de papel desconhecidos em dados legados
        }
      }
    }
    EnumSet<Papel> papeisDisponiveis = EnumSet.allOf(Papel.class);
    papeisDisponiveis.removeAll(papeisAtribuidos);
    disponiveis.getItems().setAll(papeisDisponiveis);
    atribuidos.getItems().setAll(papeisAtribuidos);
  }

  private void recarregarUsuariosNoCombo(ComboBox<UsuarioAdminResumo> combo) {
    UsuarioAdminResumo atual = combo.getValue();
    List<UsuarioAdminResumo> listaAtualizada = userAdministrationService.listarUsuarios();
    combo.getItems().setAll(listaAtualizada);
    if (atual != null) {
      for (UsuarioAdminResumo item : listaAtualizada) {
        if (item.id().equals(atual.id())) {
          combo.getSelectionModel().select(item);
          return;
        }
      }
    }
    if (!listaAtualizada.isEmpty()) {
      combo.getSelectionModel().selectFirst();
    }
  }

  private void mostrarSucesso(Label feedback, String mensagem) {
    feedback.setText(mensagem);
    feedback.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold;");
    PauseTransition pause = new PauseTransition(Duration.seconds(5));
    pause.setOnFinished(event -> feedback.setText(""));
    pause.play();
  }

  private void mostrarErro(Label feedback, String mensagem) {
    feedback.setText(mensagem);
    feedback.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
  }

  private boolean confirmarAcao(String titulo, String conteudo, String botaoConfirmar) {
    Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
    dialog.setTitle(titulo);
    dialog.setHeaderText(titulo);
    dialog.setContentText(conteudo);
    ButtonType confirmar = new ButtonType(botaoConfirmar);
    dialog.getButtonTypes().setAll(ButtonType.CANCEL, confirmar);
    return dialog.showAndWait().orElse(ButtonType.CANCEL) == confirmar;
  }

  private boolean confirmarExclusao(String login) {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Confirmar exclusao");
    dialog.setHeaderText("Excluir usuario " + login);
    dialog.setContentText("Digite EXCLUIR para confirmar:");
    return dialog
        .showAndWait()
        .map(texto -> "EXCLUIR".equals(texto == null ? "" : texto.trim().toUpperCase(Locale.ROOT)))
        .orElse(false);
  }

  private static void limpar(TextField... fields) {
    for (TextField field : fields) {
      field.clear();
    }
  }
}
