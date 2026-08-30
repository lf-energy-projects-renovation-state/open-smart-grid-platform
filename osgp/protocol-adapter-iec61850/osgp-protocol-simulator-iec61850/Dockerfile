FROM eclipse-temurin:21.0.8_9-jdk-alpine
COPY target/*.war /app/app.war
RUN chmod 777 /app/
ENTRYPOINT ["java", "-jar", "/app/app.war"]