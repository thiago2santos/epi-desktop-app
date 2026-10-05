package br.com.easynr6.gestaoepi.modules.matrix.application.port;

import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;
import br.com.easynr6.gestaoepi.modules.epi.domain.AnnexGroup;
import br.com.easynr6.gestaoepi.modules.matrix.domain.ModoMatriz;
import java.util.List;
import java.util.Optional;

public interface MatrizRepository {

  List<PerfilOpcao> listPerfis();

  Optional<PerfilAlvo> findPerfil(PerfilVigente.Tipo tipo, Long perfilId);

  Optional<EpiAlvo> findEpi(Long epiId);

  Optional<CaSugerido> caAtivoAtual(Long epiId);

  boolean existeLinhaAtiva(PerfilVigente.Tipo tipo, Long perfilId, Long epiId);

  Long incluir(
      PerfilVigente.Tipo tipo,
      Long perfilId,
      Long epiId,
      Long caBindingId,
      ModoMatriz modo,
      boolean exigeTreinamento);

  Optional<LinhaGravada> findLinha(Long linhaId);

  void alterar(Long linhaId, Long caBindingId, ModoMatriz modo, boolean exigeTreinamento);

  void inativar(Long linhaId);

  List<LinhaMatriz> listarAtivas(PerfilVigente.Tipo tipo, Long perfilId);

  List<EpiOpcao> listarCandidatos(PerfilVigente.Tipo tipo, Long perfilId);

  record PerfilOpcao(PerfilVigente.Tipo tipo, Long id, String rotulo) {}

  record PerfilAlvo(boolean encontrado, boolean ativo, boolean emGheAtivo) {}

  record EpiAlvo(boolean encontrado, boolean ativo) {}

  record CaSugerido(Long id, String numero) {}

  record EpiOpcao(Long id, String descricao, AnnexGroup anexo) {}

  record LinhaGravada(
      Long id,
      Long epiId,
      Long caBindingId,
      ModoMatriz modo,
      boolean exigeTreinamento,
      boolean ativa) {}

  record LinhaMatriz(
      Long id,
      Long epiId,
      String epi,
      AnnexGroup anexo,
      String ca,
      ModoMatriz modo,
      boolean exigeTreinamento) {}
}
