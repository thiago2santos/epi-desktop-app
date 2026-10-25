package br.com.easynr6.gestaoepi.shared.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class EscopoUnidadeTest {

  @Test
  void perfilCorporativoSemVinculoVeTodasAsUnidades() {
    assertTrue(EscopoUnidade.veTodas(Set.of(Papel.ADMIN), null));
    assertTrue(EscopoUnidade.veTodas(Set.of(Papel.SESMT), null));
  }

  @Test
  void vinculoRestringeMesmoOPerfilCorporativo() {
    assertFalse(EscopoUnidade.veTodas(Set.of(Papel.ADMIN), 2L));
    assertFalse(EscopoUnidade.veTodas(Set.of(Papel.SESMT), 4L));
  }

  @Test
  void almoxarifeEConsultaFicamNaUnidadeDoCadastro() {
    assertFalse(EscopoUnidade.veTodas(Set.of(Papel.ALMOXARIFE), null));
    assertFalse(EscopoUnidade.veTodas(Set.of(Papel.CONSULTA), 1L));
    assertTrue(EscopoUnidade.exigeVinculo(Papel.ALMOXARIFE));
    assertTrue(EscopoUnidade.exigeVinculo(Papel.CONSULTA));
    assertFalse(EscopoUnidade.exigeVinculo(Papel.ADMIN));
    assertFalse(EscopoUnidade.exigeVinculo(Papel.SESMT));
  }
}
