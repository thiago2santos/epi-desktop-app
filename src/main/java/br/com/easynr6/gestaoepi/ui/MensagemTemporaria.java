package br.com.easynr6.gestaoepi.ui;

import java.util.IdentityHashMap;
import java.util.Map;
import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Some o texto de um rótulo depois de três segundos.
 *
 * <p>Ficam de fora login, troca de senha e faixas de pré-condição: nesses casos a pessoa ainda
 * precisa ler a mensagem para conseguir continuar.
 */
public final class MensagemTemporaria {

  private final Map<Label, PauseTransition> sumicos = new IdentityHashMap<>();

  public void agendar(Label label, String textoExibido) {
    PauseTransition anterior = sumicos.remove(label);
    if (anterior != null) {
      anterior.stop();
    }
    if (textoExibido == null || textoExibido.isBlank()) {
      return;
    }
    PauseTransition pausa = new PauseTransition(Duration.seconds(3));
    pausa.setOnFinished(
        event -> {
          if (textoExibido.equals(label.getText())) {
            label.setText("");
            label
                .getStyleClass()
                .removeAll(
                    Enr6Styles.FEEDBACK_OK, Enr6Styles.FEEDBACK_DANGER, Enr6Styles.FEEDBACK_WARN);
          }
          sumicos.remove(label, pausa);
        });
    sumicos.put(label, pausa);
    pausa.play();
  }
}
