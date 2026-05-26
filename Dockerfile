# syntax=docker/dockerfile:1.6

FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q dependency:go-offline

COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests package && \
    cp target/testpire-*.jar /app/app.jar


FROM eclipse-temurin:25-jre-noble AS runtime
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --home /app app
USER app

COPY --from=build /app/app.jar /app/app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "exec java --add-exports java.base/sun.security.x509=ALL-UNNAMED $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager -jar /app/app.jar"]
