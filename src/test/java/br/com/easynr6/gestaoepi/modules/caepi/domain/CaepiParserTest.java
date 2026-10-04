package br.com.easynr6.gestaoepi.modules.caepi.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CaepiParserTest {

  private final CaepiParser parser = new CaepiParser();

  @Test
  void indiceFicaComASituacaoVigente() {
    String csv =
        """
        NR Registro CA;Situação;Validade;Equipamento;Razão Social
        028941;VENCIDO;01/01/2020;Luva velha;Outra Marca Ltda
        28941;VÁLIDO;15/03/2027;Luva de vaqueta;Vaqueta SA
        """;

    CaepiParser.Resultado resultado =
        parser.parse("RelatorioCA.csv", csv.getBytes(StandardCharsets.UTF_8));

    assertEquals(1, resultado.indice().size());
    CaepiParser.Registro vigente = resultado.indice().get(0);
    assertEquals("28941", vigente.caNumber());
    assertEquals("ACTIVE", vigente.status());
    assertEquals(LocalDate.of(2027, 3, 15), vigente.validUntil());
    assertEquals("Vaqueta SA", vigente.manufacturer());
    assertEquals(2, resultado.variantes().size());
  }

  @Test
  void inspecaoContaLinhasESituacoes() {
    String csv =
        """
        NR Registro CA;Situação;Validade;Equipamento;Razão Social
        028941;VENCIDO;01/01/2020;Luva velha;Outra Marca Ltda
        28941;VÁLIDO;15/03/2027;Luva de vaqueta;Vaqueta SA
        """;

    CaepiParser.Inspecao inspecao =
        parser.inspecionar("RelatorioCA.csv", csv.getBytes(StandardCharsets.UTF_8));

    assertEquals(CaepiParser.Formato.CSV, inspecao.formato());
    assertEquals(3, inspecao.linhas());
    assertEquals(1, inspecao.cas());
    assertEquals(2, inspecao.variantes());
    assertEquals(1, inspecao.ativos());
    assertEquals(1, inspecao.vencidos());
  }

  @Test
  void bomRepetidoNaoEscondeOCabecalho() {
    String csv =
        "\uFEFF\uFEFFNR Registro CA;Situação;Validade;Equipamento;Razão Social\n123;VÁLIDO;01/01/2027;Luva;Marca SA\n";

    CaepiParser.Inspecao inspecao =
        parser.inspecionar("RelatorioCA.csv", csv.getBytes(StandardCharsets.UTF_8));

    assertEquals(1, inspecao.cas());
    assertEquals(1, inspecao.ativos());
  }

  @Test
  void arquivoSemCabecalhoNaoPublica() {
    byte[] lixo = "sem colunas oficiais".getBytes(StandardCharsets.UTF_8);
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> parser.parse("nota.txt", lixo));
    assertTrue(ex.getMessage().startsWith("CAE-003"));
  }
}
