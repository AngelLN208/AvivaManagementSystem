# syntax=docker/dockerfile:1

# Compilación reproducible con Maven y la misma versión de Java del proyecto.
FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /workspace

# Se separa el POM para que Render reutilice la caché de dependencias.
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline -Dmaven.test.skip=true

COPY src ./src
RUN mvn -B -ntp clean package -Dmaven.test.skip=true

# La imagen final contiene únicamente el runtime y el JAR ejecutable.
FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

ENV PORT=10000

COPY --from=build /workspace/target/appointmentsystem-0.0.1-SNAPSHOT.jar /app/app.jar

# Render enruta el tráfico al puerto indicado por PORT.
EXPOSE 10000

# Usuario numérico sin privilegios; el JAR copiado es de solo lectura.
USER 10001:10001

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
