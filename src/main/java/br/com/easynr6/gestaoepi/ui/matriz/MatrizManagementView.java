package br.com.easynr6.gestaoepi.ui.matriz;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.matrix.application.MatrizManagementService;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.EpiOpcao;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.LinhaMatriz;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.PerfilOpcao;
import br.com.easynr6.gestaoepi.modules.matrix.domain.ModoMatriz;
import br.com.easynr6.gestaoepi.shared.audit.LogTroubleshooting;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.MensagemTemporaria;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** UC-MAT-01. Mantém os EPIs exigidos pelo perfil vigente. */
public final class MatrizManagementView {

  static final String LISTA_VAZIA = "Nenhum EPI na matriz deste perfil.";
  static final String CONFIRMAR_INATIVAR =
      "Tirar este EPI da matriz deste perfil? Fornecimentos já feitos permanecem.";

  private enum Tom {
    OK,
    ERRO,
    AVISO
  }

  private final UsuarioAutenticado usuario;
  private final MatrizManagementService matriz;
  private final Node root;
  private final ComboBox<PerfilOpcao> perfil = new ComboBox<>();
  private final ComboBox<EpiOpcao> epi = new ComboBox<>();
  private final TableView<LinhaMatriz> tabela = new TableView<>();
  private final ComboBox<ModoMatriz> modo = new ComboBox<>();
  private final CheckBox treinamento = new CheckBox("Exige treinamento");
  private final Button incluir = new Button("Incluir na matriz");
  private final Button salvar = new Button("Salvar");
  private final Button atualizarCa = new Button("Atualizar CA esperado");
  private final Button inativar = new Button("Inativar");
  private final Label vazio = new Label(LISTA_VAZIA);
  private final Label feedback = new Label();
  private LinhaMatriz editando;
  private boolean recarregando;
  private final MensagemTemporaria mensagens = new MensagemTemporaria();

  public MatrizManagementView(UsuarioAutenticado usuario, MatrizManagementService matriz) {
    this.usuario = usuario;
    this.matriz = matriz;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-MAT-01",
            "Matriz",
            "EPIs exigidos pela função, ou pelo GHE ativo dela. As duas listas não se somam.");
    page.section(montar());
    this.root = ReferenciaPage.scroll(page);
    carregarPerfis();
  }

  public Node root() {
    return root;
  }

  static boolean matrizProntaParaIncluir(boolean temPerfil, boolean temEpi) {
    return temPerfil && temEpi;
  }

  static boolean segueAposConfirmacao(boolean confirmou) {
    return confirmou;
  }

  private Node montar() {
    configurarTabela();
    perfil.setMaxWidth(Double.MAX_VALUE);
    perfil.setConverter(nomes(PerfilOpcao::rotulo));
    perfil.setPromptText("Função ou GHE");
    perfil.setOnAction(event -> trocarPerfil());
    epi.setMaxWidth(Double.MAX_VALUE);
    epi.setConverter(nomes(EpiOpcao::descricao));
    epi.setPromptText("EPI ativo ainda fora desta matriz");
    epi.valueProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    modo.getItems().setAll(ModoMatriz.values());
    modo.setConverter(nomes(ModoMatriz::rotulo));
    modo.setMaxWidth(Double.MAX_VALUE);
    incluir.getStyleClass().add(Styles.ACCENT);
    incluir.setOnAction(event -> incluir());
    salvar.getStyleClass().add(Styles.ACCENT);
    salvar.setOnAction(event -> salvar());
    atualizarCa.setOnAction(event -> atualizarCa());
    inativar.getStyleClass().add(Styles.DANGER);
    inativar.setOnAction(event -> inativar());
    feedback.setWrapText(true);
    feedback.setMaxWidth(Double.MAX_VALUE);

    HBox inclusao = new HBox(8, epi, incluir);
    HBox.setHgrow(epi, Priority.ALWAYS);
    VBox lista = painel("Perfil", campo("Perfil", perfil), tabela, inclusao);
    HBox.setHgrow(lista, Priority.ALWAYS);
    VBox.setVgrow(tabela, Priority.ALWAYS);
    tabela.setPrefHeight(320);
    VBox formulario =
        painel(
            "Linha",
            campo("Modo", modo),
            treinamento,
            feedback,
            new HBox(8, salvar, atualizarCa, inativar));
    formulario.setPrefWidth(360);
    formulario.setMinWidth(300);
    VBox corpo = new VBox(12, new HBox(12, lista, formulario));
    VBox.setVgrow(lista, Priority.ALWAYS);
    atualizarAcoes();
    return corpo;
  }

  private void configurarTabela() {
    tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabela.setPlaceholder(vazio);
    tabela
        .getColumns()
        .setAll(
            coluna("EPI", LinhaMatriz::epi),
            coluna("Anexo I", linha -> linha.anexo().name()),
            coluna("CA esperado", LinhaMatriz::ca),
            coluna("Modo", linha -> linha.modo().rotulo()),
            coluna("Treinamento", linha -> linha.exigeTreinamento() ? "Sim" : "Não"));
    tabela
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, anterior, atual) -> {
              if (!recarregando) {
                preencher(atual);
              }
            });
  }

  private void carregarPerfis() {
    try {
      perfil.getItems().setAll(matriz.listarPerfis(usuario.id()));
      if (!perfil.getItems().isEmpty()) {
        perfil.getSelectionModel().selectFirst();
      }
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("CARREGAR_MATRIZ", usuario.id(), "-", ex);
      mostrar(MensagensMatriz.erro(ex), Tom.ERRO);
    }
  }

  private void trocarPerfil() {
    editando = null;
    modo.setValue(ModoMatriz.INDIVIDUAL);
    treinamento.setSelected(false);
    carregar();
    carregarCandidatos();
  }

  private void incluir() {
    PerfilOpcao escolhido = perfil.getValue();
    EpiOpcao peca = epi.getValue();
    if (!matrizProntaParaIncluir(escolhido != null, peca != null)) {
      mostrar("Escolha o perfil e o EPI.", Tom.ERRO);
      return;
    }
    try {
      matriz.incluir(usuario.id(), escolhido.tipo(), escolhido.id(), peca.id());
      epi.setValue(null);
      carregar();
      carregarCandidatos();
      mostrar("EPI incluído na matriz.", Tom.OK);
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("INCLUIR_MATRIZ", usuario.id(), alvoPerfil(escolhido), ex);
      mostrar(MensagensMatriz.erro(ex), Tom.ERRO);
    }
  }

  private void salvar() {
    LinhaMatriz atual = editando;
    ModoMatriz escolhido = modo.getValue();
    if (atual == null || escolhido == null) {
      return;
    }
    if (atual.modo() == escolhido && atual.exigeTreinamento() == treinamento.isSelected()) {
      mostrar("Nenhuma alteração para salvar.", Tom.AVISO);
      return;
    }
    try {
      matriz.alterar(usuario.id(), atual.id(), escolhido, treinamento.isSelected());
      carregar();
      religar(atual.id());
      mostrar("Matriz atualizada.", Tom.OK);
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("ALTERAR_MATRIZ", usuario.id(), alvo(atual), ex);
      mostrar(MensagensMatriz.erro(ex), Tom.ERRO);
    }
  }

  private void atualizarCa() {
    LinhaMatriz atual = editando;
    if (atual == null) {
      return;
    }
    try {
      matriz.atualizarCa(usuario.id(), atual.id());
      carregar();
      religar(atual.id());
      mostrar("Matriz atualizada.", Tom.OK);
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("ATUALIZAR_CA_MATRIZ", usuario.id(), alvo(atual), ex);
      mostrar(MensagensMatriz.erro(ex), Tom.ERRO);
    }
  }

  private void inativar() {
    LinhaMatriz atual = editando;
    if (atual == null) {
      return;
    }
    if (!segueAposConfirmacao(confirmar("Inativar linha", CONFIRMAR_INATIVAR))) {
      return;
    }
    try {
      matriz.inativar(usuario.id(), atual.id());
      editando = null;
      carregar();
      carregarCandidatos();
      mostrar("Matriz atualizada.", Tom.OK);
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("INATIVAR_MATRIZ", usuario.id(), alvo(atual), ex);
      mostrar(MensagensMatriz.erro(ex), Tom.ERRO);
    }
  }

  private void carregar() {
    PerfilOpcao escolhido = perfil.getValue();
    if (escolhido == null) {
      tabela.getItems().clear();
      vazio.setText(LISTA_VAZIA);
      return;
    }
    recarregando = true;
    try {
      Long manter = editando == null ? null : editando.id();
      tabela.getItems().setAll(matriz.listarLinhas(usuario.id(), escolhido.tipo(), escolhido.id()));
      if (manter != null) {
        tabela.getItems().stream()
            .filter(item -> item.id().equals(manter))
            .findFirst()
            .ifPresent(item -> tabela.getSelectionModel().select(item));
      }
      vazio.setText(LISTA_VAZIA);
    } catch (RuntimeException ex) {
      tabela.getItems().clear();
      LogTroubleshooting.registrar("CARREGAR_MATRIZ", usuario.id(), alvoPerfil(escolhido), ex);
      mostrar(MensagensMatriz.erro(ex), Tom.ERRO);
    } finally {
      recarregando = false;
    }
    atualizarAcoes();
  }

  private void carregarCandidatos() {
    PerfilOpcao escolhido = perfil.getValue();
    EpiOpcao anterior = epi.getValue();
    try {
      epi.getItems()
          .setAll(
              escolhido == null
                  ? List.of()
                  : matriz.listarCandidatos(usuario.id(), escolhido.tipo(), escolhido.id()));
      if (anterior != null) {
        epi.getItems().stream()
            .filter(item -> item.id().equals(anterior.id()))
            .findFirst()
            .ifPresent(epi.getSelectionModel()::select);
      }
    } catch (RuntimeException ex) {
      epi.getItems().clear();
      LogTroubleshooting.registrar(
          "CARREGAR_MATRIZ", usuario.id(), escolhido == null ? "-" : alvoPerfil(escolhido), ex);
      mostrar(MensagensMatriz.erro(ex), Tom.ERRO);
    }
    atualizarAcoes();
  }

  private void religar(Long linhaId) {
    tabela.getItems().stream()
        .filter(item -> item.id().equals(linhaId))
        .findFirst()
        .ifPresent(item -> tabela.getSelectionModel().select(item));
    if (tabela.getSelectionModel().getSelectedItem() != null) {
      preencher(tabela.getSelectionModel().getSelectedItem());
    }
  }

  private void preencher(LinhaMatriz linha) {
    editando = linha;
    if (linha == null) {
      modo.setValue(ModoMatriz.INDIVIDUAL);
      treinamento.setSelected(false);
    } else {
      modo.setValue(linha.modo());
      treinamento.setSelected(linha.exigeTreinamento());
    }
    atualizarAcoes();
  }

  private void atualizarAcoes() {
    incluir.setDisable(!matrizProntaParaIncluir(perfil.getValue() != null, epi.getValue() != null));
    boolean temLinha = editando != null;
    salvar.setDisable(!temLinha);
    atualizarCa.setDisable(!temLinha);
    inativar.setDisable(!temLinha);
    modo.setDisable(!temLinha);
    treinamento.setDisable(!temLinha);
  }

  private void mostrar(String texto, Tom tom) {
    String visivel = texto == null ? "" : texto;
    feedback.setText(visivel);
    switch (tom) {
      case OK -> Enr6Styles.markOk(feedback);
      case ERRO -> Enr6Styles.markDanger(feedback);
      case AVISO -> Enr6Styles.markWarn(feedback);
    }
    if (visivel.isBlank()) {
      feedback
          .getStyleClass()
          .removeAll(Enr6Styles.FEEDBACK_OK, Enr6Styles.FEEDBACK_DANGER, Enr6Styles.FEEDBACK_WARN);
    }
    mensagens.agendar(feedback, visivel);
  }

  private static String alvo(LinhaMatriz linha) {
    return linha == null ? "-" : String.valueOf(linha.id());
  }

  private static String alvoPerfil(PerfilOpcao opcao) {
    return opcao == null ? "-" : opcao.tipo().name() + ":" + opcao.id();
  }

  private static boolean confirmar(String titulo, String conteudo) {
    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
    alerta.setTitle(titulo);
    alerta.setHeaderText(conteudo);
    ButtonType seguir = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
    alerta.getButtonTypes().setAll(seguir, cancelar);
    Optional<ButtonType> resposta = alerta.showAndWait();
    return resposta.isPresent() && resposta.get() == seguir;
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

  private static TableColumn<LinhaMatriz, String> coluna(
      String titulo, Function<LinhaMatriz, String> valor) {
    TableColumn<LinhaMatriz, String> column = new TableColumn<>(titulo);
    column.setCellValueFactory(data -> new SimpleStringProperty(valor.apply(data.getValue())));
    return column;
  }

  private static VBox campo(String rotulo, Node editor) {
    if (editor instanceof ComboBox<?> combo) {
      combo.setMaxWidth(Double.MAX_VALUE);
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
}
