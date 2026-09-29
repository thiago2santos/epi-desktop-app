package br.com.easynr6.gestaoepi.ui.operacao;

import br.com.easynr6.gestaoepi.domain.EntregaRules;
import java.time.LocalDate;
import java.util.List;

public final class EntregaWizardState {

  private static final List<String> STEPS =
      List.of(
          "1. Trabalhador", "2. EPI e lote", "3. Validacoes", "4. Confirmacao", "5. Comprovante");

  private int currentStepIndex;
  private String trabalhador;
  private String epi;
  private String lote;
  private LocalDate validadeLote;
  private int saldoAtual;
  private int quantidadeSolicitada;
  private boolean validacaoResponsavel;
  private boolean confirmacaoOperacao;
  private boolean finalizado;

  public List<String> steps() {
    return STEPS;
  }

  public String currentStepLabel() {
    return STEPS.get(currentStepIndex);
  }

  public int currentStepNumber() {
    return currentStepIndex + 1;
  }

  public boolean canGoBack() {
    return currentStepIndex > 0;
  }

  public boolean canGoNext() {
    if (currentStepIndex == STEPS.size() - 1) {
      return false;
    }
    return currentStepValido();
  }

  public void goBack() {
    if (canGoBack()) {
      currentStepIndex -= 1;
    }
  }

  public void goNext() {
    if (!canGoNext()) {
      throw new IllegalStateException("Passo atual invalido para avancar.");
    }
    currentStepIndex += 1;
  }

  public boolean canFinalize() {
    return currentStepIndex == STEPS.size() - 1 && confirmacaoOperacao;
  }

  public void finalizar() {
    if (!canFinalize()) {
      throw new IllegalStateException("Fluxo ainda nao pode ser finalizado.");
    }
    finalizado = true;
  }

  public boolean isFinalizado() {
    return finalizado;
  }

  public void setTrabalhador(String trabalhador) {
    this.trabalhador = trabalhador;
  }

  public void setEpi(String epi) {
    this.epi = epi;
  }

  public void setLote(String lote) {
    this.lote = lote;
  }

  public void setValidadeLote(LocalDate validadeLote) {
    this.validadeLote = validadeLote;
  }

  public void setSaldoAtual(int saldoAtual) {
    this.saldoAtual = saldoAtual;
  }

  public void setQuantidadeSolicitada(int quantidadeSolicitada) {
    this.quantidadeSolicitada = quantidadeSolicitada;
  }

  public void setValidacaoResponsavel(boolean validacaoResponsavel) {
    this.validacaoResponsavel = validacaoResponsavel;
  }

  public void setConfirmacaoOperacao(boolean confirmacaoOperacao) {
    this.confirmacaoOperacao = confirmacaoOperacao;
  }

  public String getTrabalhador() {
    return trabalhador;
  }

  public String getEpi() {
    return epi;
  }

  public String getLote() {
    return lote;
  }

  public LocalDate getValidadeLote() {
    return validadeLote;
  }

  public int getSaldoAtual() {
    return saldoAtual;
  }

  public int getQuantidadeSolicitada() {
    return quantidadeSolicitada;
  }

  public boolean isValidacaoResponsavel() {
    return validacaoResponsavel;
  }

  public boolean isConfirmacaoOperacao() {
    return confirmacaoOperacao;
  }

  private boolean currentStepValido() {
    return switch (currentStepIndex) {
      case 0 -> textoPreenchido(trabalhador);
      case 1 ->
          textoPreenchido(epi)
              && textoPreenchido(lote)
              && validadeLote != null
              && EntregaRules.podeEntregarLote(validadeLote, saldoAtual, quantidadeSolicitada);
      case 2 -> validacaoResponsavel;
      case 3 -> confirmacaoOperacao;
      default -> true;
    };
  }

  private static boolean textoPreenchido(String texto) {
    return texto != null && !texto.isBlank();
  }
}
