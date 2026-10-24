# ============================
# ÉTAPE 1 : BUILD
# ============================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# D'abord le pom.xml seul, et les dépendances qu'il liste
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Puis le code, et le jar
COPY src ./src
RUN mvn clean package -DskipTests


# ============================
# ÉTAPE 2 : RUN
# ============================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
