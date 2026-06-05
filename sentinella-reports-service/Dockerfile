FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -B -DskipTests -pl sentinella-reports-service -am package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
RUN mkdir -p /data/reports && chown -R app:app /data/reports
WORKDIR /app
COPY --from=build /app/sentinella-reports-service/target/sentinella-reports-service-0.0.1-SNAPSHOT.jar app.jar
RUN chown app:app /app/app.jar
USER app
EXPOSE 8086
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
