# Build stage
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first (better layer caching)
RUN mvn dependency:go-offline -B
COPY src/ /app/src/
# Build the application with optimized settings
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add non-root user
RUN addgroup -S spring && \
    adduser -S spring -G spring && \
    chown -R spring:spring /app

# Install monitoring tools
RUN apk --no-cache add curl jq tzdata && \
    cp /usr/share/zoneinfo/Europe/Paris /etc/localtime && \
    echo "Europe/Paris" > /etc/timezone && \
    apk del tzdata

# Copy the built JAR from the build stage
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

USER spring:spring

# Expose application and JMX ports
EXPOSE 8080
EXPOSE 9010

# Set JVM performance options
ENTRYPOINT ["java", \
    "-Xms256m", "-Xmx512m", \
    "-XX:+UseG1GC", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:HeapDumpPath=/tmp", \
    "-XX:+DisableExplicitGC", \
    "-XX:+ParallelRefProcEnabled", \
    "-XX:MaxGCPauseMillis=200", \
    "-jar", "app.jar" \
]
