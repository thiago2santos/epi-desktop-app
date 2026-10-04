package br.com.easynr6.gestaoepi.modules.epi;

import java.util.HexFormat;

/** PNG mínimo para o caminho de consulta online nos testes. */
public final class PrintConsulta {

  private static final byte[] PNG =
      HexFormat.of()
          .parseHex(
              "89504e470d0a1a0a0000000d49484452000000010000000108060000001f15c489"
                  + "0000000a49444154789c63000100000500010d0a2db40000000049454e44ae426082");

  private PrintConsulta() {}

  public static byte[] png() {
    return PNG.clone();
  }

  public static String nome() {
    return "consulta.png";
  }
}
