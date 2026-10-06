# ADR 0004 — Concorrência de inscrições: lock da linha do evento + índice único

- **Status:** aceito
- **Data:** 2026-10-06

## Contexto

A regra "não excede a capacidade" é um *check-then-act*: contar inscrições ativas e, se houver vaga, inserir. Duas requisições
simultâneas podem ambas contar `capacidade - 1` e ambas inserir — estourando a capacidade. O mesmo vale para "sem
inscrição duplicada" (duas requisições do mesmo participante).

## Decisão

1. **Lock pessimista na linha do evento.** `RegisterParticipant` roda dentro da porta `Transaction` e lê o evento com
   `findByIdForUpdate` (`SELECT ... FOR UPDATE`). Inscrições do **mesmo** evento passam a ser serializadas; eventos
   diferentes não se bloqueiam. A porta é do `application` (sem Spring); `SpringTransaction` a implementa com
   `TransactionTemplate`.
2. **Rede de segurança no banco.** `registrations` tem uma coluna gerada `active_key` (`1` se `ACTIVE`, `NULL` caso contrário)
   e `UNIQUE (event_id, participant_id, active_key)`. Como o MySQL ignora `NULL` em índices únicos, isso impõe "uma inscrição
   **ativa** por participante por evento" mantendo as canceladas como histórico — e uma violação vira `ConflictException`.
3. Idem e-mail único: `UNIQUE (email)` em `participants`; a verificação no caso de uso dá a mensagem amigável e o índice
   cobre a corrida (violação → `ConflictException`).

Alternativas descartadas: *lock* otimista (`@Version`) no evento — exigiria repetir a operação em conflito (retry), mais
complexidade para o exemplo; contador `registered` no evento — duplicaria estado que já é derivável.

## Consequências

- (+) Provado por `ConcurrentRegistrationIT`: 12 inscrições simultâneas num evento de 3 vagas → exatamente 3 aceitas, 9 `409`.
- (+) Corretude não depende só de código de aplicação: o banco também recusa o estado inválido.
- (−) Throughput por evento é limitado pelo lock (aceitável: a seção crítica é curta e por evento).
- (−) Coluna gerada + índice único é específico de MySQL/MariaDB (em PostgreSQL seria um índice parcial).
