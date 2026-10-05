package br.com.easynr6.gestaoepi.modules.caepi.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog.CaPublicado;
import br.com.easynr6.gestaoepi.modules.caepi.application.port.CaepiCatalog.Linha;
import br.com.easynr6.gestaoepi.modules.epi.PrintConsulta;
import br.com.easynr6.gestaoepi.modules.epi.application.EpiCatalogManagementService;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:sqlite:file:caepi-mem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class CaepiCatalogServiceIntegrationTest {

  private static final String CSV =
      """
      NR Registro CA;Situação;Validade;Equipamento;Razão Social
      28941;VENCIDO;01/01/2020;Luva velha;Outra Marca Ltda
      28941;VÁLIDO;15/03/2027;Luva de vaqueta;Vaqueta SA
      """;

  @Autowired private CaepiCatalogService caepi;
  @Autowired private EpiCatalogManagementService catalogo;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void shouldPublishAndBindFromTheOfficialRow() {
    long sesmt = createUserWithRole("sesmt.caepi", Papel.SESMT);
    long cargaId = caepi.importar(sesmt, "RelatorioCA.csv", CSV.getBytes(StandardCharsets.UTF_8));

    CaPublicado publicado = publicado("28941");
    assertEquals("ACTIVE", publicado.status());
    assertEquals(LocalDate.of(2027, 3, 15), publicado.validUntil());
    assertEquals(1, audit(cargaId, "SUCESSO"));
    Linha daMarca =
        caepi.buscar(sesmt, "", "Vaqueta").stream()
            .filter(linha -> "28941".equals(linha.caNumber()))
            .findFirst()
            .orElseThrow();
    assertEquals("ACTIVE", daMarca.status());

    long epiId =
        catalogo.createEpi(sesmt, "LUV-1", "Luva " + System.nanoTime(), AnnexGroup.F, true);
    long bindingId =
        catalogo.bindCaToEpi(
            sesmt,
            epiId,
            "028941",
            CaStatus.SUSPENDED,
            null,
            LocalDate.of(2020, 1, 1),
            LocalDateTime.of(2020, 1, 1, 0, 0),
            "texto que a base substitui",
            true,
            null,
            null);

    assertEquals("28941", numeroOf(bindingId));
    assertEquals("ACTIVE", statusVinculo(bindingId));
    assertEquals("2027-03-15", validadeVinculo(bindingId));
    assertTrue(notaOf(bindingId).startsWith("Carga CAEPI " + cargaId));
  }

  @Test
  void shouldPreserveBaseWhenTheNextFileIsInvalid() {
    long sesmt = createUserWithRole("sesmt.caepi.falha", Papel.SESMT);
    caepi.importar(sesmt, "RelatorioCA.csv", CSV.getBytes(StandardCharsets.UTF_8));

    assertThrows(
        IllegalArgumentException.class,
        () -> caepi.importar(sesmt, "nota.txt", "lixo".getBytes(StandardCharsets.UTF_8)));

    assertEquals(1, caepi.ultimaSucesso().orElseThrow().recordCount());
    long falhas =
        caepi.ultimas(sesmt).stream()
            .filter(tentativa -> "FALHA".equals(tentativa.result()))
            .count();
    assertEquals(1, falhas);
  }

  @Test
  void shouldRequirePrintWhenTheNumberIsOutsideTheBase() {
    long sesmt = createUserWithRole("sesmt.caepi.print", Papel.SESMT);
    caepi.importar(sesmt, "RelatorioCA.csv", CSV.getBytes(StandardCharsets.UTF_8));
    long epiId =
        catalogo.createEpi(sesmt, "BOT-1", "Bota " + System.nanoTime(), AnnexGroup.C, true);

    IllegalArgumentException semPrint =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                catalogo.bindCaToEpi(
                    sesmt,
                    epiId,
                    "100",
                    CaStatus.ACTIVE,
                    LocalDate.now(),
                    LocalDate.now().plusYears(1),
                    LocalDateTime.now().withNano(0),
                    "Consulta no portal CAEPI",
                    true));
    assertTrue(semPrint.getMessage().startsWith("CAD-037"));

    long bindingId =
        catalogo.bindCaToEpi(
            sesmt,
            epiId,
            "100",
            CaStatus.ACTIVE,
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            LocalDateTime.now().withNano(0),
            "Consulta no portal CAEPI",
            true,
            PrintConsulta.nome(),
            PrintConsulta.png());
    assertEquals("100", numeroOf(bindingId));
    assertEquals(1, anexos(bindingId));
  }

  @Test
  void shouldDenyAlmoxarifeImport() {
    long almox = createUserWithRole("almox.caepi", Papel.ALMOXARIFE);
    AuthorizationDeniedException ex =
        assertThrows(
            AuthorizationDeniedException.class,
            () -> caepi.importar(almox, "RelatorioCA.csv", CSV.getBytes(StandardCharsets.UTF_8)));
    assertEquals("CAE-006 Seu perfil nao permite iniciar a atualizacao CAEPI.", ex.getMessage());
  }

  @Test
  void shouldDescribeTheFileWithoutWriting() {
    long sesmt = createUserWithRole("sesmt.caepi.previa", Papel.SESMT);
    int tentativas = caepi.ultimas(sesmt).size();
    CaepiPrevia previa =
        caepi.inspecionar(sesmt, "RelatorioCA.csv", CSV.getBytes(StandardCharsets.UTF_8));

    assertEquals(64, previa.sha256().length());
    assertEquals(1, previa.inspecao().cas());
    assertEquals(2, previa.inspecao().variantes());
    assertEquals(1, previa.inspecao().ativos());
    assertEquals(1, previa.inspecao().vencidos());
    assertEquals(tentativas, caepi.ultimas(sesmt).size());
    assertEquals(0, auditDeImportacao(sesmt));
  }

  private CaPublicado publicado(String ca) {
    return caepi.findByNumber(ca).orElseThrow();
  }

  private int auditDeImportacao(long usuarioId) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = 'CAEPI_IMPORTADA' AND usuario_id = ?
            """,
            Integer.class,
            usuarioId);
    return count == null ? 0 : count;
  }

  private String numeroOf(long bindingId) {
    return jdbcTemplate.queryForObject(
        "SELECT ca_number FROM epi_ca_binding WHERE id = ?", String.class, bindingId);
  }

  private String statusVinculo(long bindingId) {
    return jdbcTemplate.queryForObject(
        "SELECT ca_status FROM epi_ca_binding WHERE id = ?", String.class, bindingId);
  }

  private String validadeVinculo(long bindingId) {
    return jdbcTemplate.queryForObject(
        "SELECT valid_until FROM epi_ca_binding WHERE id = ?", String.class, bindingId);
  }

  private String notaOf(long bindingId) {
    return jdbcTemplate.queryForObject(
        "SELECT official_check_note FROM epi_ca_binding WHERE id = ?", String.class, bindingId);
  }

  private int anexos(long bindingId) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM epi_ca_evidence WHERE binding_id = ?", Integer.class, bindingId);
    return count == null ? 0 : count;
  }

  private int audit(long cargaId, String resultado) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = 'CAEPI_IMPORTADA'
              AND entidade = 'CAEPI'
              AND entidade_id = ?
              AND resultado = ?
            """,
            Integer.class,
            String.valueOf(cargaId),
            resultado);
    return count == null ? 0 : count;
  }

  private long createUserWithRole(String loginBase, Papel role) {
    String login = loginBase + "." + System.nanoTime();
    jdbcTemplate.update(
        """
        INSERT INTO usuario (nome, login, senha_hash, ativo, credencial_troca_obrigatoria, tentativas_invalidas)
        VALUES (?, ?, ?, 1, 0, 0)
        """,
        "User " + login,
        login,
        passwordEncoder.encode("SenhaForte#2026"));
    Long userId =
        jdbcTemplate.queryForObject("SELECT id FROM usuario WHERE login = ?", Long.class, login);
    jdbcTemplate.update(
        """
        INSERT INTO usuario_papel (usuario_id, papel_id)
        VALUES (?, (SELECT id FROM papel WHERE codigo = ?))
        """,
        userId,
        role.name());
    return userId;
  }
}
