package br.com.easynr6.gestaoepi.shared.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:sqlite:file:authmem?mode=memory&cache=shared",
      "easy-nr6.bootstrap-admin.enabled=false",
      "easy-nr6.bootstrap-admin.password=SenhaBootstrap#2026"
    })
class AuthServiceIntegrationTest {

  @Autowired private AuthService authService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  void deveRetornarTrocaObrigatoriaQuandoFlagAtiva() {
    long usuarioId = inserirUsuario("maria", "SenhaForte#2026", true, 1, 0, null);
    vincularPapel(usuarioId, Papel.SESMT.name());

    AuthenticationResult resultado = authService.autenticar("maria", "SenhaForte#2026");

    assertEquals(AuthenticationStatus.FORCE_PASSWORD_CHANGE, resultado.status());
    assertNotNull(resultado.usuario());
  }

  @Test
  void deveBloquearContaAoAtingirLimiteDeTentativasInvalidas() {
    long usuarioId = inserirUsuario("joao", "SenhaForte#2026", true, 0, 4, null);
    vincularPapel(usuarioId, Papel.ALMOXARIFE.name());

    AuthenticationResult resultado = authService.autenticar("joao", "senha-errada");

    assertEquals(AuthenticationStatus.BLOCKED, resultado.status());
    assertNotNull(resultado.bloqueadoAte());
    Integer falhas =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE usuario_id = ? AND acao = 'LOGIN_BLOQUEIO_TEMPORARIO'
              AND resultado = 'FALHA' AND codigo = 'AUTH-002'
              AND correlacao IS NOT NULL
            """,
            Integer.class,
            usuarioId);
    assertEquals(1, falhas);
  }

  @Test
  void deveRegistrarLoginInexistente() {
    AuthenticationResult resultado = authService.autenticar("nao-existe", "qualquer");

    assertEquals(AuthenticationStatus.INVALID_CREDENTIAL, resultado.status());
    Integer falhas =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(1) FROM auditoria
            WHERE acao = 'LOGIN_USUARIO_DESCONHECIDO'
              AND resultado = 'FALHA' AND codigo = 'AUTH-001'
              AND usuario_id IS NULL
              AND correlacao IS NOT NULL
            """,
            Integer.class);
    assertEquals(1, falhas);
  }

  @Test
  void deveResetarTentativasAposLoginComSucesso() {
    long usuarioId =
        inserirUsuario(
            "ana", "SenhaForte#2026", true, 0, 2, LocalDateTime.now().minusMinutes(20).toString());
    vincularPapel(usuarioId, Papel.CONSULTA.name());

    AuthenticationResult resultado = authService.autenticar("ana", "SenhaForte#2026");

    assertEquals(AuthenticationStatus.SUCCESS, resultado.status());
    Integer tentativas =
        jdbcTemplate.queryForObject(
            "SELECT tentativas_invalidas FROM usuario WHERE id = ?", Integer.class, usuarioId);
    String bloqueadoAte =
        jdbcTemplate.queryForObject(
            "SELECT bloqueado_ate FROM usuario WHERE id = ?", String.class, usuarioId);
    assertEquals(0, tentativas);
    assertNull(bloqueadoAte);
  }

  private long inserirUsuario(
      String login,
      String senha,
      boolean ativo,
      int trocaObrigatoria,
      int tentativasInvalidas,
      String bloqueadoAte) {
    jdbcTemplate.update(
        """
        INSERT INTO usuario (
          nome, login, senha_hash, ativo, credencial_troca_obrigatoria, tentativas_invalidas, bloqueado_ate
        ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """,
        "Usuario Teste",
        login,
        passwordEncoder.encode(senha),
        ativo ? 1 : 0,
        trocaObrigatoria,
        tentativasInvalidas,
        bloqueadoAte);
    Long id =
        jdbcTemplate.queryForObject("SELECT id FROM usuario WHERE login = ?", Long.class, login);
    return id == null ? -1L : id;
  }

  private void vincularPapel(long usuarioId, String codigoPapel) {
    jdbcTemplate.update(
        """
        INSERT INTO usuario_papel (usuario_id, papel_id)
        VALUES (?, (SELECT id FROM papel WHERE codigo = ?))
        """,
        usuarioId,
        codigoPapel);
  }
}
