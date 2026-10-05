package br.com.easynr6.gestaoepi.modules.stock.domain;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Locale;

/** Regras de quantidade, custo e leitura dos UC-LOT-01 e UC-LOT-02. A tela não vê o código LOT. */
public class LotPolicy {

  public static final String SITUACAO_TODAS = "Todas";
  public static final String SITUACAO_VIGENTE = "Vigente";
  public static final String SITUACAO_VENCIDO = "Vencido";
  public static final String SITUACAO_ESGOTADO = "Esgotado";
  public static final String TAMANHO_UNICO = "Único";

  public String codigo(String raw) {
    if (raw == null || raw.isBlank()) {
      throw ausente();
    }
    return raw.trim();
  }

  public String tamanho(String raw) {
    return raw == null ? "" : raw.trim();
  }

  public String fabricante(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    return raw.trim();
  }

  public int quantidade(String raw) {
    if (raw == null || raw.isBlank()) {
      throw ausente();
    }
    String texto = raw.trim();
    if (texto.chars().anyMatch(c -> c < '0' || c > '9')) {
      throw new IllegalArgumentException("LOT-002 Quantidade nao inteira ou menor que um.");
    }
    try {
      int valor = Integer.parseInt(texto);
      if (valor < 1) {
        throw new IllegalArgumentException("LOT-002 Quantidade nao inteira ou menor que um.");
      }
      return valor;
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("LOT-002 Quantidade nao inteira ou menor que um.");
    }
  }

  /** Em branco vira nulo. Zero é custo informado. Negativo recusa. */
  public Integer custoCentavos(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    String normalizado = raw.trim().replace(" ", "").replace(',', '.');
    if (normalizado.startsWith("+")) {
      normalizado = normalizado.substring(1);
    }
    BigDecimal valor;
    try {
      valor = new BigDecimal(normalizado);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("LOT-007 Custo informado negativo.");
    }
    if (valor.signum() < 0) {
      throw new IllegalArgumentException("LOT-007 Custo informado negativo.");
    }
    try {
      return valor.movePointRight(2).intValueExact();
    } catch (ArithmeticException ex) {
      throw new IllegalArgumentException("LOT-007 Custo informado negativo.");
    }
  }

  public boolean vencida(LocalDate validade, LocalDate hoje) {
    return validade != null && hoje != null && validade.isBefore(hoje);
  }

  public Saldo saldo(int fisica, int reservada, LocalDate validade, LocalDate hoje) {
    boolean vencido = vencida(validade, hoje);
    int disponivel = vencido ? 0 : Math.max(0, fisica - reservada);
    String situacao;
    if (vencido) {
      situacao = SITUACAO_VENCIDO;
    } else if (fisica == 0) {
      situacao = SITUACAO_ESGOTADO;
    } else {
      situacao = SITUACAO_VIGENTE;
    }
    return new Saldo(fisica, reservada, disponivel, situacao);
  }

  public String tamanhoVisivel(String tamanho) {
    if (tamanho == null || tamanho.isBlank()) {
      return TAMANHO_UNICO;
    }
    return tamanho.trim();
  }

  /** Filtro da consulta: EPI, código, tamanho visível e situação. Texto em branco não esconde. */
  public boolean apareceNaConsulta(
      String epi,
      String codigo,
      String tamanho,
      String situacao,
      String texto,
      String situacaoFiltro) {
    if (!aceitaSituacao(situacao, situacaoFiltro)) {
      return false;
    }
    if (texto == null || texto.isBlank()) {
      return true;
    }
    String alvo = normalizar(texto);
    return contem(epi, alvo) || contem(codigo, alvo) || contem(tamanhoVisivel(tamanho), alvo);
  }

  private static boolean aceitaSituacao(String situacao, String filtro) {
    if (filtro == null || filtro.isBlank() || SITUACAO_TODAS.equalsIgnoreCase(filtro.trim())) {
      return true;
    }
    return situacao != null && situacao.equalsIgnoreCase(filtro.trim());
  }

  private static boolean contem(String valor, String alvo) {
    return normalizar(valor).contains(alvo);
  }

  private static String normalizar(String valor) {
    if (valor == null || valor.isBlank()) {
      return "";
    }
    String semAcento =
        Normalizer.normalize(valor.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    return semAcento.toLowerCase(Locale.ROOT);
  }

  private static IllegalArgumentException ausente() {
    return new IllegalArgumentException(
        "LOT-001 Unidade, EPI, CA, codigo, validade ou quantidade ausente.");
  }

  public record Saldo(int fisica, int reservada, int disponivel, String situacao) {}
}
