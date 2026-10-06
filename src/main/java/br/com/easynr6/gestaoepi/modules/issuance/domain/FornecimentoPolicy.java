package br.com.easynr6.gestaoepi.modules.issuance.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Regras da ficha. A tela não mostra o código ENT. */
public class FornecimentoPolicy {

  public static final String TERMO_VERSAO = "TERMO-NR6-01";
  public static final String TERMO_METODO = "ASSINATURA_MANUAL";
  public static final int EXCECAO_MINIMA = 10;

  public void exigirItens(int quantidade) {
    if (quantidade < 1) {
      throw new IllegalArgumentException("ENT-002 Inclua ao menos um EPI na ficha.");
    }
  }

  public int quantidade(int quantidade) {
    if (quantidade <= 0) {
      throw new IllegalArgumentException(
          "ENT-003 A quantidade precisa ser um numero inteiro maior que zero.");
    }
    return quantidade;
  }

  public void motivo(MotivoFornecimento motivo, String texto) {
    if (motivo == null || (motivo == MotivoFornecimento.OUTRO && emBranco(texto))) {
      throw new IllegalArgumentException("ENT-010 Escolha o motivo. Se for outro, descreva.");
    }
  }

  public void lote(boolean encontrado, boolean mesmoEpi, boolean mesmaUnidade, boolean vencido) {
    if (!encontrado || !mesmoEpi || !mesmaUnidade || vencido) {
      throw new IllegalArgumentException(
          "ENT-004 Escolha um lote vigente deste EPI. Peca vencida nao pode ser fornecida.");
    }
  }

  public void disponivel(int pedido, int disponivel) {
    if (pedido > disponivel) {
      throw new IllegalArgumentException("ENT-005 Nao ha quantidade disponivel neste lote.");
    }
  }

  public String excecao(boolean naMatriz, boolean podeExcecao, String texto) {
    if (naMatriz) {
      return null;
    }
    if (!podeExcecao) {
      throw new IllegalArgumentException(
          "ENT-006 Este EPI nao esta na matriz vigente. So o SESMT pode registrar a excecao.");
    }
    String limpo = texto == null ? "" : texto.trim();
    if (limpo.length() < EXCECAO_MINIMA) {
      throw new IllegalArgumentException(
          "ENT-007 Descreva a excecao com pelo menos 10 caracteres.");
    }
    return limpo;
  }

  public void ciencia(
      boolean orientado, boolean exigeTreinamento, LocalDate treinamento, LocalDate ficha) {
    if (!orientado || treinamentoInvalido(exigeTreinamento, treinamento, ficha)) {
      throw new IllegalArgumentException(
          "ENT-008 Registre a orientacao de uso. Se a matriz exige treinamento, informe a data.");
    }
  }

  public void termo(boolean aceito) {
    if (!aceito) {
      throw new IllegalArgumentException(
          "ENT-009 O fornecimento so conclui com o aceite do termo.");
    }
  }

  public void lotesUnicos(List<Long> loteIds) {
    if (loteIds == null || new LinkedHashSet<>(loteIds).size() != loteIds.size()) {
      throw new IllegalArgumentException(
          "ENT-012 Este lote ja esta nesta ficha. Ajuste a quantidade da linha.");
    }
  }

  public void reserva(Long reservaId) {
    if (reservaId != null) {
      throw new IllegalArgumentException(
          "ENT-011 A reserva nao e deste lote ou nao cobre a quantidade.");
    }
  }

  public void pedido(Long pedidoId) {
    if (pedidoId != null) {
      throw new IllegalArgumentException(
          "ENT-013 O pedido nao cobre este trabalhador, EPI ou quantidade.");
    }
  }

  public List<String> cas(String caDoLote, List<String> adicionais, Set<String> ativos) {
    if (caDoLote == null || caDoLote.isBlank() || ativos == null || !ativos.contains(caDoLote)) {
      throw new IllegalArgumentException(
          "ENT-004 Escolha um lote vigente deste EPI. Peca vencida nao pode ser fornecida.");
    }
    LinkedHashSet<String> cas = new LinkedHashSet<>();
    cas.add(caDoLote);
    if (adicionais != null) {
      for (String extra : adicionais) {
        if (extra != null && !extra.isBlank() && !extra.equals(caDoLote)) {
          if (!ativos.contains(extra)) {
            throw new IllegalArgumentException(
                "ENT-004 Escolha um lote vigente deste EPI. Peca vencida nao pode ser fornecida.");
          }
          cas.add(extra);
        }
      }
    }
    return new ArrayList<>(cas);
  }

  public static String textoDoTermo(String nome) {
    String trabalhador = nome == null || nome.isBlank() ? "" : nome.trim();
    return "Eu, "
        + trabalhador
        + ", declaro que recebi gratuitamente os EPIs constantes nesta ficha, adequados ao risco"
        + " da minha atividade e em perfeito estado de conservacao. Comprometo-me a utiliza-los"
        + " estritamente para a finalidade a que se destinam, responsabilizando-me por sua guarda"
        + " e conservacao, e a comunicar imediatamente a empresa qualquer dano, extravio ou"
        + " alteracao que os torne improprios para uso, conforme determina a NR-6.";
  }

  private static boolean treinamentoInvalido(
      boolean exigeTreinamento, LocalDate treinamento, LocalDate ficha) {
    return exigeTreinamento && (treinamento == null || ficha == null || treinamento.isAfter(ficha));
  }

  private static boolean emBranco(String texto) {
    return texto == null || texto.isBlank();
  }
}
