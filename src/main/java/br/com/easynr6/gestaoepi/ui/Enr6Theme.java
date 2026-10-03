package br.com.easynr6.gestaoepi.ui;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.Scene;

/** Tema AtlantaFX (Primer Light) mais a camada semantica Easy NR6. */
public final class Enr6Theme {

  private static final String STYLESHEET = stylesheet();

  private Enr6Theme() {}

  public static void install() {
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
  }

  public static void apply(Scene scene) {
    if (!scene.getStylesheets().contains(STYLESHEET)) {
      scene.getStylesheets().add(STYLESHEET);
    }
  }

  private static String stylesheet() {
    var resource = Enr6Theme.class.getResource("enr6.css");
    if (resource == null) {
      throw new IllegalStateException("Folha enr6.css ausente no classpath");
    }
    return resource.toExternalForm();
  }
}
