FROM maven:3.9.9-eclipse-temurin-22 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:22-jdk AS runner

WORKDIR /app

COPY --from=builder /app/target/notification-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 4007
ENTRYPOINT ["java", "-jar", "app.jar"]
