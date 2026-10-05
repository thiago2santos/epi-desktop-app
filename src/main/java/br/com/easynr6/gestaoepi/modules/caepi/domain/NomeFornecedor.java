package br.com.easynr6.gestaoepi.modules.caepi.domain;

import java.text.Normalizer;
import java.util.Locale;

/** Normaliza um nome para comparar com a razão social da base CAEPI. */
public final class NomeFornecedor {

  private NomeFornecedor() {}

  public static String chave(String raw) {
    if (raw == null || raw.isBlank()) {
      return "";
    }
    String semAcento = Normalizer.normalize(raw, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    return semAcento.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", " ").trim();
  }
}
