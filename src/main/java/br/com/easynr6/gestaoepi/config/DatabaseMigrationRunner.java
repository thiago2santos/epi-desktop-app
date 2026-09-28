package br.com.easynr6.gestaoepi.config;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseMigrationRunner implements ApplicationRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMigrationRunner.class);

  private final DataSource dataSource;

  public DatabaseMigrationRunner(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void run(ApplicationArguments args) {
    Flyway flyway =
        Flyway.configure()
            .dataSource(dataSource)
            .baselineOnMigrate(true)
            .locations("classpath:db/migration")
            .load();
    int migrations = flyway.migrate().migrationsExecuted;
    LOGGER.info("Migracoes Flyway aplicadas no startup: {}", migrations);
  }
}
