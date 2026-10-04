package br.com.easynr6.gestaoepi.ui.caepi;

import br.com.easynr6.gestaoepi.modules.caepi.application.CaepiPrevia;
import br.com.easynr6.gestaoepi.modules.caepi.domain.CaepiParser.Formato;
import br.com.easynr6.gestaoepi.modules.caepi.domain.CaepiParser.Inspecao;
import java.text.NumberFormat;
import java.util.Locale;

/** Texto de tela da carga CAEPI. O código fica no log e na auditoria. */
public final class MensagensCaepi {

  private static final Locale PT = Locale.of("pt", "BR");
  private static final NumberFormat INTEIRO = NumberFormat.getIntegerInstance(PT);

  private MensagensCaepi() {}

  public static String resumo(CaepiPrevia previa) {
    Inspecao inspecao = previa.inspecao();
    return "Arquivo: "
        + previa.nome()
        + "\nTamanho: "
        + tamanho(previa.bytes())
        + "\nSHA-256: "
        + previa.sha256()
        + "\nFormato: "
        + formato(inspecao.formato())
        + "\nLinhas: "
        + INTEIRO.format(inspecao.linhas())
        + "\nCAs distintos: "
        + INTEIRO.format(inspecao.cas())
        + "\nVariantes: "
        + INTEIRO.format(inspecao.variantes())
        + "\nNas linhas: "
        + quantidade(inspecao.ativos(), "vigente", "vigentes")
        + ", "
        + quantidade(inspecao.suspensos(), "suspenso", "suspensos")
        + ", "
        + quantidade(inspecao.cancelados(), "cancelado", "cancelados")
        + ", "
        + quantidade(inspecao.vencidos(), "vencido", "vencidos")
        + desconhecidos(inspecao.desconhecidos());
  }

  private static String desconhecidos(int total) {
    if (total == 0) {
      return "";
    }
    return ", " + quantidade(total, "situação não reconhecida", "situações não reconhecidas");
  }

  private static String quantidade(int total, String um, String varios) {
    return INTEIRO.format(total) + " " + (total == 1 ? um : varios);
  }

  private static String tamanho(int bytes) {
    if (bytes < 1024) {
      return INTEIRO.format(bytes) + " bytes";
    }
    if (bytes < 1024 * 1024) {
      return String.format(PT, "%.1f KB", bytes / 1024.0);
    }
    return String.format(PT, "%.1f MB", bytes / (1024.0 * 1024.0));
  }

  private static String formato(Formato formato) {
    return switch (formato) {
      case ZIP_OFICIAL -> "ZIP oficial (tgg_export_caepi.txt)";
      case ZIP_CSV -> "ZIP com CSV";
      case CSV -> "CSV do portal";
      case TGG -> "Texto TGG";
    };
  }

  public static String erro(RuntimeException erro) {
    String mensagem = erro.getMessage() == null ? "" : erro.getMessage();
    if (mensagem.startsWith("CAE-002")) {
      return "O arquivo recebido não pode ser aberto. A última base completa foi preservada.";
    }
    if (mensagem.startsWith("CAE-003")) {
      return "O formato do arquivo CAEPI não corresponde ao esperado. A carga foi interrompida.";
    }
    if (mensagem.startsWith("CAE-004")) {
      return "A carga CAEPI não foi concluída. Nenhuma carga parcial foi publicada.";
    }
    if (mensagem.startsWith("CAE-006") || mensagem.startsWith("AUTH-004")) {
      return "Seu perfil não permite iniciar a atualização CAEPI.";
    }
    if (mensagem.contains("atualizacao ja esta em andamento")) {
      return "A atualização já está em andamento.";
    }
    return "Não foi possível importar a base CAEPI.";
  }
}
