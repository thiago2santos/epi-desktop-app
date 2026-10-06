package br.com.easynr6.gestaoepi.ui.matriz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class PeriodicidadeUxTest {

  @Test
  void salvarSoLigaComTodasAsLinhasValidas() {
    assertFalse(PeriodicidadeManagementView.gradePronta(List.of(), List.of()));
    assertFalse(PeriodicidadeManagementView.gradePronta(List.of(""), List.of("15")));
    assertFalse(PeriodicidadeManagementView.gradePronta(List.of("0"), List.of("0")));
    assertFalse(PeriodicidadeManagementView.gradePronta(List.of("180"), List.of("180")));
    assertFalse(PeriodicidadeManagementView.gradePronta(List.of("180", ""), List.of("15", "1")));
    assertTrue(PeriodicidadeManagementView.gradePronta(List.of("180"), List.of("15")));
  }

  @Test
  void mensagensExplicamSemExporOCodigo() {
    String dias =
        MensagensPeriodicidade.erro(
            new IllegalArgumentException(
                "MAT-005 A periodicidade precisa ser um numero inteiro de dias, maior que zero."));
    String aviso =
        MensagensPeriodicidade.erro(
            new IllegalArgumentException(
                "MAT-006 O aviso antecipado precisa ser zero ou mais, e menor que a periodicidade."));

    assertEquals("A periodicidade precisa ser um número inteiro de dias, maior que zero.", dias);
    assertEquals(
        "O aviso antecipado precisa ser zero ou mais, e menor que a periodicidade.", aviso);
    assertFalse(dias.contains("MAT-005"));
    assertFalse(aviso.contains("MAT-006"));
    assertEquals(
        PeriodicidadeManagementView.LISTA_VAZIA,
        "Nenhum EPI na matriz. Inclua o EPI na matriz do perfil primeiro.");
    assertEquals("Usado em 1 função", PeriodicidadeManagementView.usadoEm(1));
    assertEquals("Usado em 2 funções", PeriodicidadeManagementView.usadoEm(2));
  }
}
