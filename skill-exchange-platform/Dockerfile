FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -r -u 1001 skillexchange
COPY --from=build /app/target/skill-exchange-platform-1.0.0.jar app.jar
USER skillexchange
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
