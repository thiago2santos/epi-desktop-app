package br.com.easynr6.gestaoepi.modules.epi.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.easynr6.gestaoepi.modules.epi.PrintConsulta;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import br.com.easynr6.gestaoepi.shared.auth.AuthorizationDeniedException;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:sqlite:file:epi-catalog-mem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class EpiCatalogManagementServiceIntegrationTest {

  @Autowired private EpiCatalogManagementService epiCatalogService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void shouldCreateEpiAndBindCaWhenActorIsAdmin() {
    long adminId = createUserWithRole("admin.epi", Papel.ADMIN);
    long epiId =
        epiCatalogService.createEpi(adminId, "CAP-100", "Capacete Classe B", AnnexGroup.A, true);
    long bindingId =
        epiCatalogService.bindCaToEpi(
            adminId,
            epiId,
            "CA 12345",
            CaStatus.ACTIVE,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2028, 1, 1),
            LocalDateTime.of(2026, 9, 29, 22, 45),
            "Consulta oficial no CAEPI",
            true,
            PrintConsulta.nome(),
            PrintConsulta.png());

    Integer epiCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM epi_catalog WHERE id = ?", Integer.class, epiId);
    Integer bindingCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM epi_ca_binding WHERE id = ?", Integer.class, bindingId);
    Integer auditCount =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1)
            FROM auditoria
            WHERE resultado = 'SUCESSO'
              AND ((acao = 'EPI_CREATED' AND entidade_id = ?)
                OR (acao = 'EPI_CA_BOUND' AND entidade_id = ?))
            """,
            Integer.class,
            String.valueOf(epiId),
            String.valueOf(bindingId));

    assertEquals(1, epiCount);
    assertEquals(1, bindingCount);
    assertEquals(2, auditCount);
  }

  @Test
  void shouldRejectCreateWhenActorHasNoPermission() {
    long consultaId = createUserWithRole("consulta.epi", Papel.CONSULTA);

    assertThrows(
        AuthorizationDeniedException.class,
        () ->
            epiCatalogService.createEpi(
                consultaId, "LUV-200", "Luva Nitrilica", AnnexGroup.F, true));
  }

  @Test
  void shouldRejectMissingOfficialEvidence() {
    long adminId = createUserWithRole("admin.epi.evidence", Papel.ADMIN);
    long epiId =
        epiCatalogService.createEpi(adminId, "BOT-300", "Bota de Seguranca", AnnexGroup.C, true);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                epiCatalogService.bindCaToEpi(
                    adminId,
                    epiId,
                    "CA-55443",
                    CaStatus.ACTIVE,
                    LocalDate.of(2026, 2, 1),
                    LocalDate.of(2028, 2, 1),
                    null,
                    "",
                    true));
    assertEquals("CAD-037 Evidencia de consulta oficial do CA ausente.", ex.getMessage());
  }

  @Test
  void shouldRejectCaValidityConflict() {
    long adminId = createUserWithRole("admin.epi.conflict", Papel.ADMIN);
    long epiId =
        epiCatalogService.createEpi(adminId, "PROT-400", "Protetor Auditivo", AnnexGroup.E, true);

    epiCatalogService.bindCaToEpi(
        adminId,
        epiId,
        "CA 99999",
        CaStatus.ACTIVE,
        LocalDate.of(2026, 3, 1),
        LocalDate.of(2026, 12, 31),
        LocalDateTime.of(2026, 9, 29, 22, 50),
        "Primeira consulta",
        true,
        PrintConsulta.nome(),
        PrintConsulta.png());

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                epiCatalogService.bindCaToEpi(
                    adminId,
                    epiId,
                    "CA99999",
                    CaStatus.ACTIVE,
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2027, 1, 1),
                    LocalDateTime.of(2026, 9, 29, 22, 55),
                    "Segunda consulta sobreposta",
                    true,
                    PrintConsulta.nome(),
                    PrintConsulta.png()));
    assertEquals("CAD-035 CA com conflito de vigencia para o mesmo EPI.", ex.getMessage());
  }

  @Test
  void shouldBlockEpiActivationWithoutActiveCoherentCa() {
    long adminId = createUserWithRole("admin.epi.status", Papel.ADMIN);
    long epiId = epiCatalogService.createEpi(adminId, "AV-500", "Avental PVC", AnnexGroup.G, false);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> epiCatalogService.setEpiStatus(adminId, epiId, true));
    assertEquals("CAD-038 Operacao nao permitida por dependencia historica.", ex.getMessage());

    epiCatalogService.bindCaToEpi(
        adminId,
        epiId,
        "CA-78787",
        CaStatus.ACTIVE,
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2028, 1, 1),
        LocalDateTime.of(2026, 9, 29, 23, 0),
        "Consulta para ativacao",
        true,
        PrintConsulta.nome(),
        PrintConsulta.png());
    epiCatalogService.setEpiStatus(adminId, epiId, true);

    Integer active =
        jdbcTemplate.queryForObject(
            "SELECT active FROM epi_catalog WHERE id = ?", Integer.class, epiId);
    assertEquals(1, active);
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
    if (userId == null) {
      return -1L;
    }
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
