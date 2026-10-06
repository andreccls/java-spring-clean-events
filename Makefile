# Works with or without a local JDK/Maven:
#  - `mvn` found  -> runs natively (MySQL still comes from docker compose)
#  - `mvn` absent -> runs inside the official Maven+JDK 21 container (compose service `maven`)
-include .env
export

MYSQL_PORT ?= 3316
API_PORT ?= 8090
MYSQL_ROOT_PASSWORD ?= dev_only_root_password
COMPOSE := docker compose

ifneq ($(shell command -v mvn 2>/dev/null),)
  # Native mode: integration tests read these variables to find MySQL.
  export TEST_MYSQL_URL ?= jdbc:mysql://127.0.0.1:$(MYSQL_PORT)/events_test?createDatabaseIfNotExist=true
  export TEST_MYSQL_PASSWORD ?= $(MYSQL_ROOT_PASSWORD)
  MVN       = mvn -B
  MVN_NODB  = mvn -B
  DB_DEP    = up-db
else
  MVN       = $(COMPOSE) --profile tools run --rm maven mvn -B
  MVN_NODB  = $(COMPOSE) --profile tools run --rm --no-deps maven mvn -B
  DB_DEP    =
endif

.DEFAULT_GOAL := help
.PHONY: help build test test-unit coverage up down up-db clean

help: ## Show this help
	@grep -E '^[a-z-]+:.*##' $(MAKEFILE_LIST) | awk -F':.*## ' '{printf "  make %-10s %s\n", $$1, $$2}'

build: ## Compile and package everything (no tests)
	$(MVN_NODB) -DskipTests package

test-unit: ## Unit tests only (domain + application + architecture); no database needed
	$(MVN_NODB) -DskipITs test

test: $(DB_DEP) ## ALL tests: unit + integration (MySQL is started automatically); enforces the 100% gate
	$(MVN) verify

coverage: test ## Tests + JaCoCo; fails if domain/application < 100%. Prints the summary table
	@sh scripts/coverage-summary.sh

up: ## Start API + MySQL (http://localhost:$(API_PORT)/swagger-ui.html)
	$(COMPOSE) up --build -d --wait

down: ## Stop the stack (keeps the database volume)
	$(COMPOSE) --profile tools down

up-db: ## Start only MySQL (used by the integration tests)
	$(COMPOSE) up -d --wait mysql

clean: ## Stop the stack, DELETE the volumes and build output
	$(COMPOSE) --profile tools down -v
	find . -type d -name target -prune -exec rm -rf {} +
