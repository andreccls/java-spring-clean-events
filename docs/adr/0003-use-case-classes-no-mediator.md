# ADR 0003 — Casos de uso como classes concretas, sem mediador e sem CQRS

- **Status:** aceito
- **Data:** 2026-10-06

## Contexto

É comum ver CQRS com um mediador (`Mediator`/`CommandBus`) ou interfaces `UseCase<I, O>` genéricas para cada operação.
Aqui há ~16 casos de uso, sem *pipeline behaviors*, sem notificações e sem modelos de leitura separados.

## Decisão

Cada caso de uso é uma **classe concreta** com um método público `execute(...)` e dependências por construtor (portas):

```java
@UseCase
public class RegisterParticipant {
    public RegistrationView execute(UUID eventId, UUID participantId) { ... }
}
```

- O controller injeta exatamente o caso de uso de que precisa; a chamada é navegável com "ir para definição".
- `@UseCase` é uma anotação própria, em Java puro (`application`). O `ApplicationConfig` (em `web`) faz uma varredura por
  essa anotação: **novo caso de uso = nova classe**, sem editar lista de beans (Aberto/Fechado).
- Sem interface `UseCase<I,O>` genérica: cada `execute` tem assinatura natural (um `UUID`, um `record` de dados, uma
  página). Uma interface única forçaria *wrappers* e não traria benefício — ninguém precisa tratar casos de uso de forma
  polimórfica (YAGNI).
- Entrada: `record`s simples (`EventData`, `ParticipantData`) ou parâmetros; saída: `record`s `*View`. Sem AutoMapper.
- Erros são exceções (`DomainException` e derivadas), traduzidas **uma vez** em `ApiExceptionHandler`.

## Consequências

- (+) Zero dependência extra, fluxo rastreável, testes triviais (instanciar com fakes e chamar `execute`).
- (+) Segregação de interfaces: o controller só conhece os casos de uso que usa.
- (−) Preocupações transversais (log, auditoria, validação em cadeia) não têm *pipeline* pronto. Hoje a transação é a porta
  `Transaction` usada explicitamente onde precisa. Se surgirem muitas, um *decorator* sobre os casos de uso resolve sem
  trazer um mediador.
