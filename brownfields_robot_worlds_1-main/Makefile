

# Version detection
VERSION := $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
RELEASE_VERSION := $(subst -SNAPSHOT,,$(VERSION))
CLEAN_VERSION := $(shell echo $(VERSION) | sed 's/-SNAPSHOT//')

# Robot Worlds Makefile - Strictly Compliant Version
PROJECT_DIR := .
REFERENCE_SERVER := $(PROJECT_DIR)/libs/reference-server-"$(RELEASE_VERSION)".jar
IT2_REFERENCE_SERVER := $(PROJECT_DIR)/libs/reference-server-"$(RELEASE_VERSION)".jar -p 5050 -s 100 -o 15,5
SERVER_SOURCE := $(PROJECT_DIR)/src/main/java/za/co/wethinkcode/server/RobotWorldServer.java
PORT := 5000

.PHONY: all compile test test-reference test-local package release release-build clean

all: compile test package

#to run the ref-server: 'make local-server ARGS="-p 5000 -s 12 -o 3,4"'
local-server:
	java -jar target/robot-world-"$(RELEASE_VERSION)"-jar-with-dependencies.jar $(ARGS)

clean:
	@echo "Cleaning project..."
	@mvn clean

compile:
	@echo "Compiling project..."
	@mvn clean compile

test-unit: compile
	@echo "Running unit tests..."
	@mvn -Dtest=!LaunchRobotTests,!ServerStartCommandlineTest,!AcceptanceCommandLineTest,!RestoreCommandTest,!WorldDaoTest test

test-reference: compile
	@echo "=== Testing against Reference Implementation ==="
	@echo "Starting reference server..."
	@trap 'kill -9 $$SERVER_PID' EXIT; \
	java -jar $(REFERENCE_SERVER) & SERVER_PID=$$!; \
	sleep 5; \
	echo "Running acceptance tests..."; \
	mvn -Dtest=!RobotMovementTests,!ServerStartCommandlineTest,!LaunchRobotTests,!AcceptanceCommandLineTest,!RestoreCommandTest,!WorldDaoTest test -Dserver.url=http://localhost:$(PORT); \
	TEST_RESULT=$$?; \
	kill -9 $$SERVER_PID || echo "server processes already killed"; \
	exit $$TEST_RESULT

test-second-reference: compile
	@echo "=== Testing against Second Reference Implementation ==="
	@echo "Starting second reference server..."
	@trap 'kill -9 $$SERVER_PID' EXIT; \
	java -jar $(IT2_REFERENCE_SERVER) & SERVER_PID=$$!; \
	sleep 5; \
	echo "Running acceptance tests..."; \
	mvn test -Dtest=!RobotMovementTests,!ServerStartCommandlineTest,!LaunchRobotTests,!AcceptanceCommandLineTest,!RestoreCommandTest,!WorldDaoTest -Dserver.url=http://localhost:${PORT}
	TEST_RESULT=$$?; \
	kill -9 $$SERVER_PID || echo "server processes already killed"; \
	exit $$TEST_RESULT

test-local: compile
	@echo "=== Testing Against Your Implementation ==="
	@echo "Building and starting our server..."
	 mvn exec:java -Dexec.mainClass="za.co.wethinkcode.server.RobotWorldServer" -Dexec.args="-p 5000 -s 5 -o 3,7" & \
	 SERVER_PID=$$!; \
	 sleep 5; \
	 echo "Running acceptance tests..."; \
	 mvn -Dtest=!ServerStartCommandlineTest,!RestoreCommandTest,!WorldDaoTest test; \
	 TEST_RESULT=$$?; \
	 echo "=== killing server process... ==="; \
	 kill -9 $$SERVER_PID; \
	 exit $$TEST_RESULT \


test: test-second-reference test-unit test-reference test-local

package: compile
	@echo "Packaging application..."
	mvn clean package -DskipTests
	echo "JAR created: target/robot-world-$(VERSION).jar"


release-build:
	@echo "Preparing release build..."
	@sed -i 's/$(VERSION)/$(RELEASE_VERSION)/' pom.xml
	@echo "Version updated to $(RELEASE_VERSION)"

release: release-build test package
	@echo "Creating release tag..."
	mvn versions:set -DnewVersion=${CLEAN_VERSION}
	@git tag -a "v$(RELEASE_VERSION)" -m "Release v$(RELEASE_VERSION)"  # Added "v" prefix
	@git push origin "v$(RELEASE_VERSION)"  # Explicitly push the tag
	mvn versions:revert
	@echo "=== Release $(RELEASE_VERSION) successfully created ==="
	@echo "Release JAR: target/robot-world-$(RELEASE_VERSION).jar"

deploy:
	mvn clean
	mvn package -DskipTests
	sudo docker build --no-cache -t robot-worlds-server:latest .
	sudo docker run -p 5050:5050 robot-worlds-server:latest

run-server:
	mvn exec:java -Dexec.mainClass="za.co.wethinkcode.server.RobotWorldServer" -Dexec.args="-p 5050 -s 5 -o 3,7"

docker-build: package
	docker build -t gitlab.wethinkco.de:5050/kamogotjhb024/brownfields_robot_worlds_1 .

docker-test: docker-run
	sleep 15; \
	sudo docker exec -it sad_bose /bin/bash
	mvn test -Dtest.server.url=http://localhost:5050

docker-push: docker-build
	docker login -u sett -p mJ3AQ9iqvDc6-pi85kz_ gitlab.wethinkco.de:5050/kamogotjhb024/brownfields_robot_worlds_1
	docker push gitlab.wethinkco.de:5050/kamogotjhb024/brownfields_robot_worlds_1

docker-pull:
	sudo docker login -u sett -p mJ3AQ9iqvDc6-pi85kz_ gitlab.wethinkco.de:5050/kamogotjhb024/brownfields_robot_worlds_1
	sudo docker pull gitlab.wethinkco.de:5050/kamogotjhb024/brownfields_robot_worlds_1

docker-run: docker-pull
	sudo docker run -p 5050:5050 gitlab.wethinkco.de:5050/kamogotjhb024/brownfields_robot_worlds_1

# Version detection
VERSION := $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
RELEASE_VERSION := $(subst -SNAPSHOT,,$(VERSION))

# Function to bump versions
define bump_version
	@echo "Current version: $(RELEASE_VERSION)"
	@python3 scripts/bump_version.py $(RELEASE_VERSION) $(1) > VERSION_TMP
	@NEW_VERSION=$$(cat VERSION_TMP) && \
		sed -i "0,/<version>.*<\/version>/s//<version>$${NEW_VERSION}<\/version>/" pom.xml && \
		rm VERSION_TMP && \
		echo "Updated version to $${NEW_VERSION}"
endef

push_patch: package
	$(call bump_version,patch)
	@git commit -am "Bump patch version"
	@git tag -a "v$(shell sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p' pom.xml | head -n1)" -m "Patch release"
	@git push --tags

push_minor: package
	$(call bump_version,minor)
	@git commit -am "Bump minor version"
	@git tag -a "v$(shell sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p' pom.xml | head -n1)" -m "Minor release"
	@git push --tags

push_major: package
	$(call bump_version,major)
	@git commit -am "Bump major version"
	@git tag -a "v$(shell sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p' pom.xml | head -n1)" -m "Major release"
	@git push --tags
