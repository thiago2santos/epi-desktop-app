package br.com.easynr6.gestaoepi.ui.cadastro;

import java.util.ArrayList;
import java.util.List;

/**
 * Decide quais operações do UC-CAD-03 o salvamento dispara.
 *
 * <p>A inativação passa por um passo próprio para exigir confirmação e para não gravar o status
 * inativo junto com a edição dos dados. O caso de uso pede confirmação explícita e a regra CAD-005
 * só vale nesse passo.
 */
public final class PlanoCadastroTrabalhador {

  public enum Passo {
    CRIAR,
    ATUALIZAR,
    INATIVAR,
    REATIVAR
  }

  private final boolean confirmarInativacao;
  private final List<Passo> passos;

  private PlanoCadastroTrabalhador(boolean confirmarInativacao, List<Passo> passos) {
    this.confirmarInativacao = confirmarInativacao;
    this.passos = passos;
  }

  public static PlanoCadastroTrabalhador de(
      boolean novo, boolean estavaAtivo, boolean ficaAtivo, boolean dadosAlterados) {
    if (novo) {
      return new PlanoCadastroTrabalhador(false, List.of(Passo.CRIAR));
    }
    List<Passo> passos = new ArrayList<>();
    if (dadosAlterados) {
      passos.add(Passo.ATUALIZAR);
    }
    if (estavaAtivo && !ficaAtivo) {
      passos.add(Passo.INATIVAR);
    } else if (!estavaAtivo && ficaAtivo) {
      passos.add(Passo.REATIVAR);
    }
    return new PlanoCadastroTrabalhador(estavaAtivo && !ficaAtivo, List.copyOf(passos));
  }

  public boolean confirmarInativacao() {
    return confirmarInativacao;
  }

  public List<Passo> passos() {
    return passos;
  }

  public boolean vazio() {
    return passos.isEmpty();
  }
}
