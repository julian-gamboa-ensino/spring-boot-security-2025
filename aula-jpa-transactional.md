# Aula Prática: Lógica de Negócios e Gerenciamento de Concorrência

Bem-vindo ao repositório da aula prática sobre **lógica de negócios** e **gerenciamento de concorrência** no projeto didático de uma concessionária de automóveis. Este projeto é baseado em microsserviços construídos com **Spring Boot**, **Java**, e **MySQL**, e implementa funcionalidades como cálculo de preços de veículos, gerenciamento de carrinhos com validade de 1 minuto, reserva de veículos, e controle de concorrência para evitar conflitos (ex.: dois usuários reservando o mesmo veículo).

## Objetivos da Aula

- Compreender o conceito de **lógica de negócios** e como ela traduz os requisitos do cliente em código.
- Explorar a implementação de regras de negócios no microsserviço `commerce-service` (cálculo de preços, carrinhos, reservas).
- Aprender sobre **gerenciamento de concorrência** usando `@Transactional` e bloqueio pessimista (`findByIdWithLock`) para evitar conflitos.
- Entender o uso da anotação `@Transactional` em operações de banco de dados.
- Realizar uma atividade prática para implementar uma nova regra de negócios e testar concorrência.

## Contexto do Projeto

O projeto simula o sistema de uma concessionária que gerencia vendas de veículos online (clientes) e físicas (vendedores). Ele é composto por três microsserviços:

- **`auth-service`**: Gerencia autenticação de usuários e emissão de tokens JWT.
- **`commerce-service`**: Contém a lógica de negócios para veículos, carrinhos, e vendas.
- **`ui-service`**: Fornece a interface web para interação com usuários.

### Requisitos Funcionais
- **Veículos**:
  - Preço base com acréscimo por cor (prata: +R$2.000, preta: +R$1.000, branca: sem acréscimo).
  - Descontos: 20% para Pessoa Jurídica (PJ), 30% para Pessoa Física PCD.
- **Carrinhos**:
  - Validade de 1 minuto; veículos no carrinho ficam indisponíveis.
  - Carrinhos expirados são limpos automaticamente.
  - Apenas um carrinho ativo por usuário.
- **Vendas**:
  - Podem ser online (clientes) ou físicas (vendedores).
  - Veículos vendidos são marcados como indisponíveis permanentemente.
- **Segurança**:
  - APIs protegidas por tokens JWT.
  - Perfis: `ROLE_USER` (clientes), `ROLE_VENDOR` (vendedores), `ROLE_ADMIN`.

### Requisitos Não Funcionais
- **Concorrência**: Evitar conflitos (ex.: dois usuários reservando o mesmo veículo).
- **Segurança**: Criptografia de dados sensíveis (ex.: CPF, senha).
- **Monitoramento**: Métricas de desempenho via `MetricsService`.

## Estrutura do Repositório

- **`commerce-service/`**: Código do microsserviço com lógica de negócios e concorrência.
  - `src/main/java/com/example/commerce/service/`: Serviços (`VehicleService`, `CartService`).
  - `src/main/java/com/example/commerce/repository/`: Repositórios (`VehicleRepository`, `CartRepository`).
- **`auth-service/`**: Autenticação e validação de tokens JWT.
- **`ui-service/`**: Interface web (não incluída na aula prática).
- **`README.md`**: Este arquivo com instruções e documentação.

## Lógica de Negócios: Exemplos de Código

A lógica de negócios está implementada no `commerce-service`, nos serviços `VehicleService` e `CartService`. Abaixo, apresentamos trechos de código que implementam os requisitos do cliente, com explicações detalhadas sobre o uso de `@Transactional`.

### 1. Cálculo de Preço do Veículo (`VehicleService`)

**Requisito**:
- Calcular o preço final de um veículo com base na cor e tipo de cliente:
  - Cor prata: +R$2.000, cor preta: +R$1.000.
  - Desconto: 20% para PJ, 30% para PCD.

**Código**:

```java
package com.example.commerce.service;

import com.example.commerce.model.Vehicle;
import com.example.commerce.model.VehicleColor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public BigDecimal calculatePrice(Long vehicleId, String customerType) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

        BigDecimal basePrice = vehicle.getPreco();
        VehicleColor color = vehicle.getColor();

        // Acréscimo por cor
        BigDecimal colorSurcharge = BigDecimal.ZERO;
        if (color == VehicleColor.PRATA) {
            colorSurcharge = new BigDecimal("2000");
        } else if (color == VehicleColor.PRETA) {
            colorSurcharge = new BigDecimal("1000");
        }

        BigDecimal priceWithColor = basePrice.add(colorSurcharge);

        // Desconto por tipo de cliente
        BigDecimal discountMultiplier = BigDecimal.ONE;
        if ("PJ".equals(customerType)) {
            discountMultiplier = new BigDecimal("0.8"); // 20% desconto
        } else if ("PCD".equals(customerType)) {
            discountMultiplier = new BigDecimal("0.7"); // 30% desconto
        }

        return priceWithColor.multiply(discountMultiplier);
    }
}
```

**Explicação**:
- Este método não usa `@Transactional` porque é uma operação de leitura (busca o veículo e faz cálculos sem modificar o banco).
- A busca no banco (`findById`) ocorre fora de uma transação explícita, mas o Spring gerencia a conexão internamente.
- **Por que não usa `@Transactional`?** Não há necessidade de atomicidade, pois não há múltiplas operações no banco que precisem ser agrupadas.

### 2. Gerenciamento do Carrinho (`CartService`)

**Requisito**:
- Carrinhos têm validade de 1 minuto.
- Veículos no carrinho ficam indisponíveis.
- Carrinhos expirados são limpos automaticamente.

**Código**:

```java
package com.example.commerce.service;

import com.example.commerce.model.*;
import com.example.commerce.repository.CartRepository;
import com.example.commerce.repository.VehicleRepository;
import com.example.commerce.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final VehicleRepository vehicleRepository;
    private static final long CART_TIMEOUT_MINUTES = 1;

    @Transactional
    public Cart criarCarrinho(String userId) {
        cartRepository.findByUserIdAndFinalizadoFalse(userId)
            .ifPresent(cart -> {
                throw new RuntimeException("Usuário já possui um carrinho ativo");
            });

        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }

    @Transactional
    public void addToCart(Long vehicleId, String userId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
            .orElseThrow(() -> new BusinessException("Veículo não encontrado"));

        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
            .orElseGet(() -> createNewCart(userId));

        cart.getVehicles().add(vehicle);
        cart.setExpirationTime(LocalDateTime.now().plusMinutes(1));
        cartRepository.save(cart);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanExpiredCarts() {
        List<Cart> expiredCarts = cartRepository.findByStatusAndExpirationTimeBefore(
            CartStatus.ACTIVE, 
            LocalDateTime.now()
        );

        for (Cart cart : expiredCarts) {
            cart.setStatus(CartStatus.EXPIRED);
            cart.getVehicles().clear();
            cartRepository.save(cart);
        }
    }

    private Cart createNewCart(String userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setExpirationTime(LocalDateTime.now().plusMinutes(1));
        return cartRepository.save(cart);
    }
}
```

**Explicação do Uso de `@Transactional`**:

- **`criarCarrinho`**:
  - **Propósito**: Cria um novo carrinho para um usuário, garantindo que não exista um carrinho ativo.
  - **Operações no Banco**:
    1. Busca por carrinhos ativos (`findByUserIdAndFinalizadoFalse`).
    2. Salva o novo carrinho (`save`).
  - **Por que `@Transactional`?**
    - Garante que a verificação de carrinho ativo e a criação do novo carrinho sejam atômicas.
    - Evita que dois carrinhos sejam criados simultaneamente para o mesmo usuário devido a concorrência (ex.: duas requisições simultâneas).
    - Se a verificação falhar (carrinho ativo encontrado), a transação não salva nada, mantendo o banco consistente.
  - **Cenário de Concorrência**: Sem `@Transactional`, duas requisições simultâneas poderiam não ver o carrinho ativo uma da outra, criando múltiplos carrinhos.

- **`addToCart`**:
  - **Propósito**: Adiciona um veículo a um carrinho ativo ou cria um novo carrinho.
  - **Operações no Banco**:
    1. Busca o veículo (`findById`).
    2. Busca ou cria um carrinho (`findByUserIdAndStatus` ou `createNewCart`).
    3. Atualiza o carrinho com o veículo e a validade (`save`).
  - **Por que `@Transactional`?**
    - Agrupa as operações de busca, criação, e atualização em uma única transação.
    - Garante que o veículo seja adicionado ao carrinho e a validade definida sem interrupções.
    - Evita inconsistências, como adicionar o mesmo veículo a dois carrinhos devido a requisições simultâneas.
  - **Nota**: Este método não usa bloqueio pessimista, mas `@Transactional` ajuda a manter consistência em cenários de baixa concorrência.

- **`cleanExpiredCarts`**:
  - **Propósito**: Limpa carrinhos expirados a cada minuto, liberando veículos.
  - **Operações no Banco**:
    1. Busca carrinhos ativos expirados (`findByStatusAndExpirationTimeBefore`).
    2. Atualiza o status e limpa veículos de cada carrinho (`save`).
  - **Por que `@Transactional`?**
    - Garante que todas as atualizações em carrinhos expirados sejam aplicadas atomicamente.
    - Evita que um carrinho seja parcialmente atualizado (ex.: status alterado, mas veículos não limpos) se ocorrer um erro.
    - Permite que a limpeza ocorra em lote sem deixar o banco em estado inconsistente.
  - **Cenário de Concorrência**: Como é um processo agendado (`@Scheduled`), `@Transactional` protege contra conflitos com outras transações (ex.: um usuário tentando usar um carrinho enquanto ele é expirado).

## Gerenciamento de Concorrência

O projeto usa **bloqueio pessimista** e transações para evitar conflitos, como dois usuários reservando o mesmo veículo simultaneamente.

### 3. Reserva de Veículos com Bloqueio Pessimista (`VehicleService`)

**Requisito**:
- Veículos no carrinho ficam indisponíveis por 1 minuto.
- Evitar que dois usuários reservem o mesmo veículo ao mesmo tempo.

**Código**:

```java
package com.example.commerce.service;

import com.example.commerce.dto.VehicleDTO;
import com.example.commerce.exception.BusinessException;
import com.example.commerce.exception.ResourceNotFoundException;
import com.example.commerce.mapper.VehicleMapper;
import com.example.commerce.model.Vehicle;
import com.example.commerce.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleMapper vehicleMapper;
    private final CartService cartService;

    @Transactional
    public VehicleDTO reservarVeiculo(Long vehicleId, String userId) {
        Vehicle vehicle = vehicleRepository.findByIdWithLock(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));
        
        if (!vehicle.isDisponivel()) {
            throw new BusinessException("Veículo não está disponível");
        }

        if (cartService.isVehicleInActiveCart(vehicleId)) {
            throw new BusinessException("Veículo já está em um carrinho ativo");
        }

        vehicle.setDisponivel(false);
        vehicleRepository.save(vehicle);
        
        cartService.addVehicleToCart(vehicleId, userId);
        return vehicleMapper.toDTO(vehicle);
    }

    @Transactional
    public VehicleDTO liberarVeiculo(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findByIdWithLock(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

        if (vehicle.isVendido()) {
            throw new BusinessException("Veículo já foi vendido");
        }

        vehicle.setDisponivel(true);
        vehicleRepository.save(vehicle);

        return vehicleMapper.toDTO(vehicle);
    }

    @Transactional
    public VehicleDTO marcarComoVendido(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findByIdWithLock(vehicleId)
            .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

        if (vehicle.isVendido()) {
            throw new BusinessException("Veículo já foi vendido");
        }

        vehicle.setVendido(true);
        vehicle.setDisponivel(false);
        vehicleRepository.save(vehicle);

        return vehicleMapper.toDTO(vehicle);
    }
}
```

**Código do Repositório** (`VehicleRepository`):

```java
package com.example.commerce.repository;

import com.example.commerce.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vehicle v WHERE v.id = :id")
    Optional<Vehicle> findByIdWithLock(Long id);
}
```

**Explicação do Uso de `@Transactional`**:

- **`reservarVeiculo`**:
  - **Propósito**: Reserva um veículo, marcando-o como indisponível e adicionando-o a um carrinho.
  - **Operações no Banco**:
    1. Busca o veículo com bloqueio pessimista (`findByIdWithLock`).
    2. Verifica disponibilidade (`isDisponivel`) e carrinhos ativos (`isVehicleInActiveCart`).
    3. Marca como indisponível (`setDisponivel(false)`).
    4. Salva o veículo (`save`).
    5. Adiciona ao carrinho (`addVehicleToCart`).
  - **Por que `@Transactional`?**
    - Garante que todas as operações sejam atômicas, evitando que o veículo seja reservado parcialmente (ex.: marcado como indisponível, mas não adicionado ao carrinho).
    - Permite o uso de bloqueio pessimista (`PESSIMISTIC_WRITE`), que só funciona dentro de uma transação.
    - Protege contra concorrência, garantindo que apenas uma transação reserve o veículo por vez.
  - **Cenário de Concorrência**: Dois usuários tentando reservar o mesmo veículo. O bloqueio pessimista e `@Transactional` garantem que apenas um consiga, enquanto o outro recebe um erro.

- **`liberarVeiculo`**:
  - **Propósito**: Libera um veículo reservado, tornando-o disponível novamente.
  - **Operações no Banco**:
    1. Busca o veículo com bloqueio pessimista (`findByIdWithLock`).
    2. Verifica se foi vendido (`isVendido`).
    3. Marca como disponível (`setDisponivel(true)`).
    4. Salva o veículo (`save`).
  - **Por que `@Transactional`?**
    - Garante atomicidade entre a verificação e a atualização do estado do veículo.
    - Suporta o bloqueio pessimista, impedindo que outras transações (ex.: outra tentativa de liberar ou reservar) acessem o veículo durante a operação.
    - Evita inconsistências, como liberar um veículo que acabou de ser vendido.
  - **Cenário de Concorrência**: Um usuário liberando um veículo enquanto outro tenta reservá-lo. O bloqueio e `@Transactional` garantem exclusividade.

- **`marcarComoVendido`**:
  - **Propósito**: Marca um veículo como vendido, tornando-o indisponível permanentemente.
  - **Operações no Banco**:
    1. Busca o veículo com bloqueio pessimista (`findByIdWithLock`).
    2. Verifica se já foi vendido (`isVendido`).
    3. Marca como vendido e indisponível (`setVendido(true)`, `setDisponivel(false)`).
    4. Salva o veículo (`save`).
  - **Por que `@Transactional`?**
    - Garante que a verificação e a atualização sejam atômicas, evitando que o veículo seja marcado como vendido duas vezes.
    - Suporta o bloqueio pessimista, essencial para operações críticas como vendas.
    - Mantém o banco consistente, mesmo em cenários de alta concorrência.
  - **Cenário de Concorrência**: Dois vendedores tentando marcar o mesmo veículo como vendido. O bloqueio e `@Transactional` garantem que apenas um consiga.

**Explicação Geral de `@Transactional`**:
- Em todos os métodos, `@Transactional` cria uma transação que envolve todas as operações no banco, garantindo que sejam executadas como uma unidade.
- O nível de isolamento padrão (`READ_COMMITTED`) minimiza leituras sujas, mas o bloqueio pessimista (`findByIdWithLock`) adiciona exclusividade para operações críticas.
- A anotação é crucial para a lógica de negócios (ex.: reservar veículos, gerenciar carrinhos) e para o gerenciamento de concorrência, especialmente em conjunto com `@Lock`.

## Atividade Prática

### Tarefa 1: Adicionar Desconto para Vendas Online

**Objetivo**: Modificar o método `calculatePrice` para aplicar um desconto de 5% em vendas online.

**Instruções**:
1. Abra o arquivo `VehicleService.java`.
2. Atualize o método `calculatePrice` para aceitar um parâmetro `saleType` e aplicar um desconto de 5% se `saleType = "ONLINE"`.
3. Teste o cálculo com um endpoint ou método de teste.

**Código sugerido**:

```java
public BigDecimal calculatePrice(Long vehicleId, String customerType, String saleType) {
    Vehicle vehicle = vehicleRepository.findById(vehicleId)
        .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));

    BigDecimal basePrice = vehicle.getPreco();
    VehicleColor color = vehicle.getColor();

    // Acréscimo por cor
    BigDecimal colorSurcharge = BigDecimal.ZERO;
    if (color == VehicleColor.PRATA) {
        colorSurcharge = new BigDecimal("2000");
    } else if (color == VehicleColor.PRETA) {
        colorSurcharge = new BigDecimal("1000");
    }

    BigDecimal priceWithColor = basePrice.add(colorSurcharge);

    // Desconto por tipo de cliente
    BigDecimal discountMultiplier = BigDecimal.ONE;
    if ("PJ".equals(customerType)) {
        discountMultiplier = new BigDecimal("0.8"); // 20% desconto
    } else if ("PCD".equals(customerType)) {
        discountMultiplier = new BigDecimal("0.7"); // 30% desconto
    }

    // Desconto extra para vendas online
    BigDecimal onlineDiscount = "ONLINE".equals(saleType) ? new BigDecimal("0.95") : BigDecimal.ONE;

    return priceWithColor.multiply(discountMultiplier).multiply(onlineDiscount);
}
```

**Teste**:
- Chame o método com `vehicleId=1`, `customerType="PJ"`, `saleType="ONLINE"`.
- Verifique se o preço final reflete o desconto de 20% (PJ) e 5% (online).

### Tarefa 2: Simular Conflito de Concorrência

**Objetivo**: Testar o bloqueio pessimista em `reservarVeiculo`.

**Instruções**:
1. Configure o ambiente local:
   - Instale **MySQL** e configure o banco conforme `application.properties`.
   - Rode o `auth-service` e `commerce-service` (`mvn spring-boot:run`).
2. Crie dois usuários com tokens JWT diferentes (use o endpoint `/api/auth/login` do `auth-service`).
3. Use **Postman** to enviar duas requisições simultâneas:
   - `POST /api/vehicles/1/reserve` com o token do Cliente A.
   - `POST /api/vehicles/1/reserve` com o token do Cliente B.
4. Observe os resultados:
   - Um cliente deve receber `200 OK` (reserva bem-sucedida).
   - O outro deve receber `400 Bad Request` (veículo indisponível).
5. Verifique no banco (tabela `vehicles`) que `disponivel = false` para o veículo `id=1`.

**Extra**:
- Modifique `reservarVeiculo` para usar `findById` em vez de `findByIdWithLock`.
- Repita o teste e observe se ambos os clientes conseguem reservar o mesmo veículo (inconsistência).

## Testando Endpoints com Postman

Os endpoints do `commerce-service` são protegidos por autenticação JWT. Abaixo estão as instruções para testá-los usando **Postman**.

### Pré-requisitos
- **Postman** instalado.
- Serviços `auth-service` e `commerce-service` rodando localmente.
- Banco de dados MySQL configurado com usuários de teste (ex.: `client1`, `client2` com senhas definidas).

### Configuração do Ambiente no Postman
1. Crie um ambiente chamado "Concessionaria".
2. Adicione as variáveis:
   - `auth_url`: `http://localhost:8080`
   - `commerce_url`: `http://localhost:8081`
   - `token`: (deixe vazio; será preenchido após o login)

### 1. Login para Obter o Token JWT
- **Endpoint**: `POST {{auth_url}}/api/auth/login`
- **Body** (JSON):
  ```json
  {
    "username": "client1",
    "password": "password123"
  }
  ```
- **Configuração**:
  - Método: `POST`
  - URL: `{{auth_url}}/api/auth/login`
  - Aba **Body** > **raw** > **JSON**: Insira o JSON acima.
  - Aba **Tests**: Adicione o script para salvar o token:
    ```javascript
    const response = pm.response.json();
    pm.environment.set("token", response.token);
    ```
- **Resultado esperado** (status `200 OK`):
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
  ```

### 2. Listar Veículos Disponíveis
- **Endpoint**: `GET {{commerce_url}}/api/vehicles/available`
- **Headers**:
  - `Authorization`: `Bearer {{token}}`
- **Configuração**:
  - Método: `GET`
  - URL: `{{commerce_url}}/api/vehicles/available`
  - Aba **Headers**:
    - Chave: `Authorization`
    - Valor: `Bearer {{token}}`
- **Resultado esperado** (status `200 OK`):
  ```json
  [
    {
      "id": 1,
      "modelo": "Civic",
      "preco": 100000.00,
      "color": "BRANCA",
      "disponivel": true
    },
    ...
  ]
  ```

### 3. Adicionar Veículo ao Carrinho
- **Endpoint**: `POST {{commerce_url}}/api/cart/add?vehicleId=1`
- **Headers**:
  - `Authorization`: `Bearer {{token}}`
  - `X-User-Id`: `client1`
- **Configuração**:
  - Método: `POST`
  - URL: `{{commerce_url}}/api/cart/add?vehicleId=1`
  - Aba **Headers**:
    - `Authorization`: `Bearer {{token}}`
    - `X-User-Id`: `client1`
- **Resultado esperado** (status `200 OK`):
  ```json
  {
    "id": 1,
    "userId": "client1",
    "vehicles": [
      {
        "id": 1,
        "modelo": "Civic",
        "preco": 100000.00,
        "color": "BRANCA"
      }
    ],
    "expirationTime": "2025-05-02T10:01:00"
  }
  ```

### 4. Reservar Veículo
- **Endpoint**: `POST {{commerce_url}}/api/vehicles/1/reserve`
- **Headers**:
  - `Authorization`: `Bearer {{token}}`
  - `X-User-Id`: `client1`
- **Configuração**:
  - Método: `POST`
  - URL: `{{commerce_url}}/api/vehicles/1/reserve`
  - Aba **Headers**:
    - `Authorization`: `Bearer {{token}}`
    - `X-User-Id`: `client1`
- **Resultado esperado** (status `200 OK`):
  ```json
  {
    "id": 1,
    "modelo": "Civic",
    "preco": 100000.00,
    "color": "BRANCA",
    "disponivel": false
  }
  ```

### 5. Testar Conflito de Concorrência
- **Objetivo**: Simular dois usuários reservando o mesmo veículo (`id=1`) simultaneamente.
- **Passos**:
  1. Obtenha tokens JWT para dois usuários (ex.: `client1` e `client2`) via `POST {{auth_url}}/api/auth/login`.
  2. Configure duas requisições no Postman:
     - Requisição 1: `POST {{commerce_url}}/api/vehicles/1/reserve`
       - Headers: `Authorization: Bearer <token_client1>`, `X-User-Id: client1`
     - Requisição 2: `POST {{commerce_url}}/api/vehicles/1/reserve`
       - Headers: `Authorization: Bearer <token_client2>`, `X-User-Id: client2`
  3. Envie as requisições simultaneamente (use "Run Collection" ou duas janelas do Postman).
  4. **Resultado esperado**:
     - Uma requisição retorna `200 OK` (reserva bem-sucedida).
     - A outra retorna `400 Bad Request` (ex.: "Veículo não está disponível").
  5. Verifique no banco (tabela `vehicles`) que `disponivel = false` para `id=1`.

### Possíveis Erros
- **401 Unauthorized**: Token ausente ou inválido. Verifique o token no header `Authorization`.
- **403 Forbidden**: Usuário sem permissão (ex.: `ROLE_USER` necessário). Confirme o perfil do usuário.
- **400 Bad Request**: Veículo já reservado ou inválido. Verifique o estado no banco.
- **500 Internal Server Error**: Banco de dados não configurado. Confirme `application.properties`.

## Como Executar o Projeto

### Pré-requisitos
- **Java 17** ou superior.
- **Maven** para gerenciar dependências.
- **MySQL** com um banco configurado (ex.: `concessionaria`).
- **Postman** para testar endpoints.

### Passos
1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/concessionaria-projeto.git
   cd concessionaria-projeto
   ```
2. Configure o banco de dados em `commerce-service/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/concessionaria
   spring.datasource.username=seu_usuario
   spring.datasource.password=sua_senha
   spring.jpa.hibernate.ddl-auto=update
   ```
3. Inicie os serviços:
   ```bash
   cd auth-service
   mvn spring-boot:run
   ```
   ```bash
   cd ../commerce-service
   mvn spring-boot:run
   ```
4. Teste os endpoints com Postman conforme descrito acima.

## Recursos Adicionais

- **Documentação do Spring Data JPA**: [Transactions](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions)
- **Bloqueio Pessimista**: [Baeldung - JPA Locking](https://www.baeldung.com/jpa-optimistic-pessimistic-locking)
- **Tutorial de Microsserviços com Spring Boot**: [Spring Microservices](https://spring.io/guides/gs/spring-boot/)
- **Postman Documentação**: [Postman Learning Center](https://learning.postman.com/docs/getting-started/introduction/)

## Contribuições

Sinta-se à vontade para abrir issues ou pull requests com melhorias, correções, ou novos exemplos. Este repositório é um recurso educacional, e sua colaboração é bem-vinda!

## Licença

Este projeto está licenciado sob a [MIT License](LICENSE).