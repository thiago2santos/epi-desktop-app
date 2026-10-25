package br.com.easynr6.gestaoepi.modules.employee.application.usecase;

import br.com.easynr6.gestaoepi.identity.application.port.CredentialHasher;
import br.com.easynr6.gestaoepi.modules.employee.application.port.EmployeeRepository;
import br.com.easynr6.gestaoepi.modules.employee.domain.EmployeePolicy;
import br.com.easynr6.gestaoepi.shared.audit.AuditTrail;
import org.springframework.stereotype.Component;

/** Grava o hash da senha de recebimento. A senha em claro não entra na auditoria. */
@Component
public class SenhaRecebimentoGravacao {

  static final String DETALHE = "Senha de recebimento definida.";

  private final EmployeeRepository employeeRepository;
  private final EmployeeAccessAuthorizer accessAuthorizer;
  private final CredentialHasher credentialHasher;
  private final AuditTrail auditTrail;
  private final EmployeePolicy employeePolicy = new EmployeePolicy();

  public SenhaRecebimentoGravacao(
      EmployeeRepository employeeRepository,
      EmployeeAccessAuthorizer accessAuthorizer,
      CredentialHasher credentialHasher,
      AuditTrail auditTrail) {
    this.employeeRepository = employeeRepository;
    this.accessAuthorizer = accessAuthorizer;
    this.credentialHasher = credentialHasher;
    this.auditTrail = auditTrail;
  }

  public void gravar(Long actorId, Long employeeId, String matricula, String senha) {
    accessAuthorizer.assertCanDefineReceiptPassword(actorId);
    String limpa = employeePolicy.receiptPassword(senha, matricula);
    employeeRepository.gravarSenhaRecebimento(employeeId, credentialHasher.encode(limpa));
    auditTrail.registrarEventoCritico(
        actorId, "SENHA_RECEBIMENTO_DEFINIDA", "EMPLOYEE", String.valueOf(employeeId), DETALHE);
  }
}
