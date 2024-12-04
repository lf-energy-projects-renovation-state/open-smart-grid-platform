FROM eclipse-temurin:17-jdk-alpine
RUN addgroup -S iec61850-simulator && adduser -S iec61850-simulator -G iec61850-simulator \
 && mkdir -p /var/log/osp/logs/ \
 && chown iec61850-simulator:iec61850-simulator /var/log/osp/logs/ \
 && mkdir -p /data/osgp/ \
 && chown iec61850-simulator:iec61850-simulator /data/osgp/
USER iec61850-simulator:iec61850-simulator
COPY target/*.war /app/
ENTRYPOINT ["java", \
    "-cp", \
    "/app/:/app/WEB-INF/lib-provided/*:/app/WEB-INF/lib/*", \
    "-Dspring.config.name=light-measurement-rtu", \
    "-Drtu.icd=/data/osgp/FlexOVL_540_171101_2_out-unbuffered.icd", \
    "org.springframework.boot.loader.launch.WarLauncher"]
