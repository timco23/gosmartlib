# STAGE 1: Build de applicatie met JDK 21 en Maven
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Kopieer pom en source code
COPY pom.xml .
COPY src ./src

# Build de jar
RUN mvn clean package -DskipTests

# STAGE 2: Run de jar in een lichte JDK image
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Kopieer de jar van de build stage
COPY --from=build /app/target/*.jar app.jar

# Expose de poort
EXPOSE 8080

# Start de Spring Boot applicatie
ENTRYPOINT ["java","-jar","app.jar"]
