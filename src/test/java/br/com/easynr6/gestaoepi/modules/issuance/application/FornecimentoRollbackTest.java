package br.com.easynr6.gestaoepi.modules.issuance.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.easynr6.gestaoepi.modules.employee.application.EmployeeManagementService;
import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import br.com.easynr6.gestaoepi.modules.employee.domain.UnitPolicy;
import br.com.easynr6.gestaoepi.modules.epi.PrintConsulta;
import br.com.easynr6.gestaoepi.modules.epi.application.EpiCatalogManagementService;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.epi.domain.CaStatus;
import br.com.easynr6.gestaoepi.modules.issuance.application.port.BaixaFornecimento;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarFornecimentoUseCase.ItemParaRegistrar;
import br.com.easynr6.gestaoepi.modules.issuance.application.usecase.RegistrarFornecimentoUseCase.Pedido;
import br.com.easynr6.gestaoepi.modules.issuance.domain.MotivoFornecimento;
import br.com.easynr6.gestaoepi.modules.matrix.application.MatrizManagementService;
import br.com.easynr6.gestaoepi.modules.stock.application.StockManagementService;
import br.com.easynr6.gestaoepi.shared.auth.Papel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Import(FornecimentoRollbackTest.BaixaQueFalha.class)
@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:sqlite:file:fornecimento-rollback-mem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class FornecimentoRollbackTest {

  private static final AtomicInteger FILIAL = new AtomicInteger(90);
  private static final AtomicInteger CA = new AtomicInteger(720000);
  private static final int[] PESOS_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] PESOS_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

  @Autowired private FornecimentoManagementService fornecimento;
  @Autowired private MatrizManagementService matriz;
  @Autowired private EmployeeManagementService estrutura;
  @Autowired private EpiCatalogManagementService catalogo;
  @Autowired private StockManagementService estoque;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void shouldRollBackFichaTermAndSuccessAuditWhenTheMovementFails() {
    long adminId = usuario("admin.rollback", Papel.ADMIN);
    long almoxId = usuario("almox.rollback", Papel.ALMOXARIFE);
    long unitId =
        estrutura.createUnit(adminId, "Planta rollback " + System.nanoTime(), proximoCnpj(), true);
    long departmentId = estrutura.createDepartment(adminId, "Setor " + unitId, unitId, true);
    long jobRoleId =
        estrutura.createJobRole(adminId, "Operador " + System.nanoTime(), departmentId, true);
    long employeeId =
        estrutura.createEmployee(
            adminId,
            "MAT" + System.nanoTime(),
            "Operador falha",
            departmentId,
            jobRoleId,
            null,
            true);
    long epiId =
        catalogo.createEpi(adminId, "RBK", "Luva falha " + System.nanoTime(), AnnexGroup.F, true);
    long caId =
        catalogo.bindCaToEpi(
            adminId,
            epiId,
            String.valueOf(CA.incrementAndGet()),
            CaStatus.ACTIVE,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2028, 1, 1),
            LocalDateTime.of(2026, 10, 5, 8, 0),
            "Consulta oficial para o fornecimento",
            true,
            PrintConsulta.nome(),
            PrintConsulta.png());
    matriz.incluir(adminId, PerfilVigente.Tipo.FUNCAO, jobRoleId, epiId);
    long loteId =
        estoque.receiveLot(
            adminId,
            unitId,
            epiId,
            caId,
            "L" + System.nanoTime(),
            null,
            "",
            LocalDate.now().plusDays(12),
            "2",
            "1.00",
            false);

    assertThrows(
        IllegalStateException.class,
        () ->
            fornecimento.registrar(
                almoxId,
                new Pedido(
                    employeeId,
                    true,
                    List.of(
                        new ItemParaRegistrar(
                            epiId,
                            loteId,
                            1,
                            MotivoFornecimento.PRIMEIRA_ENTREGA,
                            null,
                            true,
                            null,
                            null,
                            List.of(),
                            null,
                            null)))));

    assertEquals(
        0, contar("SELECT COUNT(1) FROM fornecimento_ficha WHERE employee_id = ?", employeeId));
    assertEquals(
        0,
        contar(
            """
            SELECT COUNT(1) FROM fornecimento_termo t
            JOIN fornecimento_ficha f ON f.id = t.ficha_id
            WHERE f.employee_id = ?
            """,
            employeeId));
    assertEquals(
        0,
        contar(
            """
            SELECT COUNT(1) FROM estoque_movimento
            WHERE lote_id = ? AND movement_type = 'BAIXA_FORNECIMENTO'
            """,
            loteId));
    assertEquals(
        0,
        contar(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = 'FORNECIMENTO_REGISTRADO' AND resultado = 'SUCESSO'
            """,
            employeeId));
  }

  private int contar(String sql, long id) {
    if (sql.contains("FORNECIMENTO_REGISTRADO")) {
      Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
      return count == null ? 0 : count;
    }
    Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
    return count == null ? 0 : count;
  }

  private long usuario(String loginBase, Papel role) {
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

  private static String proximoCnpj() {
    String base = String.format("44755266%04d", FILIAL.incrementAndGet());
    int primeiro = digito(base, PESOS_PRIMEIRO);
    int segundo = digito(base + primeiro, PESOS_SEGUNDO);
    return UnitPolicy.formatarCnpj(base + primeiro + segundo);
  }

  private static int digito(String base, int[] pesos) {
    int soma = 0;
    for (int i = 0; i < pesos.length; i++) {
      soma += (base.charAt(i) - '0') * pesos[i];
    }
    int resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  }

  @TestConfiguration
  static class BaixaQueFalha {
    @Bean
    @Primary
    BaixaFornecimento baixaFornecimento() {
      return baixa -> {
        throw new IllegalStateException("falha no movimento");
      };
    }
  }
}
