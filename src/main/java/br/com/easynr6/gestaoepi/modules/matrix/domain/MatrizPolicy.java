package br.com.easynr6.gestaoepi.modules.matrix.domain;

import br.com.easynr6.gestaoepi.modules.employee.domain.PerfilVigente;

/** Regras de UC-MAT-01. A lista vigente é a do perfil, sem somar função e GHE. */
public class MatrizPolicy {

  public void requireInclusao(PerfilVigente.Tipo tipo, Long perfilId, Long epiId) {
    if (tipo == null || perfilId == null || epiId == null) {
      throw new IllegalArgumentException("MAT-001 Escolha o perfil e o EPI.");
    }
  }

  public void assertPodeIncluir(
      PerfilVigente.Tipo tipo, boolean encontrado, boolean ativo, boolean emGheAtivo) {
    if (tipo == PerfilVigente.Tipo.FUNCAO && emGheAtivo) {
      throw new IllegalArgumentException("MAT-008 Funcao membro de GHE ativo.");
    }
    if (!encontrado || !ativo) {
      throw new IllegalArgumentException("MAT-004 Escolha um perfil ativo e um EPI ativo.");
    }
  }

  public void assertEpiAtivo(boolean encontrado, boolean ativo) {
    if (!encontrado || !ativo) {
      throw new IllegalArgumentException("MAT-004 Escolha um perfil ativo e um EPI ativo.");
    }
  }

  public void assertTemCaAtivo(boolean temCa) {
    if (!temCa) {
      throw new IllegalArgumentException(
          "MAT-003 Este EPI nao tem CA ativo para entrar na matriz.");
    }
  }

  public void assertSemLinhaAtiva(boolean jaExiste) {
    if (jaExiste) {
      throw new IllegalArgumentException("MAT-002 Este EPI ja esta na matriz deste perfil.");
    }
  }

  public void assertLinhaAtiva(boolean encontrada, boolean ativa) {
    if (!encontrada || !ativa) {
      throw new IllegalArgumentException("MAT-007 Esta linha da matriz nao esta ativa.");
    }
  }
}
