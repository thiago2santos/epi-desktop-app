package br.com.easynr6.gestaoepi.ui.operacao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.shared.auth.Papel;
import br.com.easynr6.gestaoepi.shared.auth.UsuarioAutenticado;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class HistoricoUxTest {

  @Test
  void shouldNotConsultWhenThePeriodEndsBeforeItStarts() {
    AtomicBoolean consultou = new AtomicBoolean();
    LocalDate inicio = LocalDate.of(2026, 10, 5);
    LocalDate fim = LocalDate.of(2026, 10, 4);

    if (HistoricoPeriodo.valido(inicio, fim)) {
      consultou.set(true);
    }

    assertFalse(consultou.get());
    assertTrue(HistoricoPeriodo.valido(inicio, inicio));
    assertTrue(MensagensHistorico.VAZIO.contains("período"));
    assertTrue(MensagensHistorico.PERIODO.contains("data final"));
  }

  @Test
  void shouldHideReturnAndReversalFromConsulta() {
    UsuarioAutenticado consulta =
        new UsuarioAutenticado(1L, "Consulta", "consulta", Set.of(Papel.CONSULTA));
    UsuarioAutenticado almox =
        new UsuarioAutenticado(2L, "Almox", "almox", Set.of(Papel.ALMOXARIFE));
    UsuarioAutenticado sesmt = new UsuarioAutenticado(3L, "Sesmt", "sesmt", Set.of(Papel.SESMT));

    assertFalse(HistoricoAcoes.devolver(consulta));
    assertFalse(HistoricoAcoes.estornar(consulta));
    assertTrue(HistoricoAcoes.devolver(almox));
    assertFalse(HistoricoAcoes.estornar(almox));
    assertTrue(HistoricoAcoes.estornar(sesmt));
  }
}
