setup:
	chmod +x ./gradlew
	./gradlew wrapper --gradle-version 8.13
	cd frontend && npm ci
	cd code && ./gradlew build installDist

install:
	./gradlew installDist

start:
	./gradlew run

build:
	./gradlew build

test:
	./gradlew test

clean:
	./gradlew clean

check-updates:
	./gradlew dependencyUpdates

.PHONY: setup install start build test clean check-updates
