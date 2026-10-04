FROM eclipse-temurin:21-jdk-alpine
COPY target/*.jar app.jar
COPY config-repo ./config-repo
ENTRYPOINT ["java", "-jar", "/app.jar"]
