
# Etapa 1: Baixar dependências antes de copiar o código
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Copia apenas o pom.xml e baixa as dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Agora sim, copia o restante do código
COPY src ./src

# Compila o projeto
RUN mvn clean package -DskipTests

# Etapa 2: Gerar a imagem final
FROM openjdk:17-jdk-slim
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
