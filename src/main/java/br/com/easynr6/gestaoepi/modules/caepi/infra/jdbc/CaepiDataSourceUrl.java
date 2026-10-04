package br.com.easynr6.gestaoepi.modules.caepi.infra.jdbc;

/** Arquivo SQLite só do catálogo, ao lado do banco principal. */
public final class CaepiDataSourceUrl {

  private CaepiDataSourceUrl() {}

  public static String catalogo(String principal) {
    String fallback = "jdbc:sqlite:file:caepi-catalogo?mode=memory&cache=shared";
    String prefix = "jdbc:sqlite:";
    if (principal == null || !principal.regionMatches(true, 0, prefix, 0, prefix.length())) {
      return fallback;
    }
    String rest = principal.substring(prefix.length());
    if (rest.startsWith(":memory:")) {
      return fallback;
    }
    int consulta = rest.indexOf('?');
    String caminho = consulta < 0 ? rest : rest.substring(0, consulta);
    String query = consulta < 0 ? "" : rest.substring(consulta);
    if (caminho.startsWith("file:")) {
      return prefix + "file:" + caminho.substring("file:".length()) + "-caepi" + query;
    }
    int barra = Math.max(caminho.lastIndexOf('/'), caminho.lastIndexOf('\\'));
    int ponto = caminho.lastIndexOf('.');
    String catalogo =
        ponto > barra
            ? caminho.substring(0, ponto) + "-caepi" + caminho.substring(ponto)
            : caminho + "-caepi.db";
    return prefix + catalogo + query;
  }
}
