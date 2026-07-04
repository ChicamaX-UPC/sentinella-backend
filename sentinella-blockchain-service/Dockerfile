FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -B -DskipTests -pl sentinella-blockchain-service -am package

FROM eclipse-temurin:21-jre
RUN groupadd -r app && useradd -r -g app app
WORKDIR /app
COPY --from=build /app/sentinella-blockchain-service/target/sentinella-blockchain-service-0.0.1-SNAPSHOT.jar app.jar
RUN chown app:app /app/app.jar
USER app
EXPOSE 8093
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
