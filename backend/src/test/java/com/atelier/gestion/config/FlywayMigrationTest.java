package com.atelier.gestion.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FlywayMigrationTest {
  @Container
  static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")
      .withDatabaseName("atelier_test")
      .withUsername("atelier")
      .withPassword("atelier_test");

  @Test @Order(1)
  void aplicaTodasLasMigracionesEnPostgresqlReal() {
    var result = Flyway.configure()
        .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
        .locations("classpath:db/migration")
        .load()
        .migrate();

    assertThat(result.migrationsExecuted).isEqualTo(9);
  }

  @Test @Order(2)
  void correlativoEsAtomicoBajoConcurrencia() throws Exception {
    Flyway.configure().dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
        .locations("classpath:db/migration").load().migrate();
    int solicitudes = 20;
    var executor = java.util.concurrent.Executors.newFixedThreadPool(8);
    try {
      var tareas = java.util.stream.IntStream.range(0, solicitudes).mapToObj(i ->
          (java.util.concurrent.Callable<Integer>) () -> {
            try (var connection = java.sql.DriverManager.getConnection(postgres.getJdbcUrl(),
                postgres.getUsername(), postgres.getPassword());
                var statement = connection.prepareStatement("""
                    insert into correlativos_proyecto(anio,ultimo_numero,actualizado_en)
                    values (2099,1,now()) on conflict (anio) do update
                    set ultimo_numero=correlativos_proyecto.ultimo_numero+1,actualizado_en=now()
                    returning ultimo_numero
                    """)) {
              try (var rs = statement.executeQuery()) { rs.next(); return rs.getInt(1); }
            }
          }).toList();
      var numeros = new java.util.TreeSet<Integer>();
      for (var resultado : executor.invokeAll(tareas)) numeros.add(resultado.get());
      assertThat(numeros).containsExactlyElementsOf(
          java.util.stream.IntStream.rangeClosed(1, solicitudes).boxed().toList());
    } finally {
      executor.shutdownNow();
    }
  }
}
