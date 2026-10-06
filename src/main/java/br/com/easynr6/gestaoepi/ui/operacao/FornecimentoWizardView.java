package br.com.easynr6.gestaoepi.ui.operacao;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.issuance.application.FornecimentoManagementService;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.FornecimentoRepository.Trabalhador;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.ConsultarFornecimentoUseCase.LoteVisivel;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarFornecimentoUseCase.ItemParaRegistrar;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarFornecimentoUseCase.Pedido;
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoFornecimento;
import br.com.easynr6.gestaoepi.modules.matrix.application.PeriodicidadeManagementService;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.EpiOpcao;
import br.com.easynr6.gestaoepi.modules.matrix.application.port.MatrizRepository.LinhaMatriz;
import br.com.easynr6.gestaoepi.modules.matrix.application.usecase.LerCoberturaUseCase.LeituraCobertura;
import br.com.easynr6.gestaoepi.shared.audit.LogTroubleshooting;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import br.com.easynr6.gestaoepi.ui.shell.ReferenciaPage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** UC-ENT-01. Cinco passos e um diálogo. Cancelar o diálogo não grava. */
public final class FornecimentoWizardView {

  private static final List<String> PASSOS =
      List.of("Trabalhador", "Itens da função", "Lote e quantidade", "Ciência e termo", "Revisão");

  private final UsuarioAutenticado usuario;
  private final FornecimentoManagementService fornecimento;
  private final PeriodicidadeManagementService periodicidade;
  private final VBox corpo = new VBox(12);
  private final Label feedback = new Label();
  private final List<Rascunho> rascunhos = new ArrayList<>();
  private final Node root;
  private int passo;
  private Trabalhador trabalhador;
  private boolean termoAceito;

  public FornecimentoWizardView(
      UsuarioAutenticado usuario,
      FornecimentoManagementService fornecimento,
      PeriodicidadeManagementService periodicidade) {
    this.usuario = usuario;
    this.fornecimento = fornecimento;
    this.periodicidade = periodicidade;
    ReferenciaPage page =
        ReferenciaPage.of(
            "UC-ENT-01",
            "Registrar fornecimento",
            "Ficha do trabalhador, com lote, ciência e termo na mesma confirmação.");
    page.section(corpo);
    page.legal(MensagensFornecimento.LEGAL);
    this.root = ReferenciaPage.scroll(page);
    mostrar();
  }

  public Node root() {
    return root;
  }

  static Integer quantidade(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    String texto = raw.trim();
    if (texto.chars().anyMatch(caractere -> caractere < '0' || caractere > '9')) {
      return null;
    }
    try {
      int valor = Integer.parseInt(texto);
      return valor > 0 ? valor : null;
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private void mostrar() {
    feedback.setWrapText(true);
    corpo.getChildren().setAll(faixaDePassos(), painelDoTrabalhador(), conteudo(), acoes());
    corpo.getChildren().add(feedback);
  }

  private Node faixaDePassos() {
    HBox faixa = new HBox(8);
    for (int i = 0; i < PASSOS.size(); i++) {
      Label item = new Label((i + 1) + " · " + PASSOS.get(i));
      if (i == passo) {
        item.getStyleClass().add(Enr6Styles.EMPHASIS);
      }
      faixa.getChildren().add(item);
    }
    return faixa;
  }

  private Node painelDoTrabalhador() {
    if (trabalhador == null) {
      return new Label("");
    }
    VBox cartao = new VBox(4, new Label(trabalhador.nome()), new Label(detalhe(trabalhador)));
    cartao.getStyleClass().add(Enr6Styles.PANEL);
    VBox cobertura = new VBox(4, new Label("Cobertura"));
    cobertura.getStyleClass().add(Enr6Styles.PANEL);
    for (LeituraCobertura leitura : periodicidade.cobertura(trabalhador.id())) {
      cobertura.getChildren().add(new Label(leitura.epi() + " — " + leitura.situacao()));
    }
    HBox linha = new HBox(12, cartao, cobertura);
    HBox.setHgrow(cobertura, Priority.ALWAYS);
    return linha;
  }

  private Node conteudo() {
    return switch (passo) {
      case 0 -> passoTrabalhador();
      case 1 -> passoItens();
      case 2 -> passoLotes();
      case 3 -> passoTermo();
      default -> passoRevisao();
    };
  }

  private Node passoTrabalhador() {
    TextField busca = new TextField();
    busca.setPromptText("Matrícula ou nome");
    VBox resultados = new VBox(6);
    Button botao = new Button("Buscar");
    botao.getStyleClass().add(Styles.ACCENT);
    botao.setOnAction(event -> buscar(busca.getText(), resultados));
    return new VBox(8, new Label("Buscar trabalhador ativo"), busca, botao, resultados);
  }

  private void buscar(String texto, VBox resultados) {
    try {
      List<Trabalhador> encontrados = fornecimento.buscar(usuario.id(), texto);
      resultados.getChildren().clear();
      if (encontrados.isEmpty()) {
        resultados.getChildren().add(new Label(MensagensFornecimento.TRABALHADOR_AUSENTE));
        return;
      }
      for (Trabalhador candidato : encontrados) {
        Button escolher = new Button(candidato.nome() + " · " + candidato.matricula());
        escolher.setOnAction(event -> escolher(candidato));
        resultados.getChildren().add(escolher);
      }
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("BUSCAR_TRABALHADOR", usuario.id(), "-", ex);
      avisar(MensagensFornecimento.erro(ex));
    }
  }

  private void escolher(Trabalhador candidato) {
    trabalhador = candidato;
    rascunhos.clear();
    termoAceito = false;
    passo = 1;
    limparFeedback();
    mostrar();
  }

  private Node passoItens() {
    VBox lista = new VBox(8);
    for (LinhaMatriz linha : fornecimento.itensDaFuncao(usuario.id(), trabalhador.id())) {
      CheckBox marca = new CheckBox(linha.epi());
      marca.setSelected(contem(linha.epiId()));
      marca.setOnAction(event -> alternarMatriz(linha, marca.isSelected()));
      lista.getChildren().add(marca);
    }
    boolean podeExcecao = usuario.temPapel(Papel.ADMIN) || usuario.temPapel(Papel.SESMT);
    if (!podeExcecao) {
      Label aviso = new Label("Item fora da matriz fica com o SESMT.");
      aviso.setWrapText(true);
      lista.getChildren().add(aviso);
      return lista;
    }
    ComboBox<EpiOpcao> fora = new ComboBox<>();
    fora.getItems().setAll(fornecimento.episForaDaMatriz(usuario.id(), trabalhador.id()));
    fora.setPromptText("Incluir fora da matriz");
    fora.setConverter(nomes(EpiOpcao::descricao));
    TextField excecao = new TextField();
    excecao.setPromptText("Exceção, pelo menos 10 caracteres");
    Button incluir = new Button("Incluir fora da matriz");
    incluir.setOnAction(event -> incluirFora(fora.getValue(), excecao.getText()));
    lista.getChildren().addAll(fora, excecao, incluir);
    return lista;
  }

  private void alternarMatriz(LinhaMatriz linha, boolean marcado) {
    rascunhos.removeIf(item -> item.epiId == linha.epiId());
    if (marcado) {
      rascunhos.add(Rascunho.daMatriz(linha));
    }
  }

  private void incluirFora(EpiOpcao epi, String excecao) {
    if (epi == null || contem(epi.id())) {
      return;
    }
    rascunhos.add(Rascunho.fora(epi, excecao));
    limparFeedback();
    mostrar();
  }

  private Node passoLotes() {
    VBox lista = new VBox(12);
    if (rascunhos.isEmpty()) {
      lista.getChildren().add(new Label("Inclua ao menos um EPI na ficha."));
      return lista;
    }
    for (Rascunho rascunho : rascunhos) {
      lista.getChildren().add(editorDeLote(rascunho));
    }
    return lista;
  }

  private Node editorDeLote(Rascunho rascunho) {
    List<LoteVisivel> lotes =
        fornecimento.lotes(usuario.id(), trabalhador.unitId(), rascunho.epiId);
    ComboBox<LoteVisivel> lote = new ComboBox<>();
    lote.getItems().setAll(lotes);
    lote.setConverter(nomes(item -> item.codigo() + " · " + item.situacao()));
    lote.setValue(
        lotes.stream().filter(item -> item.id() == rascunho.loteId).findFirst().orElse(null));
    Label saldo = new Label(textoDoLote(lote.getValue()));
    saldo.setWrapText(true);
    lote.setOnAction(
        event -> {
          LoteVisivel escolhido = lote.getValue();
          rascunho.loteId = escolhido == null ? 0 : escolhido.id();
          saldo.setText(textoDoLote(escolhido));
        });
    TextField quantidade = new TextField(rascunho.quantidade);
    quantidade.setPromptText("Quantidade");
    quantidade.textProperty().addListener((obs, antigo, novo) -> rascunho.quantidade = novo);
    ComboBox<MotivoFornecimento> motivo = new ComboBox<>();
    motivo.getItems().setAll(MotivoFornecimento.values());
    motivo.setConverter(nomes(MotivoFornecimento::rotulo));
    motivo.setValue(rascunho.motivo);
    TextField outro = new TextField(rascunho.motivoTexto);
    outro.setPromptText("Descreva o motivo");
    outro.setVisible(rascunho.motivo == MotivoFornecimento.OUTRO);
    outro.setManaged(outro.isVisible());
    motivo.setOnAction(
        event -> {
          rascunho.motivo = motivo.getValue();
          boolean visivel = rascunho.motivo == MotivoFornecimento.OUTRO;
          outro.setVisible(visivel);
          outro.setManaged(visivel);
        });
    outro.textProperty().addListener((obs, antigo, novo) -> rascunho.motivoTexto = novo);
    return new VBox(6, new Label(rascunho.epi), lote, saldo, quantidade, motivo, outro);
  }

  private Node passoTermo() {
    VBox lista = new VBox(8);
    for (Rascunho rascunho : rascunhos) {
      CheckBox ciencia = new CheckBox("Orientação de uso registrada — " + rascunho.epi);
      ciencia.setSelected(rascunho.ciencia);
      ciencia.setOnAction(event -> rascunho.ciencia = ciencia.isSelected());
      lista.getChildren().add(ciencia);
      if (rascunho.exigeTreinamento) {
        DatePicker data = new DatePicker(rascunho.treinamento);
        data.setPromptText("Data do treinamento");
        data.valueProperty().addListener((obs, antigo, novo) -> rascunho.treinamento = novo);
        lista.getChildren().add(data);
      }
    }
    Label termo = new Label(fornecimento.textoDoTermo(trabalhador.nome()));
    termo.setWrapText(true);
    CheckBox aceite = new CheckBox(MensagensFornecimento.TERMO_CAIXA);
    aceite.setSelected(termoAceito);
    aceite.setOnAction(event -> termoAceito = aceite.isSelected());
    lista.getChildren().addAll(termo, aceite);
    return lista;
  }

  private Node passoRevisao() {
    VBox lista = new VBox(6, new Label(trabalhador.nome() + " · " + trabalhador.matricula()));
    for (Rascunho rascunho : rascunhos) {
      lista.getChildren().add(new Label(rascunho.epi + " · quantidade " + rascunho.quantidade));
    }
    Button confirmar = new Button("Confirmar fornecimento");
    confirmar.getStyleClass().add(Styles.ACCENT);
    confirmar.setDefaultButton(true);
    confirmar.setDisable(!revisaoPronta());
    confirmar.setOnAction(event -> abrirDialogo());
    lista.getChildren().add(confirmar);
    return lista;
  }

  private void abrirDialogo() {
    boolean confirmou = dialogo();
    ConfirmacaoFornecimento.seguir(confirmou, this::gravar);
  }

  private void gravar() {
    try {
      long fichaId = fornecimento.registrar(usuario.id(), pedido());
      avisarOk(MensagensFornecimento.sucesso(fichaId));
      trabalhador = null;
      rascunhos.clear();
      termoAceito = false;
      passo = 0;
      mostrar();
      avisarOk(MensagensFornecimento.sucesso(fichaId));
    } catch (RuntimeException ex) {
      LogTroubleshooting.registrar("REGISTRAR_FORNECIMENTO", usuario.id(), "-", ex);
      avisar(MensagensFornecimento.erro(ex));
    }
  }

  private Pedido pedido() {
    List<ItemParaRegistrar> itens = new ArrayList<>();
    for (Rascunho rascunho : rascunhos) {
      Integer qtd = quantidade(rascunho.quantidade);
      itens.add(
          new ItemParaRegistrar(
              rascunho.epiId,
              rascunho.loteId == 0 ? null : rascunho.loteId,
              qtd == null ? 0 : qtd,
              rascunho.motivo,
              rascunho.motivoTexto,
              rascunho.ciencia,
              rascunho.treinamento,
              rascunho.excecao,
              List.of(),
              null,
              null));
    }
    return new Pedido(trabalhador.id(), termoAceito, itens);
  }

  private Node acoes() {
    Button anterior = new Button("Anterior");
    anterior.setDisable(passo == 0);
    anterior.setOnAction(
        event -> {
          passo = passo - 1;
          limparFeedback();
          mostrar();
        });
    Button proximo = new Button("Próximo");
    proximo.getStyleClass().add(Styles.ACCENT);
    proximo.setDisable(passo >= 4 || !passoPronto());
    proximo.setOnAction(
        event -> {
          passo = passo + 1;
          limparFeedback();
          mostrar();
        });
    HBox linha = new HBox(8, anterior, proximo);
    linha.setAlignment(Pos.CENTER_LEFT);
    return linha;
  }

  private boolean passoPronto() {
    return switch (passo) {
      case 0 -> trabalhador != null;
      case 1 -> !rascunhos.isEmpty();
      case 2 -> rascunhos.stream().allMatch(this::lotePronto);
      case 3 -> rascunhos.stream().allMatch(this::cienciaPronta) && termoAceito;
      default -> false;
    };
  }

  private boolean revisaoPronta() {
    return trabalhador != null
        && !rascunhos.isEmpty()
        && rascunhos.stream().allMatch(item -> lotePronto(item) && cienciaPronta(item))
        && termoAceito;
  }

  private boolean lotePronto(Rascunho rascunho) {
    if (rascunho.loteId == 0
        || rascunho.motivo == null
        || quantidade(rascunho.quantidade) == null) {
      return false;
    }
    if (rascunho.motivo == MotivoFornecimento.OUTRO
        && (rascunho.motivoTexto == null || rascunho.motivoTexto.isBlank())) {
      return false;
    }
    LoteVisivel lote = lote(rascunho);
    return lote != null && !"Vencido".equals(lote.situacao());
  }

  private boolean cienciaPronta(Rascunho rascunho) {
    if (!rascunho.ciencia) {
      return false;
    }
    return !rascunho.exigeTreinamento || rascunho.treinamento != null;
  }

  private LoteVisivel lote(Rascunho rascunho) {
    return fornecimento.lotes(usuario.id(), trabalhador.unitId(), rascunho.epiId).stream()
        .filter(item -> item.id() == rascunho.loteId)
        .findFirst()
        .orElse(null);
  }

  private boolean contem(long epiId) {
    return rascunhos.stream().anyMatch(item -> item.epiId == epiId);
  }

  private static String detalhe(Trabalhador trabalhador) {
    return trabalhador.matricula() + " · " + trabalhador.setor() + " · " + trabalhador.funcao();
  }

  private static String textoDoLote(LoteVisivel lote) {
    if (lote == null) {
      return "Escolha o lote.";
    }
    String frase =
        "Física "
            + lote.fisica()
            + " · Reservada "
            + lote.reservada()
            + " · Disponível "
            + lote.disponivel()
            + " · "
            + lote.situacao()
            + ". CA na compra "
            + lote.ca()
            + ". Validade da peça "
            + lote.validade()
            + ".";
    if ("Vencido".equals(lote.situacao())) {
      return frase + " Escolha um lote vigente deste EPI. Peça vencida não pode ser fornecida.";
    }
    return frase;
  }

  private static boolean dialogo() {
    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
    alerta.setTitle("Confirmar fornecimento");
    alerta.setHeaderText(MensagensFornecimento.CONFIRMAR);
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

  private static <T> javafx.util.StringConverter<T> nomes(
      java.util.function.Function<T, String> nome) {
    return new javafx.util.StringConverter<>() {
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

  private static final class Rascunho {
    private final long epiId;
    private final String epi;
    private final boolean exigeTreinamento;
    private final String excecao;
    private long loteId;
    private String quantidade = "1";
    private MotivoFornecimento motivo = MotivoFornecimento.PRIMEIRA_ENTREGA;
    private String motivoTexto = "";
    private boolean ciencia;
    private LocalDate treinamento;

    private Rascunho(long epiId, String epi, boolean exigeTreinamento, String excecao) {
      this.epiId = epiId;
      this.epi = epi;
      this.exigeTreinamento = exigeTreinamento;
      this.excecao = excecao;
    }

    private static Rascunho daMatriz(LinhaMatriz linha) {
      return new Rascunho(linha.epiId(), linha.epi(), linha.exigeTreinamento(), null);
    }

    private static Rascunho fora(EpiOpcao epi, String excecao) {
      return new Rascunho(epi.id(), epi.descricao(), false, excecao);
    }
  }
}
