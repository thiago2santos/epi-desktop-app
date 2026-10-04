package br.com.easynr6.gestaoepi.ui.caepi;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.caepi.application.CaepiCatalogService;
import br.com.easynr6.gestaoepi.modules.caepi.application.CaepiPrevia;
import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog.Tentativa;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.MensagemTemporaria;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/** UC-CAE-01. Mostra o resumo do arquivo e só então publica a base. */
public final class CaepiImportView {

  private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private final UsuarioAutenticado usuario;
  private final CaepiCatalogService caepi;
  private final TableView<Tentativa> tabela = new TableView<>();
  private final Button selecionar = new Button("Selecionar arquivo");
  private final Button importar = new Button("Importar");
  private final Label feedback = new Label();
  private final Label faixa = new Label();
  private final Label previa = new Label("Nenhum arquivo selecionado.");
  private final Node root;
  private final MensagemTemporaria mensagens = new MensagemTemporaria();
  private byte[] arquivoBytes;
  private String arquivoNome;

  public CaepiImportView(UsuarioAutenticado usuario, CaepiCatalogService caepi) {
    this.usuario = usuario;
    this.caepi = caepi;
    ReferenciaPage page =
        ReferenciaPage.of(
            "Governança",
            "Importação CAEPI",
            "Selecione o arquivo, confira o resumo e importe. A carga não trava o restante do sistema.");
    page.section(montar());
    this.root = ReferenciaPage.scroll(page);
    atualizar();
  }

  public Node root() {
    return root;
  }

  private Node montar() {
    tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    tabela.setPlaceholder(new Label("Nenhuma tentativa registrada."));
    tabela.setPrefHeight(360);
    tabela
        .getColumns()
        .setAll(
            coluna("Quando", item -> item.finishedAt().format(DATA)),
            coluna("Resultado", Tentativa::result),
            coluna("Arquivo", Tentativa::sourceName),
            coluna("CAs", item -> Integer.toString(item.recordCount())),
            coluna(
                "Motivo",
                item ->
                    item.failureReason() == null
                        ? ""
                        : MensagensCaepi.erro(new IllegalArgumentException(item.failureReason()))));
    selecionar.setOnAction(event -> selecionar());
    importar.getStyleClass().add(Styles.ACCENT);
    importar.setDisable(true);
    importar.setOnAction(event -> importar());
    feedback.setWrapText(true);
    faixa.setWrapText(true);
    previa.setWrapText(true);
    HBox acoes = new HBox(8, selecionar, importar);
    acoes.setAlignment(Pos.CENTER_LEFT);
    return new VBox(12, faixa, acoes, feedback, previa, tabela);
  }

  private void selecionar() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Arquivo CAEPI");
    chooser
        .getExtensionFilters()
        .add(new FileChooser.ExtensionFilter("ZIP, TXT ou CSV", "*.zip", "*.txt", "*.csv"));
    File arquivo =
        chooser.showOpenDialog(
            feedback.getScene() == null ? null : feedback.getScene().getWindow());
    if (arquivo == null) {
      return;
    }
    travar(true);
    feedback.setText("Lendo o arquivo.");
    Enr6Styles.markOk(feedback);
    Task<ArquivoLido> tarefa =
        new Task<>() {
          @Override
          protected ArquivoLido call() throws IOException {
            byte[] bytes = Files.readAllBytes(arquivo.toPath());
            return new ArquivoLido(
                bytes, caepi.inspecionar(usuario.id(), arquivo.getName(), bytes));
          }
        };
    tarefa.setOnSucceeded(
        event -> {
          ArquivoLido lido = tarefa.getValue();
          arquivoBytes = lido.bytes();
          arquivoNome = lido.previa().nome();
          previa.setText(MensagensCaepi.resumo(lido.previa()));
          feedback.setText("Confira o resumo e importe se o arquivo estiver certo.");
          Enr6Styles.markOk(feedback);
          travar(false);
          importar.setDisable(false);
        });
    tarefa.setOnFailed(
        event -> {
          arquivoBytes = null;
          arquivoNome = null;
          importar.setDisable(true);
          previa.setText("Nenhum arquivo selecionado.");
          Throwable falha = tarefa.getException();
          if (falha instanceof IOException) {
            mostrar("Não foi possível ler o arquivo.");
          } else {
            mostrar(MensagensCaepi.erro(comoRuntime(falha)));
          }
          travar(false);
        });
    iniciar(tarefa, "caepi-previa");
  }

  private void importar() {
    byte[] bytes = arquivoBytes;
    String nome = arquivoNome;
    if (bytes == null || nome == null) {
      return;
    }
    travar(true);
    importar.setDisable(true);
    feedback.setText("Importando a base. A tela continua disponível.");
    Enr6Styles.markOk(feedback);
    Task<Void> tarefa =
        new Task<>() {
          @Override
          protected Void call() {
            caepi.importar(usuario.id(), nome, bytes);
            return null;
          }
        };
    tarefa.setOnSucceeded(
        event -> {
          feedback.setText("Base CAEPI atualizada.");
          Enr6Styles.markOk(feedback);
          mensagens.agendar(feedback, feedback.getText());
          atualizar();
          travar(false);
          importar.setDisable(false);
        });
    tarefa.setOnFailed(
        event -> {
          mostrar(MensagensCaepi.erro(comoRuntime(tarefa.getException())));
          atualizar();
          travar(false);
          importar.setDisable(false);
        });
    iniciar(tarefa, "caepi-import");
  }

  private void atualizar() {
    try {
      faixa.setText(caepi.faixaStatus());
      tabela.getItems().setAll(caepi.ultimas(usuario.id()));
    } catch (RuntimeException ex) {
      mostrar(MensagensCaepi.erro(ex));
    }
  }

  private void travar(boolean ocupado) {
    selecionar.setDisable(ocupado);
    if (ocupado) {
      importar.setDisable(true);
    }
  }

  private void mostrar(String texto) {
    feedback.setText(texto);
    Enr6Styles.markDanger(feedback);
    mensagens.agendar(feedback, texto);
  }

  private static void iniciar(Task<?> tarefa, String nome) {
    Thread trabalho = new Thread(tarefa, nome);
    trabalho.setDaemon(true);
    trabalho.start();
  }

  private static RuntimeException comoRuntime(Throwable falha) {
    if (falha instanceof RuntimeException erro) {
      return erro;
    }
    return new IllegalStateException(falha);
  }

  private record ArquivoLido(byte[] bytes, CaepiPrevia previa) {}

  private static TableColumn<Tentativa, String> coluna(
      String titulo, Function<Tentativa, String> valor) {
    TableColumn<Tentativa, String> column = new TableColumn<>(titulo);
    column.setCellValueFactory(data -> new SimpleStringProperty(valor.apply(data.getValue())));
    return column;
  }
}
