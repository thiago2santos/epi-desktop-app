package br.com.easynr6.gestaoepi.modules.stock.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Regras de quantidade, custo e leitura do UC-LOT-01. A tela não vê o código LOT. */
public class LotPolicy {

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
      situacao = "Vencido";
    } else if (fisica == 0) {
      situacao = "Esgotado";
    } else {
      situacao = "Vigente";
    }
    return new Saldo(fisica, reservada, disponivel, situacao);
  }

  private static IllegalArgumentException ausente() {
    return new IllegalArgumentException(
        "LOT-001 Unidade, EPI, CA, codigo, validade ou quantidade ausente.");
  }

  public record Saldo(int fisica, int reservada, int disponivel, String situacao) {}
}
