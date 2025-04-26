FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace/app

COPY pom.xml .
# Aproveitar cache de dependências Maven
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline

COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /workspace/app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
