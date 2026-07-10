.PHONY: all domain cli core ffi jgit vcs-indexer vcs-indexer-ffi vcs-indexer-jgit \
	infra-sql infra-arangodb \
	combo-core-ffi combo-core-jgit combo-ffi-jgit combo-core-infra-sql \
	combo-core-infra-arangodb \
	combo-all help

# Default: run all tests
all: combo-all

help:
	@echo "Available targets:"
	@echo ""
	@echo "Single module tests:"
	@echo "  cli             - Test cli module"
	@echo "  core            - Test core module"
	@echo "  ffi             - Test ffi module"
	@echo "  jgit            - Test jgit module"
	@echo "  infra-sql         - Test infrastructure-sql module"
	@echo "  infra-arangodb    - Test infrastructure-arangodb module"
	@echo "  vcs-indexer-ffi - Test vcs-indexer with gix profile"
	@echo "  vcs-indexer-jgit - Test vcs-indexer with jgit profile"
	@echo "  vcs-indexer     - Test vcs-indexer with both profiles"
	@echo ""
	@echo "Combined tests (modules tested together):"
	@echo "  combo-core-ffi      - Test core + ffi together"
	@echo "  combo-core-jgit     - Test core + jgit together"
	@echo "  combo-ffi-jgit      - Test ffi + jgit together"
	@echo "  combo-core-infra-sql      - Test core + infrastructure-sql together"
	@echo "  combo-core-infra-arangodb - Test core + infrastructure-arangodb together"
	@echo "  combo-all           - Test all modules together"
	@echo ""
	@echo "  all             - Alias for combo-all"
	@echo "  help            - Show this help message"

cli:
	mvn clean verify --pl cli

core: domain
	mvn clean verify --pl core

domain:
	mvn clean verify --pl domain

ffi:
	mvn clean verify --pl ffi

jgit:
	mvn clean verify --pl jgit

infra-sql: check-docker
	mvn clean verify --pl infrastructure-sql

infra-arangodb: check-docker
	mvn clean verify --pl infrastructure-arangodb

vcs-indexer-ffi:
	mvn clean verify --pl integration-test/vcs-indexer -Dspring.profiles.active=gix

vcs-indexer-jgit:
	mvn clean verify --pl integration-test/vcs-indexer -Dspring.profiles.active=jgit

vcs-indexer: vcs-indexer-ffi vcs-indexer-jgit

combo-core-ffi:
	mvn clean verify --pl core,ffi

combo-core-jgit:
	mvn clean verify --pl core,jgit

combo-ffi-jgit:
	mvn clean verify --pl ffi,jgit

combo-core-infra-sql: check-docker
	mvn clean verify --pl core,infrastructure-sql

combo-core-infra-arangodb: check-docker
	mvn clean verify --pl core,infrastructure-arangodb

combo-all:
	mvn clean verify --pl core,ffi,jgit,integration-test/vcs-indexer -Dspring.profiles.active=ffi
	mvn clean verify --pl core,ffi,jgit,integration-test/vcs-indexer -Dspring.profiles.active=jgit

check-docker:
	@docker info > /dev/null 2>&1 || { echo "ERROR: Docker is not running or not reachable."; echo "       Please start Docker and try again."; exit 1; }
