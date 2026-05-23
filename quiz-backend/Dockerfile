FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .

# Make wrapper executable and download dependencies
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk-jammy

# Install compilers (Python, Node.js, C++)
RUN apt-get update && \
    apt-get install -y python3 python-is-python3 nodejs g++ && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render exposes the port dynamically, but defaults to 8080 for Spring Boot
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
