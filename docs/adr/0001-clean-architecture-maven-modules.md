# ADR 0001 — Clean Architecture com módulos Maven separados

- **Status:** aceito
- **Data:** 2026-10-06

## Contexto

Clean Architecture só vale se a regra de dependência (tudo aponta para o domínio) for **imposta**, não apenas combinada.
Em projeto de pacotes únicos, um `import org.springframework...` no domínio compila e só um *code review* atento pega.

## Decisão

Quatro módulos Maven: `domain` ← `application` ← `infrastructure` ← `web`.

- `domain` não declara **nenhuma** dependência de runtime; `application` só depende de `domain`. Vazar Spring/JPA para o
  núcleo deixa de compilar.
- `infrastructure` (adaptador de saída) implementa as portas definidas em `application`; `web` (adaptador de entrada)
  também é o *composition root* (`EventsApplication`) e é o único que enxerga todos os módulos.
- Um teste ArchUnit (`ArchitectureTest`) complementa o grafo de módulos: pega dependências **transitivas** de framework e
  garante convenções (controllers não usam adaptadores; `@UseCase` só em `application`).

Não criei um módulo `bootstrap` separado do `web`: seria um módulo a mais só para conter uma classe `main` e uma classe de
configuração (YAGNI). Se surgir um segundo adaptador de entrada (ex.: mensageria), aí vale separar.

## Consequências

- (+) A regra de dependência é verificada pelo compilador **e** por teste; erosão arquitetural quebra o build.
- (+) Núcleo testável sem subir Spring (testes unitários em milissegundos).
- (−) Mais arquivos `pom.xml` e uma pequena cerimônia de build multi-módulo.
- (−) As portas ficam em `application`, então `infrastructure` depende de `application` (e não o contrário) — é a inversão
  de dependência funcionando como desejado, mas surpreende quem vem de arquitetura em camadas clássica.
