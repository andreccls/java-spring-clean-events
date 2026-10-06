# ADR 0002 — Entidades JPA separadas das de domínio, com mappers manuais

- **Status:** aceito
- **Data:** 2026-10-06

## Contexto

Duas opções: (a) anotar as classes de domínio com JPA, ou (b) ter entidades JPA próprias em `infrastructure` e converter.
A opção (a) é menos código, mas obriga o domínio a depender de `jakarta.persistence`, exige construtor sem argumentos e
setters/campos mutáveis para o Hibernate — exatamente o que o domínio rico evita — e quebraria a regra "domain sem
framework" (e o módulo `domain` sem dependências).

## Decisão

Opção (b): `EventEntity`, `ParticipantEntity` e `RegistrationEntity` (pacote-privadas, em `infrastructure/persistence`)
com **um par de métodos cada** — `static from(Domínio)` e `toDomain()` — e os adaptadores `Jpa*Repository` fazem a ponte.
O domínio expõe `Event.restore(...)` (e equivalentes) para **reidratar** um agregado sem refazer regras de criação.

Sem MapStruct: são três tipos com poucos campos; o mapeamento manual é explícito, rastreável com "ir para definição" e
não adiciona dependência nem processador de anotações (KISS/YAGNI). Sem Lombok pelo mesmo motivo; `record`s cobrem VOs e DTOs.

## Consequências

- (+) Domínio 100 % Java puro e totalmente testável sem banco; o esquema do banco pode evoluir sem contaminar o modelo.
- (+) Mapeamento coberto pelos testes de integração (round-trip de todos os campos, inclusive microssegundos).
- (−) Código de mapeamento escrito à mão; ao adicionar um campo é preciso lembrar de entidade + `from` + `toDomain`
  (o `ddl-auto=validate` e os testes de round-trip denunciam o esquecimento).
- (−) `save` faz um `merge` (SELECT + INSERT/UPDATE) porque o id é atribuído pela aplicação; aceitável para o volume de um exemplo.
