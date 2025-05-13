# Apostila - Infraestrutura de Serviço (Ambiente Dev com Casos)

## Introdução
Bem-vindo à apostila prática sobre a infraestrutura de serviço no ambiente de desenvolvimento. Esta apostila guia os alunos na configuração e uso de um sistema distribuído com `ui-service` como front-end simples, `auth-service`, `commerce-service`, um banco de dados MySQL compartilhado e uma interface `phpmyadmin` para visualização.

## Descrição dos Componentes
- Web Browser Client: Interface do usuário que acessa o sistema via navegador.
- ui-service (Port 8080): Microserviço mais simples, contendo todo o front-end. Opera na porta 8080 e redireciona solicitações para `auth-service` e `commerce-service`.
- auth-service (Port 8082): Serviço de autenticação que valida usuários usando o banco MySQL.
- commerce-service (Port 8081): Serviço de comércio que processa operações de negócios usando o banco MySQL.
- MySQL: Banco de dados compartilhado (`commercedb`) para autenticação e comércio, acessado na porta 3306.
- phpMyAdmin (Port 8085): Interface web para gerenciar e visualizar o banco de dados MySQL.

## Fluxo de Dados
O fluxo de dados no sistema segue esta sequência:
1. O Web Browser Client envia solicitações ao ui-service.
2. O ui-service redireciona solicitações de autenticação para o auth-service.
3. O auth-service valida os usuários e acessa/atualiza o MySQL.
4. Para operações de comércio, o ui-service envia requisições ao commerce-service.
5. O commerce-service processa as operações e acessa/atualiza o MySQL.
6. O phpMyAdmin permite monitoramento e gestão do banco de dados.

## Instruções de Configuração
Para configurar o ambiente de desenvolvimento:
- Instale o Docker e o Docker Compose.
- Crie um arquivo `docker-compose.dev.yml` com o seguinte conteúdo:
  ```
  version: '3.8'
  services:
    mysql:
      image: mysql:8.0
      container_name: mysql
      environment:
        MYSQL_ROOT_PASSWORD: root
        MYSQL_DATABASE: commercedb
      ports:
        - "3306:3306"
      volumes:
        - mysql_data:/var/lib/mysql
      networks:
        - dev-network
    phpmyadmin:
      image: phpmyadmin/phpmyadmin
      container_name: phpmyadmin
      ports:
        - "8085:80"
      environment:
        PMA_HOST: mysql
      depends_on:
        - mysql
      networks:
        - dev-network
    auth-service:
      build:
        context: ./auth-service
        dockerfile: Dockerfile.dev
      container_name: auth-service-dev
      ports:
        - "8082:8082"
      environment:
        - SPRING_PROFILES_ACTIVE=dev
        - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/commercedb
        - SPRING_DATASOURCE_USERNAME=root
        - SPRING_DATASOURCE_PASSWORD=root
      volumes:
        - ./auth-service:/app
        - ~/.m2:/root/.m2
      working_dir: /app
      command: ["./mvnw", "spring-boot:run"]
      depends_on:
        - mysql
      networks:
        - dev-network
    commerce-service:
      build:
        context: ./commerce-service
        dockerfile: Dockerfile.dev
      container_name: commerce-service-dev
      ports:
        - "8081:8081"
      environment:
        - SPRING_PROFILES_ACTIVE=dev
        - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/commercedb
        - SPRING_DATASOURCE_USERNAME=root
        - SPRING_DATASOURCE_PASSWORD=root
        - AUTH_SERVICE_URL=http://auth-service:8082
      volumes:
        - ./commerce-service:/app
        - ~/.m2:/root/.m2
      working_dir: /app
      command: ["./mvnw", "spring-boot:run"]
      depends_on:
        - mysql
        - auth-service
      networks:
        - dev-network
    ui-service:
      build:
        context: ./ui-service
        dockerfile: Dockerfile.dev
      container_name: ui-service-dev
      ports:
        - "8080:8080"
      environment:
        - SPRING_PROFILES_ACTIVE=dev
        - COMMERCE_SERVICE_URL=http://commerce-service:8081
        - AUTH_SERVICE_URL=http://auth-service:8082
      volumes:
        - ./ui-service:/app
        - ~/.m2:/root/.m2
      working_dir: /app
      command: ["./mvnw", "spring-boot:run"]
      depends_on:
        - commerce-service
        - auth-service
      networks:
        - dev-network
  volumes:
    mysql_data:
  networks:
    dev-network:
      driver: bridge
  ```
- Execute `docker-compose -f docker-compose.dev.yml up` para iniciar os serviços.
- Acesse o phpMyAdmin em `http://localhost:8085` para gerenciar o banco.
- Desenvolva os microserviços nas pastas correspondentes (`auth-service`, `commerce-service`, `ui-service`) e ajuste os `Dockerfile.dev` conforme necessário.

## Casos Específicos de Configuração com Docker Compose
Abaixo estão os arquivos `docker-compose-CASO` para diferentes combinações de serviços:

### Caso 1: auth-service com commerce-service
Crie o arquivo `docker-compose-CASO1.yml`:
```
version: '3.8'
services:
  auth-service:
    build:
      context: ./auth-service
      dockerfile: Dockerfile.dev
    container_name: auth-service-case1
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    volumes:
      - ./auth-service:/app
      - ~/.m2:/root/.m2
    working_dir: /app
    command: ["./mvnw", "spring-boot:run"]
    networks:
      - case1-network
  commerce-service:
    build:
      context: ./commerce-service
      dockerfile: Dockerfile.dev
    container_name: commerce-service-case1
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - AUTH_SERVICE_URL=http://auth-service:8082
    volumes:
      - ./commerce-service:/app
      - ~/.m2:/root/.m2
    working_dir: /app
    command: ["./mvnw", "spring-boot:run"]
    depends_on:
      - auth-service
    networks:
      - case1-network
networks:
  case1-network:
    driver: bridge
```
Execute com `docker-compose -f docker-compose-CASO1.yml up`.

### Caso 2: ui-service com commerce-service
Crie o arquivo `docker-compose-CASO2.yml`:
```
version: '3.8'
services:
  ui-service:
    build:
      context: ./ui-service
      dockerfile: Dockerfile.dev
    container_name: ui-service-case2
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - COMMERCE_SERVICE_URL=http://commerce-service:8081
    volumes:
      - ./ui-service:/app
      - ~/.m2:/root/.m2
    working_dir: /app
    command: ["./mvnw", "spring-boot:run"]
    networks:
      - case2-network
  commerce-service:
    build:
      context: ./commerce-service
      dockerfile: Dockerfile.dev
    container_name: commerce-service-case2
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    volumes:
      - ./commerce-service:/app
      - ~/.m2:/root/.m2
    working_dir: /app
    command: ["./mvnw", "spring-boot:run"]
    networks:
      - case2-network
networks:
  case2-network:
    driver: bridge
```
Execute com `docker-compose -f docker-compose-CASO2.yml up`.

### Caso 3: auth-service com ui-service
Crie o arquivo `docker-compose-CASO3.yml`:
```
version: '3.8'
services:
  auth-service:
    build:
      context: ./auth-service
      dockerfile: Dockerfile.dev
    container_name: auth-service-case3
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    volumes:
      - ./auth-service:/app
      - ~/.m2:/root/.m2
    working_dir: /app
    command: ["./mvnw", "spring-boot:run"]
    networks:
      - case3-network
  ui-service:
    build:
      context: ./ui-service
      dockerfile: Dockerfile.dev
    container_name: ui-service-case3
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - AUTH_SERVICE_URL=http://auth-service:8082
    volumes:
      - ./ui-service:/app
      - ~/.m2:/root/.m2
    working_dir: /app
    command: ["./mvnw", "spring-boot:run"]
    depends_on:
      - auth-service
    networks:
      - case3-network
networks:
  case3-network:
    driver: bridge
```
Execute com `docker-compose -f docker-compose-CASO3.yml up`.

### Caso 4: commerce-service com o mysql
Crie o arquivo `docker-compose-CASO4.yml`:
```
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: mysql-case4
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: commercedb
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - case4-network
  commerce-service:
    build:
      context: ./commerce-service
      dockerfile: Dockerfile.dev
    container_name: commerce-service-case4
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/commercedb
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=root
    volumes:
      - ./commerce-service:/app
      - ~/.m2:/root/.m2
    working_dir: /app
    command: ["./mvnw", "spring-boot:run"]
    depends_on:
      - mysql
    networks:
      - case4-network
volumes:
  mysql_data:
networks:
  case4-network:
    driver: bridge
```
Execute com `docker-compose -f docker-compose-CASO4.yml up`.

### Caso 5: auth-service com o mysql
Crie o arquivo `docker-compose-CASO5.yml`:
```
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: mysql-case5
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: authdb
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - case5-network
  auth-service:
    build:
      context: ./auth-service
      dockerfile: Dockerfile.dev
    container_name: auth-service-case5
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/authdb
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=root
    volumes:
      - ./auth-service:/app
      - ~/.m2:/root/.m2
    working_dir: /app
    command: ["./mvnw", "spring-boot:run"]
    depends_on:
      - mysql
    networks:
      - case5-network
volumes:
  mysql_data:
networks:
  case5-network:
    driver: bridge
```
Execute com `docker-compose -f docker-compose-CASO5.yml up`.

## Conclusão
Esta apostila detalha a configuração do ambiente de desenvolvimento com MySQL e phpMyAdmin, além de fornecer configurações específicas para diferentes combinações de serviços. Explore os serviços, teste os fluxos de dados e contribua com melhorias no repositório GitHub.