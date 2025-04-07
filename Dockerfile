FROM maven:3.8.5-openjdk-17 AS builder
WORKDIR /app

# Najpierw kopiujemy plik pom.xml i pobieramy zależności, żeby nie robić tego za każdym razem
COPY pom.xml .
RUN mvn dependency:go-offline

# Dopiero potem kopiujemy resztę kodu
COPY src ./src
RUN mvn clean package -DskipTests

# Tworzymy finalny obraz
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/src/main/resources/static/ /app/static/
COPY --from=builder /app/src/main/resources/templates/ /app/templates/
CMD ["java", "-jar", "app.jar"]

