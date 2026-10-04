package br.com.easynr6.gestaoepi.modules.caepi.infra.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CaepiDataSourceUrlTest {

  @Test
  void arquivoAoLadoDoBancoPrincipal() {
    assertEquals(
        "jdbc:sqlite:easynr6-dev-caepi.db",
        CaepiDataSourceUrl.catalogo("jdbc:sqlite:easynr6-dev.db"));
  }

  @Test
  void memoriaCompartilhadaGanhaSufixo() {
    assertEquals(
        "jdbc:sqlite:file:caepi-mem-caepi?mode=memory&cache=shared",
        CaepiDataSourceUrl.catalogo("jdbc:sqlite:file:caepi-mem?mode=memory&cache=shared"));
  }

  @Test
  void memoriaPuraNaoDivideConexao() {
    assertEquals(
        "jdbc:sqlite:file:caepi-catalogo?mode=memory&cache=shared",
        CaepiDataSourceUrl.catalogo("jdbc:sqlite::memory:"));
  }
}
