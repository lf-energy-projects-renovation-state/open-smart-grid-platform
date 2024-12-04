FROM eclipse-temurin:17-jdk-alpine
RUN mkdir -p /var/log/osp/logs/ \
  && chmod 777 /var/log/osp/logs/
COPY target/*.war /app/
ENTRYPOINT ["java", \
    "-cp", \
    "/app/:/app/WEB-INF/lib-provided/*:/app/WEB-INF/lib/*", \
    "-Dspring.profiles.active=light_measurement_device", \
    "org.springframework.boot.loader.launch.WarLauncher"]
