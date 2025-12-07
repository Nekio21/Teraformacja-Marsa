FROM maven:3.8.5-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17.0.17_10-jdk-ubi10-minimal
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/src/main/resources/static/ /app/static/
COPY --from=builder /app/src/main/resources/templates/ /app/templates/
CMD ["java", "-jar", "app.jar"]

