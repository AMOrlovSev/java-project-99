# Build
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY gradlew ./
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

COPY src src
COPY config config

RUN chmod +x gradlew
RUN ./gradlew --no-daemon clean build -x test -x checkstyleMain -x checkstyleTest --stacktrace

# Run
FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]