package br.com.easynr6.gestaoepi.modules.caepi.domain;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Lê o ZIP oficial, o texto TGG ou o CSV do portal e monta o índice de um CA. */
public class CaepiParser {

  /** O CSV do portal observado em 2026 passa de 150 MB. */
  private static final int LIMITE_BYTES = 400_000_000;

  private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final Map<String, Integer> RANK =
      Map.of("ACTIVE", 4, "SUSPENDED", 3, "CANCELED", 2, "EXPIRED", 1);

  public Resultado parse(String fileName, byte[] content) {
    return abrir(fileName, content).resultado();
  }

  public Inspecao inspecionar(String fileName, byte[] content) {
    Aberto aberto = abrir(fileName, content);
    return new Inspecao(
        aberto.formato(),
        aberto.linhas(),
        aberto.resultado().indice().size(),
        aberto.resultado().variantes().size(),
        contar(aberto.resultado().variantes(), "ACTIVE"),
        contar(aberto.resultado().variantes(), "SUSPENDED"),
        contar(aberto.resultado().variantes(), "CANCELED"),
        contar(aberto.resultado().variantes(), "EXPIRED"),
        contar(aberto.resultado().variantes(), "UNKNOWN"));
  }

  private Aberto abrir(String fileName, byte[] content) {
    if (content == null || content.length == 0) {
      throw new IllegalArgumentException(
          "CAE-003 O formato do arquivo CAEPI nao corresponde ao esperado.");
    }
    if (content.length > LIMITE_BYTES) {
      throw new IllegalArgumentException("CAE-002 O arquivo recebido nao pode ser aberto.");
    }
    String nome = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
    Conteudo conteudo = nome.endsWith(".zip") ? extrairZip(content) : new Conteudo(content, null);
    String texto = decodificar(conteudo.bytes());
    Formato formato = conteudo.formato() == null ? formatoTexto(texto) : conteudo.formato();
    return new Aberto(formato, linhasUteis(texto), interpretar(texto));
  }

  private static String decodificar(byte[] texto) {
    String utf8 = new String(texto, StandardCharsets.UTF_8);
    if (pareceCsv(utf8) || pareceTgg(utf8)) {
      return utf8;
    }
    return new String(texto, Charset.forName("ISO-8859-1"));
  }

  private static Formato formatoTexto(String texto) {
    return pareceCsv(texto) ? Formato.CSV : Formato.TGG;
  }

  private static int linhasUteis(String texto) {
    int total = 0;
    for (String linha : semBom(texto).split("\\R", -1)) {
      if (!linha.isBlank()) {
        total++;
      }
    }
    return total;
  }

  private static int contar(List<Registro> registros, String status) {
    int total = 0;
    for (Registro registro : registros) {
      if (status.equals(registro.status())) {
        total++;
      }
    }
    return total;
  }

  private Resultado interpretar(String texto) {
    String corpo = semBom(texto);
    if (pareceCsv(corpo)) {
      return csv(corpo);
    }
    if (pareceTgg(corpo)) {
      return tgg(corpo);
    }
    throw new IllegalArgumentException(
        "CAE-003 O formato do arquivo CAEPI nao corresponde ao esperado.");
  }

  private static Conteudo extrairZip(byte[] content) {
    try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
      byte[] escolhido = null;
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        String nome = entry.getName() == null ? "" : entry.getName().replace('\\', '/');
        String base = nome.substring(nome.lastIndexOf('/') + 1).toLowerCase(Locale.ROOT);
        boolean oficial = base.equals("tgg_export_caepi.txt");
        boolean csv = base.endsWith(".csv");
        if (!oficial && !csv) {
          continue;
        }
        byte[] lido = zip.readNBytes(LIMITE_BYTES + 1);
        if (lido.length > LIMITE_BYTES) {
          throw new IllegalArgumentException("CAE-002 O arquivo recebido nao pode ser aberto.");
        }
        if (oficial) {
          return new Conteudo(lido, Formato.ZIP_OFICIAL);
        }
        escolhido = lido;
      }
      if (escolhido == null) {
        throw new IllegalArgumentException("CAE-002 O arquivo recebido nao pode ser aberto.");
      }
      return new Conteudo(escolhido, Formato.ZIP_CSV);
    } catch (IOException ex) {
      throw new IllegalArgumentException("CAE-002 O arquivo recebido nao pode ser aberto.");
    }
  }

  private Resultado csv(String texto) {
    List<List<String>> linhas = csvLinhas(texto);
    if (linhas.isEmpty()) {
      throw new IllegalArgumentException(
          "CAE-003 O formato do arquivo CAEPI nao corresponde ao esperado.");
    }
    List<String> header = linhas.get(0).stream().map(CaepiParser::cabecalho).toList();
    int idxCa = indice(header, "REGISTRO CA");
    int idxSit = indice(header, "SITUACAO");
    if (idxCa < 0 || idxSit < 0) {
      throw new IllegalArgumentException(
          "CAE-003 O formato do arquivo CAEPI nao corresponde ao esperado.");
    }
    int idxVal = indice(header, "VALIDADE");
    int idxEq = indiceExato(header, "EQUIPAMENTO");
    int idxFab = indiceRazao(header);
    Map<String, Registro> indice = new LinkedHashMap<>();
    List<Registro> variantes = new ArrayList<>();
    for (int i = 1; i < linhas.size(); i++) {
      List<String> cols = linhas.get(i);
      String ca = numero(celula(cols, idxCa));
      if (ca == null) {
        continue;
      }
      Registro registro =
          new Registro(
              ca,
              situacao(celula(cols, idxSit)),
              data(celula(cols, idxVal)),
              cortar(celula(cols, idxEq), 160),
              cortar(celula(cols, idxFab), 80));
      variantes.add(registro);
      fundir(indice, registro);
    }
    if (indice.isEmpty()) {
      throw new IllegalArgumentException(
          "CAE-003 O formato do arquivo CAEPI nao corresponde ao esperado.");
    }
    return new Resultado(List.copyOf(indice.values()), List.copyOf(variantes));
  }

  private Resultado tgg(String texto) {
    Map<String, Registro> indice = new LinkedHashMap<>();
    List<Registro> variantes = new ArrayList<>();
    String[] linhas = texto.split("\\R");
    for (int i = 0; i < linhas.length; i++) {
      String linha = linhas[i].trim();
      if (linha.isEmpty()) {
        continue;
      }
      if (i == 0 && linha.toLowerCase(Locale.ROOT).matches("^(nr|numero|registro).*")) {
        continue;
      }
      String[] cols = linha.split("\\|", -1);
      if (cols.length < 5) {
        continue;
      }
      String ca = null;
      for (int c = 0; c < Math.min(cols.length, 4); c++) {
        ca = numero(cols[c]);
        if (ca != null) {
          break;
        }
      }
      if (ca == null) {
        continue;
      }
      String situacao = "";
      for (int c = cols.length - 1; c >= 0; c--) {
        String cell = cols[c].trim();
        if (cell.matches("(?i).*(ATIV|CANCEL|SUSP|VENC|VALID).*")) {
          situacao = cell;
          break;
        }
      }
      if (situacao.isEmpty() && cols.length > 6) {
        situacao = cols[6].trim();
      }
      String equipamento = cols.length > 7 ? cols[7] : cols.length > 3 ? cols[3] : "";
      Registro registro = new Registro(ca, situacao(situacao), null, cortar(equipamento, 160), "");
      variantes.add(registro);
      fundir(indice, registro);
    }
    if (indice.isEmpty()) {
      throw new IllegalArgumentException(
          "CAE-003 O formato do arquivo CAEPI nao corresponde ao esperado.");
    }
    return new Resultado(List.copyOf(indice.values()), List.copyOf(variantes));
  }

  private static void fundir(Map<String, Registro> indice, Registro novo) {
    Registro anterior = indice.get(novo.caNumber());
    if (anterior == null || rank(novo.status()) > rank(anterior.status())) {
      indice.put(novo.caNumber(), novo);
      return;
    }
    if (rank(novo.status()) == rank(anterior.status())
        && novo.validUntil() != null
        && (anterior.validUntil() == null || novo.validUntil().isAfter(anterior.validUntil()))) {
      indice.put(novo.caNumber(), novo);
    }
  }

  private static int rank(String status) {
    return RANK.getOrDefault(status, 0);
  }

  static String numero(String raw) {
    if (raw == null) {
      return null;
    }
    StringBuilder digits = new StringBuilder();
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      if (c >= '0' && c <= '9') {
        digits.append(c);
      }
    }
    int inicio = 0;
    while (inicio < digits.length() - 1 && digits.charAt(inicio) == '0') {
      inicio++;
    }
    if (inicio > 0) {
      digits.delete(0, inicio);
    }
    if (digits.isEmpty() || digits.length() > 6) {
      return null;
    }
    return digits.toString();
  }

  static String situacao(String raw) {
    String u = cabecalho(raw == null ? "" : raw);
    if (u.contains("CANCEL")) {
      return "CANCELED";
    }
    if (u.contains("SUSPEND") || u.contains("SUSP")) {
      return "SUSPENDED";
    }
    if (u.contains("VENC") || u.contains("EXPIR")) {
      return "EXPIRED";
    }
    if (u.contains("VALID") || u.contains("ATIV")) {
      return "ACTIVE";
    }
    return "UNKNOWN";
  }

  private static LocalDate data(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return LocalDate.parse(raw.trim(), DATA_BR);
    } catch (DateTimeParseException ex) {
      return null;
    }
  }

  private static List<List<String>> csvLinhas(String texto) {
    List<List<String>> linhas = new ArrayList<>();
    List<String> row = new ArrayList<>();
    StringBuilder cell = new StringBuilder();
    boolean aspas = false;
    String s = semBom(texto);
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (aspas) {
        if (c == '"') {
          if (i + 1 < s.length() && s.charAt(i + 1) == '"') {
            cell.append('"');
            i++;
          } else {
            aspas = false;
          }
        } else {
          cell.append(c);
        }
      } else if (c == '"') {
        aspas = true;
      } else if (c == ';') {
        row.add(cell.toString());
        cell.setLength(0);
      } else if (c == '\n') {
        row.add(cell.toString());
        cell.setLength(0);
        if (row.stream().anyMatch(item -> item != null && !item.isBlank())) {
          linhas.add(row);
        }
        row = new ArrayList<>();
      } else if (c != '\r') {
        cell.append(c);
      }
    }
    if (!cell.isEmpty() || !row.isEmpty()) {
      row.add(cell.toString());
      if (row.stream().anyMatch(item -> item != null && !item.isBlank())) {
        linhas.add(row);
      }
    }
    return linhas;
  }

  private static boolean pareceCsv(String texto) {
    String head =
        semBom(texto).substring(0, Math.min(800, semBom(texto).length())).toUpperCase(Locale.ROOT);
    return head.contains("REGISTRO CA") && head.contains(";");
  }

  private static boolean pareceTgg(String texto) {
    return texto.contains("|");
  }

  private static String semBom(String texto) {
    if (texto == null || texto.isEmpty()) {
      return "";
    }
    int inicio = 0;
    while (inicio < texto.length() && texto.charAt(inicio) == '\uFEFF') {
      inicio++;
    }
    return inicio == 0 ? texto : texto.substring(inicio);
  }

  private static String cabecalho(String valor) {
    String n = java.text.Normalizer.normalize(valor, java.text.Normalizer.Form.NFD);
    return n.replaceAll("\\p{M}", "").trim().toUpperCase(Locale.ROOT);
  }

  private static int indice(List<String> header, String parte) {
    for (int i = 0; i < header.size(); i++) {
      if (header.get(i).contains(parte)) {
        return i;
      }
    }
    return -1;
  }

  private static int indiceExato(List<String> header, String nome) {
    for (int i = 0; i < header.size(); i++) {
      if (header.get(i).equals(nome)) {
        return i;
      }
    }
    return -1;
  }

  private static int indiceRazao(List<String> header) {
    for (int i = 0; i < header.size(); i++) {
      String h = header.get(i);
      if (h.contains("RAZAO SOCIAL") && !h.contains("LABORATORIO")) {
        return i;
      }
    }
    return -1;
  }

  private static String celula(List<String> cols, int index) {
    if (index < 0 || index >= cols.size()) {
      return "";
    }
    return cols.get(index).replace("\"", "").trim();
  }

  private static String cortar(String valor, int max) {
    String texto = valor == null ? "" : valor.trim();
    return texto.length() <= max ? texto : texto.substring(0, max);
  }

  public record Registro(
      String caNumber,
      String status,
      LocalDate validUntil,
      String equipment,
      String manufacturer) {}

  public record Resultado(List<Registro> indice, List<Registro> variantes) {}

  public enum Formato {
    ZIP_OFICIAL,
    ZIP_CSV,
    CSV,
    TGG
  }

  public record Inspecao(
      Formato formato,
      int linhas,
      int cas,
      int variantes,
      int ativos,
      int suspensos,
      int cancelados,
      int vencidos,
      int desconhecidos) {}

  private record Conteudo(byte[] bytes, Formato formato) {}

  private record Aberto(Formato formato, int linhas, Resultado resultado) {}
}
