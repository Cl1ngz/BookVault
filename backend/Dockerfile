# ETAP 1: Budowanie aplikacji (Build)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Kopiujemy plik konfiguracyjny Mavena, aby pobrać zależności (cache-owanie)
COPY pom.xml .
RUN mvn dependency:go-offline

# Kopiujemy kod źródłowy i budujemy paczkę JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ETAP 2: Uruchamianie aplikacji (Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Tworzymy użytkownika systemowego, aby nie uruchamiać aplikacji jako root (bezpieczeństwo)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Kopiujemy tylko gotowy plik JAR z poprzedniego etapu
COPY --from=build /app/target/*.jar app.jar

# Port, na którym działa Spring Boot
EXPOSE 8080

# Komenda startowa
ENTRYPOINT ["java", "-jar", "app.jar"]
