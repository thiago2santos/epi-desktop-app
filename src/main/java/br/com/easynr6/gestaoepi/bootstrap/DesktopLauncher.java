package br.com.easynr6.gestaoepi.bootstrap;

import br.com.easynr6.gestaoepi.EasyNr6Application;
import br.com.easynr6.gestaoepi.ui.EasyNr6DesktopApp;
import javafx.application.Application;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public final class DesktopLauncher {

  private DesktopLauncher() {}

  public static void main(String[] args) {
    ConfigurableApplicationContext context =
        new SpringApplicationBuilder(EasyNr6Application.class)
            .web(WebApplicationType.NONE)
            .run(args);
    EasyNr6DesktopApp.setApplicationContext(context);
    Application.launch(EasyNr6DesktopApp.class, args);
  }
}
