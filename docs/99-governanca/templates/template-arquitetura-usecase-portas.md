# Template - Arquitetura por Use Case + Portas (KISS/YAGNI)

Use este template para implementar novos modulos/cadastros preservando:

- nucleo de dominio isolado;
- sem vazamento de infraestrutura para dentro do core;
- componentes injetaveis e testaveis unitariamente.

## 1) Estrutura de pacotes recomendada

```text
src/main/java/br/com/easynr6/gestaoepi/modules/<module>/
  domain/
    <Entity>.java
    <ValueObject>.java
    <Module>Policy.java
    <Module>Error.java
  application/
    port/
      <Module>Repository.java
      AuthorizationService.java
      Clock.java                # reutilizar porta compartilhada quando existir
    usecase/
      Create<Aggregate>UseCase.java
      Update<Aggregate>UseCase.java
      Set<Aggregate>StatusUseCase.java
      List<Aggregate>UseCase.java
  infra/
    jdbc/
      Jdbc<Module>Repository.java
    config/
      <Module>WiringConfig.java # opcional, so se necessario
  ui/
    <Module>Controller.java
```

Regra pratica:

- `domain` nao importa Spring/JDBC/JavaFX;
- `application` orquestra regra de negocio via portas;
- `infra` implementa IO/banco/framework;
- `ui` chama use cases e renderiza feedback.

## 2) Contratos minimos (portas)

Comece pequeno (YAGNI). So criar portas que o use case realmente precisa.

```java
public interface EquipmentRepository {
  boolean existsByBusinessKey(String key);
  Long create(Equipment equipment);
  java.util.Optional<Equipment> findById(Long id);
  void update(Equipment equipment);
  void setActive(Long id, boolean active);
  java.util.List<Equipment> list(String query, Boolean active);
}
```

```java
public interface AuthorizationService {
  void assertCanManageCatalog(Long actorId);
}
```

Observacoes:

- `AuditTrail` pode permanecer em `shared` como porta transversal;
- `Clock` deve ser injetavel para testes deterministicos de data/hora.

## 3) Esqueleto de use case (injeção por construtor)

```java
@org.springframework.stereotype.Service
public class CreateEquipmentUseCase {

  private final EquipmentRepository equipmentRepository;
  private final AuthorizationService authorizationService;
  private final br.com.easynr6.gestaoepi.shared.audit.AuditTrail auditTrail;
  private final EquipmentPolicy equipmentPolicy = new EquipmentPolicy();

  public CreateEquipmentUseCase(
      EquipmentRepository equipmentRepository,
      AuthorizationService authorizationService,
      br.com.easynr6.gestaoepi.shared.audit.AuditTrail auditTrail) {
    this.equipmentRepository = equipmentRepository;
    this.authorizationService = authorizationService;
    this.auditTrail = auditTrail;
  }

  @org.springframework.transaction.annotation.Transactional
  public Long execute(Long actorId, String code, String description, boolean active) {
    authorizationService.assertCanManageCatalog(actorId);
    equipmentPolicy.validateRequiredFields(code, description);

    String normalizedCode = equipmentPolicy.normalizeCode(code);
    if (equipmentRepository.existsByBusinessKey(normalizedCode)) {
      throw new IllegalArgumentException("CAD-XXX Business key already exists.");
    }

    Equipment equipment = Equipment.createNew(normalizedCode, description, active);
    Long id = equipmentRepository.create(equipment);
    auditTrail.registrarEventoCritico(
        actorId, "EQUIPMENT_CREATED", "EQUIPMENT", String.valueOf(id), "Catalog record created");
    return id;
  }
}
```

## 4) Regras para nao vazar infra para o dominio

- proibido no `domain`:
  - `JdbcTemplate`, `ResultSet`, `EntityManager`, `@Repository`;
  - `@Service`, `@Transactional`;
  - classes JavaFX (`TableView`, `TextField` etc.).
- permitido no `domain`:
  - classes puras Java;
  - entidades, enums, value objects e policies.

## 5) Padrao de erro e mensagens

- dominio retorna erro codificado (`CAD-0xx`, `AUTH-0xx`);
- UI traduz para mensagem amigavel;
- log/auditoria sempre com contexto tecnico minimo:
  - `actorId`, `action`, `entity`, `entityId`, `details`.

## 6) Testes unitarios obrigatorios por use case

Para cada use case novo, garantir no minimo:

- cenario feliz;
- validacao de obrigatorios;
- regra de unicidade/conflito;
- autorizacao negada;
- auditoria chamada em acao critica.

Exemplo de stack de teste:

- JUnit 5
- Mockito (ou fake in-memory simples)

## 7) Checklist rapido antes do commit

- nomes de codigo em ingles (dominio, classes, campos, enums);
- texto de UI em pt-BR (quando aplicavel);
- use case sem SQL direto;
- policy sem dependencia externa;
- testes cobrindo bloqueios reais de negocio;
- documentacao do UC atualizada (`spec`, `matriz`, `backlog`).
