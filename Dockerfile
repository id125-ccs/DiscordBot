FROM eclipse-temurin:23-jre-alpine AS discord-bot

# Copy build to /app
WORKDIR /app
COPY build/libs/app.jar app.jar

# Run .jar file
ENTRYPOINT ["java", "-jar", "app.jar"]