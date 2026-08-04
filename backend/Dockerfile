# ============================================================================
# TICKET-ADV146 — Spring Boot Dockerfile (multi-stage Maven -> JRE 25 slim)
#
# Stage 1: Build the JAR using Temurin JDK 25.
# Stage 2: Copy only the JAR into a lightweight JRE image.
# ============================================================================

FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY lib lib

# Make Maven wrapper executable and convert CRLF to LF for Linux
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Install custom local starter dependency
RUN ./mvnw -B install:install-file \
    -Dfile=lib/recon-audit-starter-1.0.0.jar \
    -DpomFile=lib/recon-audit-starter-1.0.0.pom

# Download dependencies
RUN ./mvnw -q -B dependency:go-offline || true

# Copy source code
COPY src src

# Build the application
RUN ./mvnw -q -B -DskipTests package && \
    mv target/reconx-*.jar app.jar

# ----------------------------------------------------------------------------

FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S reconx && adduser -S reconx -G reconx

USER reconx

WORKDIR /app

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]