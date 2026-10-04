package br.com.easynr6.gestaoepi.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** WAL deixa a consulta seguir enquanto a carga grava o arquivo SQLite. */
@Component
public class SqlitePragmas {

  private static final Logger LOGGER = LoggerFactory.getLogger(SqlitePragmas.class);

  public SqlitePragmas(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("PRAGMA journal_mode=WAL");
    } catch (SQLException ex) {
      LOGGER.info("Modo WAL indisponivel neste banco: {}", ex.getMessage());
    }
  }
}
