package br.com.easynr6.gestaoepi;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class EasyNr6Application {

  public static void main(String[] args) {
    new SpringApplicationBuilder(EasyNr6Application.class)
        .web(WebApplicationType.NONE)
        .run(args);
  }
}
