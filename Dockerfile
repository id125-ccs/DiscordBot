FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Ensure JAVA_HOME is set
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="$JAVA_HOME/bin:$PATH"

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:21-jre-alpine AS discord-bot

# Copy build to /app
WORKDIR /app
COPY ./config ./config
COPY --from=builder /build/build/libs/app.jar app.jar

# Run .jar file
ENTRYPOINT ["java", "-jar", "app.jar"]