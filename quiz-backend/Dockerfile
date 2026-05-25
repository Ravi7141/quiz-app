# Stage 1 — Build
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

# Stage 2 — Run
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Install compilers (Python, Node.js, C++, Java already in base image)
RUN apt-get update && \
    apt-get install -y python3 python-is-python3 nodejs g++ && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# JVM memory limits for Northflank free tier (512MB container)
ENTRYPOINT ["java", \
  "-Xms128m", \
  "-Xmx256m", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=60.0", \
  "-jar", "app.jar"]