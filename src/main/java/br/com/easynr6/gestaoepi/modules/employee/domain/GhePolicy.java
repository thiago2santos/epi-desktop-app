package br.com.easynr6.gestaoepi.modules.employee.domain;

import java.util.List;

/** Regras de UC-CAD-07. O perfil vigente é o GHE ativo da função, ou a própria função. */
public class GhePolicy {

  public String requireName(String name) {
    String normalized = name == null ? "" : name.trim();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("CAD-051 Nome ou unidade ausente.");
    }
    return normalized;
  }

  public void requireUnit(Long unitId) {
    if (unitId == null) {
      throw new IllegalArgumentException("CAD-051 Nome ou unidade ausente.");
    }
  }

  public void assertJobRoleCanLink(boolean found, boolean active, boolean sameUnit) {
    if (!found || !active || !sameUnit) {
      throw new IllegalArgumentException(
          "CAD-054 Funcao inativa, de outra unidade ou inexistente.");
    }
  }

  public void assertNotInOtherGhe(Long currentGheId, Long targetGheId) {
    if (currentGheId != null && !currentGheId.equals(targetGheId)) {
      throw new IllegalArgumentException("CAD-055 Funcao ja pertence a outro GHE.");
    }
  }

  /** GHE ativo usa só a lista do grupo, mesmo vazia. Sem GHE ativo, usa a lista da função. */
  public <T> List<T> listaVigente(boolean gheAtivo, List<T> linhasDoGhe, List<T> linhasDaFuncao) {
    List<T> escolhida = gheAtivo ? linhasDoGhe : linhasDaFuncao;
    if (escolhida == null || escolhida.isEmpty()) {
      return List.of();
    }
    return List.copyOf(escolhida);
  }
}
