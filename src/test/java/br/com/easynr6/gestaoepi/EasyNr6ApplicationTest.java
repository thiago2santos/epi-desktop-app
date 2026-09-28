package br.com.easynr6.gestaoepi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:sqlite::memory:",
    "spring.flyway.enabled=false"
})
class EasyNr6ApplicationTest {

  @Test
  void contextLoads() {
  }
}
