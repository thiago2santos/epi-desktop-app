package br.com.easynr6.gestaoepi.shared.audit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Lê o código estável no início da mensagem de regra, como CAD-021 ou AUTH-004. */
public final class CodigoRegra {

  private static final Pattern CODIGO = Pattern.compile("^([A-Z]+-\\d+)\\b");

  private CodigoRegra() {}

  public static String extrair(String mensagem) {
    if (mensagem == null || mensagem.isBlank()) {
      return null;
    }
    Matcher encontrado = CODIGO.matcher(mensagem.trim());
    if (!encontrado.find()) {
      return null;
    }
    return encontrado.group(1);
  }

  public static String semCodigo(String mensagem) {
    if (mensagem == null) {
      return "";
    }
    return mensagem.trim().replaceFirst("^[A-Z]+-\\d+\\s+", "");
  }
}
