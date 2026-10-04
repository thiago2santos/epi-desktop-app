package br.com.easynr6.gestaoepi.modules.caepi.infra.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Catálogo CAEPI em arquivo próprio. A carga grava só aqui e não segura a auditoria nem o restante
 * do sistema.
 */
@Component
public class CaepiDatabase implements DisposableBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(CaepiDatabase.class);

  private final HikariDataSource pool;
  private final NamedParameterJdbcTemplate jdbc;
  private final TransactionTemplate transacao;

  public CaepiDatabase(@Value("${spring.datasource.url}") String urlPrincipal) {
    HikariConfig config = new HikariConfig();
    config.setPoolName("caepi");
    config.setDriverClassName("org.sqlite.JDBC");
    config.setJdbcUrl(CaepiDataSourceUrl.catalogo(urlPrincipal));
    config.setMaximumPoolSize(4);
    config.setConnectionInitSql("PRAGMA busy_timeout=30000");
    this.pool = new HikariDataSource(config);
    try {
      wal();
      migrar();
    } catch (RuntimeException ex) {
      this.pool.close();
      throw ex;
    }
    this.jdbc = new NamedParameterJdbcTemplate(pool);
    this.transacao = new TransactionTemplate(new DataSourceTransactionManager(pool));
  }

  public NamedParameterJdbcTemplate jdbc() {
    return jdbc;
  }

  public <T> T escrever(Supplier<T> trabalho) {
    return transacao.execute(status -> trabalho.get());
  }

  @Override
  public void destroy() {
    pool.close();
  }

  private void wal() {
    try (Connection connection = pool.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("PRAGMA journal_mode=WAL");
    } catch (SQLException ex) {
      LOGGER.info("Modo WAL indisponivel no catalogo CAEPI: {}", ex.getMessage());
    }
  }

  private void migrar() {
    int aplicadas =
        Flyway.configure()
            .dataSource(pool)
            .baselineOnMigrate(true)
            .locations("classpath:db/caepi")
            .load()
            .migrate()
            .migrationsExecuted;
    LOGGER.info("Migracoes do catalogo CAEPI aplicadas: {}", aplicadas);
  }
}
