# Build: docker build -t micro-savings .
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

COPY target/micro-savings-1.0.0.jar app.jar

EXPOSE 5477

ENTRYPOINT ["java", "-jar", "app.jar"]
