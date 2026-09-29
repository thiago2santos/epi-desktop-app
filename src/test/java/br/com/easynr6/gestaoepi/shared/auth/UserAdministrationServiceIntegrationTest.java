package br.com.easynr6.gestaoepi.shared.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:sqlite:file:useradminmem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class UserAdministrationServiceIntegrationTest {

  @Autowired private UserAdministrationService userAdministrationService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void deveRecusarCadastroQuandoAtorNaoForAdmin() {
    long sesmtId = inserirUsuarioComPapel("sesmt-cad", Papel.SESMT);

    assertThrows(
        AuthorizationDeniedException.class,
        () ->
            userAdministrationService.cadastrarUsuario(
                sesmtId,
                "Novo Usuario",
                "novo.usuario",
                "SenhaForte#2026",
                true,
                Set.of(Papel.CONSULTA)));
  }

  @Test
  void deveCadastrarUsuarioComTrocaObrigatoriaQuandoAdmin() {
    long adminId = inserirUsuarioComPapel("admin-cad", Papel.ADMIN);
    String loginNovo = "novo." + System.nanoTime();

    long usuarioCriadoId =
        userAdministrationService.cadastrarUsuario(
            adminId, "Usuario Criado", loginNovo, "SenhaForte#2026", true, Set.of(Papel.SESMT));

    Integer trocaObrigatoria =
        jdbcTemplate.queryForObject(
            "SELECT credencial_troca_obrigatoria FROM usuario WHERE id = ?",
            Integer.class,
            usuarioCriadoId);
    String papel =
        jdbcTemplate.queryForObject(
            """
            SELECT p.codigo
            FROM papel p
            JOIN usuario_papel up ON up.papel_id = p.id
            WHERE up.usuario_id = ?
            """,
            String.class,
            usuarioCriadoId);
    Integer auditoriaCount =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = 'USUARIO_CRIADO' AND entidade = 'USUARIO' AND entidade_id = ?
            """,
            Integer.class,
            String.valueOf(usuarioCriadoId));

    assertEquals(1, trocaObrigatoria);
    assertEquals(Papel.SESMT.name(), papel);
    assertEquals(1, auditoriaCount);
  }

  @Test
  void deveRecusarResetPorUsuarioNaoAdmin() {
    long adminId = inserirUsuarioComPapel("admin-reset", Papel.ADMIN);
    long consultaId = inserirUsuarioComPapel("consulta-reset", Papel.CONSULTA);
    long alvoId = criarUsuarioSemPapel("alvo.reset");

    userAdministrationService.atribuirPapel(adminId, alvoId, Papel.SESMT);

    assertThrows(
        AuthorizationDeniedException.class,
        () ->
            userAdministrationService.resetarCredencial(
                consultaId, alvoId, "alvo.reset", "NovaSenha#2026"));
  }

  @Test
  void devePermitirResetPorAdminEAuditarEvento() {
    long adminId = inserirUsuarioComPapel("admin-ok-reset", Papel.ADMIN);
    long alvoId = criarUsuarioSemPapel("alvo.ok.reset");

    userAdministrationService.resetarCredencial(
        adminId, alvoId, "alvo.ok.reset", "OutraSenha#2026");

    Integer trocaObrigatoria =
        jdbcTemplate.queryForObject(
            "SELECT credencial_troca_obrigatoria FROM usuario WHERE id = ?", Integer.class, alvoId);
    Integer auditoriaCount =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = 'CREDENCIAL_RESETADA' AND entidade = 'USUARIO' AND entidade_id = ?
            """,
            Integer.class,
            String.valueOf(alvoId));

    assertEquals(1, trocaObrigatoria);
    assertEquals(1, auditoriaCount);
  }

  @Test
  void devePermitirRemocaoDePapelPorAdminEAuditar() {
    long adminId = inserirUsuarioComPapel("admin-remove-papel", Papel.ADMIN);
    long alvoId = criarUsuarioSemPapel("alvo.remove.papel");

    userAdministrationService.atribuirPapel(adminId, alvoId, Papel.CONSULTA);
    userAdministrationService.removerPapel(adminId, alvoId, Papel.CONSULTA);

    Integer papeisRestantes =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM usuario_papel WHERE usuario_id = ?", Integer.class, alvoId);
    Integer auditoriaCount =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = 'PAPEL_REMOVIDO' AND entidade = 'USUARIO' AND entidade_id = ?
            """,
            Integer.class,
            String.valueOf(alvoId));

    assertEquals(0, papeisRestantes);
    assertEquals(1, auditoriaCount);
  }

  @Test
  void deveRecusarRemocaoDePapelQuandoAtorNaoForAdmin() {
    long adminId = inserirUsuarioComPapel("admin-remove-denied", Papel.ADMIN);
    long sesmtId = inserirUsuarioComPapel("sesmt-remove-denied", Papel.SESMT);
    long alvoId = criarUsuarioSemPapel("alvo.remove.denied");

    userAdministrationService.atribuirPapel(adminId, alvoId, Papel.CONSULTA);

    assertThrows(
        AuthorizationDeniedException.class,
        () -> userAdministrationService.removerPapel(sesmtId, alvoId, Papel.CONSULTA));
  }

  private long inserirUsuarioComPapel(String loginBase, Papel papel) {
    long usuarioId = criarUsuarioSemPapel(loginBase + "." + System.nanoTime());
    jdbcTemplate.update(
        """
        INSERT INTO usuario_papel (usuario_id, papel_id)
        VALUES (?, (SELECT id FROM papel WHERE codigo = ?))
        """,
        usuarioId,
        papel.name());
    return usuarioId;
  }

  private long criarUsuarioSemPapel(String login) {
    String senhaHash = passwordEncoder.encode("SenhaForte#2026");
    jdbcTemplate.update(
        """
        INSERT INTO usuario (
          nome, login, senha_hash, ativo, credencial_troca_obrigatoria, tentativas_invalidas
        ) VALUES (?, ?, ?, 1, 0, 0)
        """,
        "Usuario " + login,
        login,
        senhaHash);
    Long id =
        jdbcTemplate.queryForObject("SELECT id FROM usuario WHERE login = ?", Long.class, login);
    return id == null ? -1L : id;
  }
}
