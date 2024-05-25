ARG DEPENDENCY=extracted

FROM amazoncorretto:17-alpine-jdk AS builder
ARG DEPENDENCY
WORKDIR ${DEPENDENCY}
ARG JAR_FILE=target/*.jar
ADD ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM builder AS launcher
WORKDIR application
ARG DEPENDENCY
COPY --from=builder ${DEPENDENCY}/dependencies/ ./
COPY --from=builder ${DEPENDENCY}/spring-boot-loader/ ./
COPY --from=builder ${DEPENDENCY}/snapshot-dependencies/ ./
COPY --from=builder ${DEPENDENCY}/application/ ./
EXPOSE 8082
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]