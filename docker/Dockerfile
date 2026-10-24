# ============================
# ÉTAPE 1 : BUILD
# ============================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copie du projet et build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests


# ============================
# ÉTAPE 2 : RUN
# ============================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Exposition du port
EXPOSE 8080

# Lancement de l’application
ENTRYPOINT ["java", "-jar", "app.jar"]
