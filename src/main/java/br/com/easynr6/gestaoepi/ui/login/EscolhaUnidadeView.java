package br.com.easynr6.gestaoepi.ui.login;

import atlantafx.base.theme.Styles;
import br.com.easynr6.gestaoepi.modules.employee.application.port.OrgStructureRepository.UnitOption;
import br.com.easynr6.gestaoepi.modules.employee.domain.UnitPolicy;
import br.com.easynr6.gestaoepi.ui.Enr6Styles;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** Unidades que o perfil autenticado pode gerenciar. */
public class EscolhaUnidadeView extends BorderPane {

  public EscolhaUnidadeView(
      List<UnitOption> unidades, Consumer<UnitOption> aoEscolher, Runnable aoVoltar) {
    setPadding(new Insets(32));
    getStyleClass().add(Enr6Styles.LOGIN_ROOT);

    VBox card = new VBox(14);
    card.setPadding(new Insets(28));
    card.setAlignment(Pos.CENTER_LEFT);
    card.setPrefWidth(420);
    card.setMaxWidth(420);
    card.getStyleClass().add(Enr6Styles.LOGIN_CARD);

    Label titulo = new Label("Unidade");
    titulo.getStyleClass().add(Enr6Styles.LOGIN_TITLE);
    card.getChildren().add(titulo);

    if (unidades == null || unidades.isEmpty()) {
      Label aviso = new Label("Seu usuário não está vinculado a uma unidade.");
      aviso.getStyleClass().add(Enr6Styles.LOGIN_SUBTITLE);
      aviso.setWrapText(true);
      Button voltar = new Button("Voltar ao login");
      voltar.setMaxWidth(Double.MAX_VALUE);
      voltar.setOnAction(event -> aoVoltar.run());
      card.getChildren().addAll(aviso, voltar);
    } else {
      Label subtitulo = new Label("Escolha a unidade que vai gerenciar");
      subtitulo.getStyleClass().add(Enr6Styles.LOGIN_SUBTITLE);
      ComboBox<UnitOption> unidade = new ComboBox<>();
      unidade.getItems().setAll(unidades);
      unidade.setPromptText("Unidade");
      unidade.setMaxWidth(Double.MAX_VALUE);
      unidade.setConverter(rotulo());
      Button continuar = new Button("Continuar");
      continuar.getStyleClass().add(Styles.ACCENT);
      continuar.setDefaultButton(true);
      continuar.setMaxWidth(Double.MAX_VALUE);
      continuar.setDisable(true);
      unidade
          .valueProperty()
          .addListener((obs, anterior, atual) -> continuar.setDisable(atual == null));
      continuar.setOnAction(
          event -> {
            if (unidade.getValue() != null) {
              aoEscolher.accept(unidade.getValue());
            }
          });
      Button voltar = new Button("Voltar ao login");
      voltar.setMaxWidth(Double.MAX_VALUE);
      voltar.setOnAction(event -> aoVoltar.run());
      card.getChildren().addAll(subtitulo, unidade, continuar, voltar);
    }
    setCenter(card);
    setAlignment(card, Pos.CENTER);
  }

  public static String rotulo(UnitOption unidade) {
    return unidade.name() + " · " + UnitPolicy.formatarCnpj(unidade.cnpj());
  }

  private static StringConverter<UnitOption> rotulo() {
    return new StringConverter<>() {
      @Override
      public String toString(UnitOption unidade) {
        return unidade == null ? "" : rotulo(unidade);
      }

      @Override
      public UnitOption fromString(String texto) {
        return null;
      }
    };
  }
}
