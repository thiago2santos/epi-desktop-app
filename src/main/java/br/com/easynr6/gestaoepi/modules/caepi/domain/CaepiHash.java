package br.com.easynr6.gestaoepi.modules.caepi.domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class CaepiHash {

  private CaepiHash() {}

  public static String sha256(byte[] bytes) {
    try {
      byte[] conteudo = bytes == null ? new byte[0] : bytes;
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(conteudo));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("CAE-004 A carga CAEPI nao foi concluida.", ex);
    }
  }
}
