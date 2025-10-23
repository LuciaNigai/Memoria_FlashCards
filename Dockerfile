# Stage 1: Build the application
FROM maven:3.9.8-amazoncorretto-21 AS builder

# Set the working directory
WORKDIR /app

# Copy the Maven pom.xml and install dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the project and build the JAR
COPY src /app/src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:21

WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /app/target/memoria-gateway-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the port and run the application
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]