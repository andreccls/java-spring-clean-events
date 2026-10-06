# ADR 0005 — Estratégia de testes e metas de cobertura

- **Status:** aceito
- **Data:** 2026-10-06

## Contexto

O valor do sistema está nas regras de negócio, então é onde a cobertura precisa ser total e barata. Os adaptadores precisam
ser provados contra a tecnologia real. A arquitetura precisa ser protegida contra erosão. E, por ser um projeto de
referência, os testes unitários também servem como documentação executável.

## Decisão

1. **`domain`** — JUnit 5 + AssertJ puros, escritos em **TDD** (teste vermelho → verde). Meta: **100 % de linhas e branches,
   como gate** (regra `check` do JaCoCo; `mvn verify` falha abaixo disso).
2. **`application`** — casos de uso com **fakes em memória** escritos à mão (`InMemory*Repository`, relógio fixo, `Transaction`
   direta) em vez de Mockito: testes por estado, legíveis, e os fakes documentam o contrato das portas. Meta: **100 %, gate**.
3. **`web` / `ArchitectureTest`** — ArchUnit: regra de dependência e convenções.
4. **`infrastructure` `*IT`** — integração com **MySQL real** (mapeamento, migrations, índices únicos, filtros SQL, lock sob
   concorrência). **`web` `*IT`** — `MockMvc` + aplicação completa + MySQL real (contrato HTTP, `ProblemDetail`, OpenAPI, health).
   Cobertura só **reportada**.
5. **Convenção:** `*Test` = unitário (surefire, sem I/O), `*IT` = integração (failsafe). `mvn test` roda só unitários.

**MySQL nos testes de integração:** `docker compose` (serviço `mysql` com *healthcheck*) + variáveis de ambiente
(`TEST_MYSQL_URL`, `TEST_MYSQL_PASSWORD`), **em vez de Testcontainers**. Motivo: Testcontainers precisa falar com o Docker
daemon; quando o Maven roda **dentro de um container** (é como `make test` funciona sem JDK instalado) seria preciso montar
`/var/run/docker.sock` e resolver rede entre containers irmãos. Com o compose, o mesmo caminho funciona local, no container
do Maven e no GitHub Actions (service container). O custo: exige `make up-db` (automático no `make test`) e o banco de testes
`events_test` é recriado (`flyway.clean()` + `migrate()`) a cada execução.

**Cobertura:** JaCoCo 0.8.13, agente no `parent`, relatório em `verify`; o `check` fica **por módulo** (`domain`,
`application`), então cada camada precisa ser coberta pelos **seus próprios** testes. Nenhuma exclusão de cobertura é usada.

## Consequências

- (+) Regressão de regra de negócio ou de arquitetura derruba o CI.
- (+) Fakes documentam o contrato das portas; trocar de ORM não toca os testes de `application`.
- (−) 100 % de branches exige disciplina (cada `throw` tem um teste), por isso o gate só vale onde compensa: `domain` e
  `application`. `infrastructure` e `web` são medidos, não travados.
- (−) Testes de integração exigem MySQL no ar e rodam mais devagar (~35 s no total contra ~2 s dos unitários).
