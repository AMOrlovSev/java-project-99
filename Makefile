.DEFAULT_GOAL := build-run

setup:
	./gradlew wrapper --gradle-version 8.10

clean:
	./gradlew clean

build:
	./gradlew clean build

install:
	./gradlew clean install

run-dist:
	./build/install/app/bin/app

run:
	./gradlew run

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

lint:
	./gradlew checkstyleMain

update-deps:
	./gradlew refreshVersions
	# ./gradlew dependencyUpdates -Drevision=release


build-run: build run

.PHONY: build