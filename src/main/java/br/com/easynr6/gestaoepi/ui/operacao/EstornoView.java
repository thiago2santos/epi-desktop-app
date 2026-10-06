package br.com.easynr6.gestaoepi.ui.operacao;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.issuance.application.EstornoManagementService;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.EstornoRepository.ItemAberto;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.EstornoRepository.TrabalhadorBusca;
import br.com.easynr6.gestaoepi.shared.audit.LogTroubleshooting;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/** UC-POS-02. A ficha permanece. A física do lote volta a quantidade inteira. */
public final class EstornoView {

  private final UsuarioAutenticado usuario;
  private final EstornoManagementService estorno;
  private final VBox corpo = new VBox(12);
  private final Label feedback = new Label();
  private final Node root;
  private TrabalhadorBusca trabalhador;
  private ItemAberto item;
  private String motivo = "";

  public EstornoView(
      UsuarioAutenticado usuario, EstornoManagementService estorno, Long itemInicial) {
    this.usuario = usuario;
    this.estorno = estorno;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-POS-02",
            "Estorno",
            "Corrige o lançamento sem apagar a ficha. O saldo da prateleira volta.");
    page.section(corpo);
    this.root = ReferenciaPage.scroll(page);
    if (itemInicial != null) {
      abrirItem(itemInicial);
    }
    mostrar();
  }

  private void abrirItem(Long itemInicial) {
    try {
      item = estorno.exigirAberto(usuario.id(), itemInicial);
      trabalhador =
          new TrabalhadorBusca(item.employeeId(), item.matricula(), item.nome(), true, "", "");
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("ABRIR_ESTORNO", usuario.id(), String.valueOf(itemInicial), ex);
      avisar(MensagensEstorno.erro(ex));
    }
  }

  public Node root() {
    return root;
  }

  private void mostrar() {
    feedback.setWrapText(true);
    VBox pagina = new VBox(12, busca(), lista(), formulario(), feedback);
    corpo.getChildren().setAll(pagina);
  }

  private Node busca() {
    TextField campo = new TextField();
    campo.setPromptText("Matrícula ou nome");
    VBox resultados = new VBox(6);
    Button botao = new Button("Buscar");
    botao.getStyleClass().add(Styles.ACCENT);
    botao.setOnAction(event -> buscar(campo.getText(), resultados));
    return new VBox(8, new Label("Buscar trabalhador"), campo, botao, resultados);
  }

  private void buscar(String texto, VBox resultados) {
    try {
      List<TrabalhadorBusca> encontrados = estorno.buscar(usuario.id(), texto);
      resultados.getChildren().clear();
      if (encontrados.isEmpty()) {
        resultados.getChildren().add(new Label("Nenhum trabalhador com essa matrícula ou nome."));
        return;
      }
      for (TrabalhadorBusca candidato : encontrados) {
        Button escolher = new Button(candidato.nome() + " · " + candidato.matricula());
        escolher.setOnAction(
            event -> {
              trabalhador = candidato;
              item = null;
              limparFeedback();
              mostrar();
            });
        resultados.getChildren().add(escolher);
      }
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("BUSCAR_ESTORNO", usuario.id(), "-", ex);
      avisar(MensagensEstorno.erro(ex));
    }
  }

  private Node lista() {
    if (trabalhador == null) {
      return new Label("");
    }
    VBox linhas = new VBox(6, new Label(trabalhador.nome() + " · " + trabalhador.matricula()));
    List<ItemAberto> abertos = estorno.abertos(usuario.id(), trabalhador.id());
    if (abertos.isEmpty()) {
      linhas.getChildren().add(new Label(MensagensEstorno.VAZIO));
      return linhas;
    }
    for (ItemAberto aberto : abertos) {
      Button escolher = new Button(rotulo(aberto));
      if (item != null && item.itemId() == aberto.itemId()) {
        escolher.getStyleClass().add(Styles.ACCENT);
      }
      escolher.setOnAction(
          event -> {
            item = aberto;
            limparFeedback();
            mostrar();
          });
      linhas.getChildren().add(escolher);
    }
    return linhas;
  }

  private Node formulario() {
    if (item == null) {
      return new Label("");
    }
    TextField campo = new TextField(motivo);
    campo.setPromptText("Motivo do estorno");
    campo.textProperty().addListener((obs, anterior, novo) -> motivo = novo);
    Button registrar = new Button("Estornar");
    registrar.getStyleClass().add(Styles.ACCENT);
    registrar.setOnAction(event -> abrirDialogo());
    return new VBox(8, new Label(rotulo(item)), campo, registrar);
  }

  private void abrirDialogo() {
    ConfirmacaoEstorno.seguir(dialogo(), this::gravar);
  }

  private void gravar() {
    try {
      estorno.registrar(usuario.id(), item.itemId(), motivo);
      item = null;
      mostrar();
      avisarOk(MensagensEstorno.SUCESSO);
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar(
          "REGISTRAR_ESTORNO",
          usuario.id(),
          String.valueOf(item == null ? "-" : item.itemId()),
          ex);
      avisar(MensagensEstorno.erro(ex));
    }
  }

  private static String rotulo(ItemAberto aberto) {
    return aberto.epi()
        + " · CA "
        + aberto.ca()
        + " · lote "
        + aberto.lote()
        + " · "
        + aberto.fornecimento()
        + " · qtd "
        + aberto.quantidade();
  }

  private static boolean dialogo() {
    javafx.scene.control.Alert alerta =
        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
    alerta.setTitle("Estorno");
    alerta.setHeaderText(MensagensEstorno.CONFIRMAR);
    ButtonType seguir = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
    ButtonType voltar = new ButtonType("Voltar", ButtonBar.ButtonData.CANCEL_CLOSE);
    alerta.getButtonTypes().setAll(seguir, voltar);
    Button confirmar = (Button) alerta.getDialogPane().lookupButton(seguir);
    confirmar.setDefaultButton(true);
    Button cancelar = (Button) alerta.getDialogPane().lookupButton(voltar);
    cancelar.setCancelButton(true);
    return alerta.showAndWait().filter(resposta -> resposta == seguir).isPresent();
  }

  private void avisar(String texto) {
    feedback.setText(texto);
    feedback.getStyleClass().setAll(Enr6Styles.FEEDBACK_DANGER);
  }

  private void avisarOk(String texto) {
    feedback.setText(texto);
    feedback.getStyleClass().setAll(Enr6Styles.FEEDBACK_OK);
  }

  private void limparFeedback() {
    feedback.setText("");
    feedback.getStyleClass().clear();
  }
}
