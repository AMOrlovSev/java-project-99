FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY gradlew .
RUN chmod +x gradlew

COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN ./gradlew --no-daemon dependencies

COPY src src
COPY config config

RUN ./gradlew --no-daemon bootJar

ENV JAVA_OPTS="-Xmx512M -Xms512M"
EXPOSE 7070

CMD ["java", "-jar", "build/libs/app-0.0.1-SNAPSHOT.jar"]