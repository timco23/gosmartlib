# STAGE 1: Build met Maven + JDK 21
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Eerst pom voor betere layer cache
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# Daarna source
COPY src ./src

# Build jar
RUN mvn -B clean package -DskipTests

# STAGE 2: Run met JRE (kleiner dan JDK)
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]