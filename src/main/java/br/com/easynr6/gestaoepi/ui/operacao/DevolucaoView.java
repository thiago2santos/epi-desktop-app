package br.com.easynr6.gestaoepi.ui.operacao;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.issuance.application.DevolucaoManagementService;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.DevolucaoRepository.ItemPendente;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.DevolucaoRepository.TrabalhadorBusca;
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoDevolucao;
import br.com.easynr6.gestaoepi.shared.audit.LogTroubleshooting;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.time.LocalDate;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** UC-POS-01. A peça devolvida não volta ao disponível. */
public final class DevolucaoView {

  private final UsuarioAutenticado usuario;
  private final DevolucaoManagementService devolucao;
  private final VBox corpo = new VBox(12);
  private final Label feedback = new Label();
  private final Node root;
  private TrabalhadorBusca trabalhador;
  private ItemPendente item;
  private LocalDate data = LocalDate.now();
  private MotivoDevolucao motivo = MotivoDevolucao.DESLIGAMENTO;
  private String motivoTexto = "";

  public DevolucaoView(
      UsuarioAutenticado usuario, DevolucaoManagementService devolucao, Long itemInicial) {
    this.usuario = usuario;
    this.devolucao = devolucao;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-POS-01",
            "Devolução / descarte",
            "Registra a saída da peça com o trabalhador. O saldo da prateleira não muda.");
    page.section(corpo);
    this.root = ReferenciaPage.scroll(page);
    if (itemInicial != null) {
      abrirItem(itemInicial);
    }
    mostrar();
  }

  public Node root() {
    return root;
  }

  private void abrirItem(Long itemInicial) {
    try {
      item = devolucao.exigirPendente(usuario.id(), itemInicial);
      trabalhador =
          new TrabalhadorBusca(item.employeeId(), item.matricula(), item.nome(), true, "", "");
      data = item.fornecimento();
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar(
          "ABRIR_DEVOLUCAO", usuario.id(), String.valueOf(itemInicial), ex);
      avisar(MensagensDevolucao.erro(ex));
    }
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
      List<TrabalhadorBusca> encontrados = devolucao.buscar(usuario.id(), texto);
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
      LogTroubleshooting.registrar("BUSCAR_DEVOLUCAO", usuario.id(), "-", ex);
      avisar(MensagensDevolucao.erro(ex));
    }
  }

  private Node lista() {
    if (trabalhador == null) {
      return new Label("");
    }
    VBox linhas = new VBox(6, new Label(trabalhador.nome() + " · " + trabalhador.matricula()));
    List<ItemPendente> pendentes = devolucao.pendentes(usuario.id(), trabalhador.id());
    if (pendentes.isEmpty()) {
      linhas.getChildren().add(new Label(MensagensDevolucao.VAZIO));
      return linhas;
    }
    for (ItemPendente pendente : pendentes) {
      Button escolher = new Button(rotulo(pendente));
      if (item != null && item.itemId() == pendente.itemId()) {
        escolher.getStyleClass().add(Styles.ACCENT);
      }
      escolher.setOnAction(
          event -> {
            item = pendente;
            data = pendente.fornecimento();
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
    DatePicker dia = new DatePicker(data);
    dia.valueProperty().addListener((obs, anterior, novo) -> data = novo);
    ComboBox<MotivoDevolucao> motivos = new ComboBox<>();
    motivos.getItems().setAll(MotivoDevolucao.values());
    motivos.setConverter(nomes(MotivoDevolucao::rotulo));
    motivos.setValue(motivo);
    TextField outro = new TextField(motivoTexto);
    outro.setPromptText("Descreva o motivo");
    outro.setVisible(motivo == MotivoDevolucao.OUTRO);
    outro.setManaged(outro.isVisible());
    motivos.setOnAction(
        event -> {
          motivo = motivos.getValue();
          boolean visivel = motivo == MotivoDevolucao.OUTRO;
          outro.setVisible(visivel);
          outro.setManaged(visivel);
        });
    outro.textProperty().addListener((obs, anterior, novo) -> motivoTexto = novo);
    Button registrar = new Button("Registrar devolução");
    registrar.getStyleClass().add(Styles.ACCENT);
    registrar.setOnAction(event -> abrirDialogo());
    return new VBox(8, new Label(rotulo(item)), dia, motivos, outro, registrar);
  }

  private void abrirDialogo() {
    ConfirmacaoDevolucao.seguir(dialogo(), this::gravar);
  }

  private void gravar() {
    try {
      devolucao.registrar(usuario.id(), item.itemId(), data, motivo, motivoTexto);
      item = null;
      mostrar();
      avisarOk(MensagensDevolucao.SUCESSO);
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar(
          "REGISTRAR_DEVOLUCAO",
          usuario.id(),
          String.valueOf(item == null ? "-" : item.itemId()),
          ex);
      avisar(MensagensDevolucao.erro(ex));
    }
  }

  private static String rotulo(ItemPendente pendente) {
    return pendente.epi()
        + " · CA "
        + pendente.ca()
        + " · lote "
        + pendente.lote()
        + " · "
        + pendente.fornecimento()
        + " · qtd "
        + pendente.quantidade();
  }

  private static boolean dialogo() {
    javafx.scene.control.Alert alerta =
        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
    alerta.setTitle("Devolução");
    alerta.setHeaderText(MensagensDevolucao.CONFIRMAR);
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

  private static <T> StringConverter<T> nomes(java.util.function.Function<T, String> nome) {
    return new StringConverter<>() {
      @Override
      public String toString(T valor) {
        return valor == null ? "" : nome.apply(valor);
      }

      @Override
      public T fromString(String value) {
        return null;
      }
    };
  }
}
