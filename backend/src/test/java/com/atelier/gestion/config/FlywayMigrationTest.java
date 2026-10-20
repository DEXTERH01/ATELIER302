package com.atelier.gestion.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class FlywayMigrationTest {
  @Container
  static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")
      .withDatabaseName("atelier_test")
      .withUsername("atelier")
      .withPassword("atelier_test");

  @Test
  void aplicaLasTresMigracionesEnPostgresqlReal() {
    var result = Flyway.configure()
        .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
        .locations("classpath:db/migration")
        .load()
        .migrate();

    assertThat(result.migrationsExecuted).isEqualTo(3);
  }
}
