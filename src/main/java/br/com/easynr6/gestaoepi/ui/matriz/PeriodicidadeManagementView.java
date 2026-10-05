package br.com.easynr6.gestaoepi.ui.matriz;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.matrix.application.PeriodicidadeManagementService;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository.Definicao;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.PeriodicidadeRepository.LinhaPeriodicidade;
import br.com.easynr6.gestaoepi.shared.audit.LogTroubleshooting;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.MensagemTemporaria;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** UC-MAT-02. Um prazo por EPI, usado por toda função que o tem na matriz. */
public final class PeriodicidadeManagementView {

  static final String LISTA_VAZIA =
      "Nenhum EPI na matriz. Inclua o EPI na matriz do perfil primeiro.";

  private enum Tom {
    OK,
    ERRO,
    AVISO
  }

  private final UsuarioAutenticado usuario;
  private final PeriodicidadeManagementService periodicidade;
  private final Node root;
  private final VBox linhas = new VBox(8);
  private final Label vazio = new Label(LISTA_VAZIA);
  private final Label feedback = new Label();
  private final Button salvar = new Button("Salvar");
  private final List<Editor> editores = new ArrayList<>();
  private final MensagemTemporaria mensagens = new MensagemTemporaria();

  public PeriodicidadeManagementView(
      UsuarioAutenticado usuario, PeriodicidadeManagementService periodicidade) {
    this.usuario = usuario;
    this.periodicidade = periodicidade;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-MAT-02",
            "Periodicidade",
            "Dias de troca por EPI. Sem número salvo, a cobertura diz Sem prazo e o fornecimento segue.");
    page.section(montar());
    this.root = ReferenciaPage.scroll(page);
    carregar();
  }

  public Node root() {
    return root;
  }

  static boolean gradePronta(List<String> dias, List<String> avisos) {
    if (dias == null || avisos == null || dias.isEmpty() || dias.size() != avisos.size()) {
      return false;
    }
    for (int i = 0; i < dias.size(); i++) {
      if (!linhaPronta(dias.get(i), avisos.get(i))) {
        return false;
      }
    }
    return true;
  }

  static boolean linhaPronta(String dias, String aviso) {
    Integer prazo = inteiro(dias);
    Integer antecedencia = inteiro(aviso);
    return prazo != null
        && prazo > 0
        && antecedencia != null
        && antecedencia >= 0
        && antecedencia < prazo;
  }

  static String usadoEm(int funcoes) {
    return funcoes == 1 ? "Usado em 1 função" : "Usado em " + funcoes + " funções";
  }

  private Node montar() {
    salvar.getStyleClass().add(Styles.ACCENT);
    salvar.setOnAction(event -> salvar());
    feedback.setWrapText(true);
    feedback.setMaxWidth(Double.MAX_VALUE);
    vazio.setWrapText(true);
    VBox corpo = new VBox(12, linhas, vazio, feedback, salvar);
    atualizarAcoes();
    return corpo;
  }

  private void carregar() {
    try {
      montarLinhas(periodicidade.listar(usuario.id()));
    } catch (RuntimeException ex) {
      editores.clear();
      linhas.getChildren().clear();
      LogTroubleshooting.registrar("CARREGAR_PERIODICIDADE", usuario.id(), "-", ex);
      mostrar(MensagensPeriodicidade.erro(ex), Tom.ERRO);
    }
    atualizarAcoes();
  }

  private void montarLinhas(List<LinhaPeriodicidade> itens) {
    editores.clear();
    linhas.getChildren().clear();
    for (LinhaPeriodicidade item : itens) {
      Editor editor = new Editor(item);
      editores.add(editor);
      linhas.getChildren().add(editor.linha());
    }
    vazio.setVisible(itens.isEmpty());
    vazio.setManaged(itens.isEmpty());
    vazio.setText(LISTA_VAZIA);
  }

  private void salvar() {
    if (!gradePronta(textosDias(), textosAvisos())) {
      mostrar(primeiroErro(), Tom.ERRO);
      marcar();
      return;
    }
    try {
      List<Definicao> pedidos = new ArrayList<>();
      for (Editor editor : editores) {
        pedidos.add(
            new Definicao(
                editor.epiId, inteiro(editor.dias.getText()), inteiro(editor.aviso.getText())));
      }
      int gravadas = periodicidade.salvar(usuario.id(), pedidos);
      carregar();
      mostrar(
          gravadas == 0 ? "Nenhuma alteração para salvar." : "Periodicidade salva.",
          gravadas == 0 ? Tom.AVISO : Tom.OK);
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("SALVAR_PERIODICIDADE", usuario.id(), "-", ex);
      mostrar(MensagensPeriodicidade.erro(ex), Tom.ERRO);
    }
  }

  private void atualizarAcoes() {
    salvar.setDisable(!gradePronta(textosDias(), textosAvisos()));
    marcar();
    String erro = primeiroErro();
    if (erro != null && editores.stream().anyMatch(Editor::comecou)) {
      mostrar(erro, Tom.ERRO);
    }
  }

  private void marcar() {
    for (Editor editor : editores) {
      boolean pronta = linhaPronta(editor.dias.getText(), editor.aviso.getText());
      boolean mostrarMarca = editor.comecou() && !pronta;
      Enr6Styles.markFieldInvalid(editor.dias, mostrarMarca && !diasValidos(editor.dias.getText()));
      Enr6Styles.markFieldInvalid(editor.aviso, mostrarMarca && diasValidos(editor.dias.getText()));
    }
  }

  private String primeiroErro() {
    for (Editor editor : editores) {
      if (editor.comecou() && !linhaPronta(editor.dias.getText(), editor.aviso.getText())) {
        String codigo =
            diasValidos(editor.dias.getText())
                ? "MAT-006 O aviso antecipado precisa ser zero ou mais, e menor que a periodicidade."
                : "MAT-005 A periodicidade precisa ser um numero inteiro de dias, maior que zero.";
        return MensagensPeriodicidade.erro(new IllegalArgumentException(codigo));
      }
    }
    return null;
  }

  private List<String> textosDias() {
    return editores.stream().map(editor -> editor.dias.getText()).toList();
  }

  private List<String> textosAvisos() {
    return editores.stream().map(editor -> editor.aviso.getText()).toList();
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

  private static boolean diasValidos(String texto) {
    Integer prazo = inteiro(texto);
    return prazo != null && prazo > 0;
  }

  private static Integer inteiro(String texto) {
    if (texto == null || texto.isBlank()) {
      return null;
    }
    try {
      return Integer.valueOf(texto.trim());
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private final class Editor {
    private final long epiId;
    private final String epi;
    private final int funcoes;
    private final TextField dias = new TextField();
    private final TextField aviso = new TextField();

    private Editor(LinhaPeriodicidade item) {
      this.epiId = item.epiId();
      this.epi = item.epi();
      this.funcoes = item.funcoes();
      dias.setPromptText("Dias");
      aviso.setPromptText("Aviso");
      dias.setText(item.dias() == null ? "" : Integer.toString(item.dias()));
      aviso.setText(item.aviso() == null ? "" : Integer.toString(item.aviso()));
      dias.textProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
      aviso.textProperty().addListener((obs, anterior, atual) -> atualizarAcoes());
    }

    private boolean comecou() {
      return !dias.getText().isBlank() || !aviso.getText().isBlank();
    }

    private Node linha() {
      Label nome = new Label(epi);
      nome.setMinWidth(220);
      Label uso = new Label(usadoEm(funcoes));
      uso.setMinWidth(160);
      Label unidade = new Label("dias");
      HBox.setHgrow(nome, Priority.ALWAYS);
      dias.setPrefWidth(90);
      aviso.setPrefWidth(90);
      HBox linha = new HBox(8, nome, uso, dias, aviso, unidade);
      linha.setAlignment(Pos.CENTER_LEFT);
      return linha;
    }
  }
}
