package br.com.easynr6.gestaoepi.modules.employee.domain;

/** Regras de UC-CAD-01. O CNPJ persiste com 14 dígitos; a máscara fica na tela. */
public class UnitPolicy {

  private static final int[] PESOS_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] PESOS_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

  public String normalizeName(String name) {
    return name == null ? "" : name.trim();
  }

  public String requireName(String name) {
    String normalized = normalizeName(name);
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("CAD-041 Nome ou CNPJ ausente.");
    }
    return normalized;
  }

  public String normalizeCnpj(String raw) {
    if (raw == null || raw.isEmpty()) {
      return "";
    }
    StringBuilder digits = new StringBuilder(raw.length());
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      if (c >= '0' && c <= '9') {
        digits.append(c);
      }
    }
    return digits.toString();
  }

  public String requireCnpj(String raw) {
    String digits = normalizeCnpj(raw);
    if (digits.isEmpty()) {
      throw new IllegalArgumentException("CAD-041 Nome ou CNPJ ausente.");
    }
    if (digits.length() != 14 || !digitosVerificadoresValidos(digits)) {
      throw new IllegalArgumentException("CAD-042 CNPJ invalido.");
    }
    return digits;
  }

  public void assertCanDeactivate(boolean hasActiveDepartments) {
    if (hasActiveDepartments) {
      throw new IllegalArgumentException(
          "CAD-045 Inativacao recusada: a unidade ainda tem setor ativo.");
    }
  }

  public static String formatarCnpj(String cnpj) {
    if (cnpj == null || cnpj.length() != 14) {
      return "";
    }
    return cnpj.substring(0, 2)
        + "."
        + cnpj.substring(2, 5)
        + "."
        + cnpj.substring(5, 8)
        + "/"
        + cnpj.substring(8, 12)
        + "-"
        + cnpj.substring(12);
  }

  private static boolean digitosVerificadoresValidos(String digits) {
    int primeiro = digito(digits.substring(0, 12), PESOS_PRIMEIRO);
    int segundo = digito(digits.substring(0, 12) + primeiro, PESOS_SEGUNDO);
    return digits.charAt(12) == Character.forDigit(primeiro, 10)
        && digits.charAt(13) == Character.forDigit(segundo, 10);
  }

  private static int digito(String base, int[] pesos) {
    int soma = 0;
    for (int i = 0; i < pesos.length; i++) {
      soma += (base.charAt(i) - '0') * pesos[i];
    }
    int resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  }
}
