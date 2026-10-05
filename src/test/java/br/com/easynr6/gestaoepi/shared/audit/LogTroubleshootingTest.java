package br.com.easynr6.gestaoepi.shared.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class LogTroubleshootingTest {

  private Logger logger;
  private ListAppender<ILoggingEvent> appender;
  private Level nivelAnterior;

  @BeforeEach
  void captura() {
    logger = (Logger) LoggerFactory.getLogger(LogTroubleshooting.class);
    nivelAnterior = logger.getLevel();
    logger.setLevel(Level.WARN);
    appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
  }

  @AfterEach
  void libera() {
    logger.detachAppender(appender);
    logger.setLevel(nivelAnterior);
    Correlacao.fechar(true);
  }

  @Test
  void recusaComCodigoViraWarnSemStack() {
    LogTroubleshooting.registrar(
        "CONSULTAR_LOTES",
        7L,
        null,
        new AuthorizationDeniedException(
            "AUTH-004 Voce nao tem permissao para executar esta acao."));

    ILoggingEvent evento = appender.list.getFirst();
    assertEquals(Level.WARN, evento.getLevel());
    assertTrue(evento.getFormattedMessage().contains("acao=CONSULTAR_LOTES"));
    assertTrue(evento.getFormattedMessage().contains("codigo=AUTH-004"));
    assertTrue(evento.getFormattedMessage().contains("usuarioId=7"));
    assertTrue(evento.getFormattedMessage().contains("alvo=-"));
    assertTrue(evento.getFormattedMessage().contains("correlacao="));
    assertNull(evento.getThrowableProxy());
    assertNull(Correlacao.atual());
  }

  @Test
  void falhaSemCodigoViraErrorComStack() {
    LogTroubleshooting.registrar(
        "CONSULTAR_LOTES", 4L, "12", new IllegalStateException("SQLITE_BUSY"));

    ILoggingEvent evento = appender.list.getFirst();
    assertEquals(Level.ERROR, evento.getLevel());
    assertTrue(evento.getFormattedMessage().contains("acao=CONSULTAR_LOTES"));
    assertTrue(evento.getFormattedMessage().contains("alvo=12"));
    assertEquals("SQLITE_BUSY", evento.getThrowableProxy().getMessage());
    assertNull(Correlacao.atual());
  }
}
