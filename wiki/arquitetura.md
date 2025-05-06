# Layered Architecture no Projeto da Concessionária

Este documento descreve como a **Layered Architecture** (Arquitetura em Camadas) é aplicada no projeto da concessionária de automóveis, um sistema baseado em microsserviços construído com **Spring Boot**, **Java**, e **MySQL**. A Layered Architecture organiza o código em camadas distintas (Controller, Service, Repository) para promover separação de preocupações, manutenibilidade e escalabilidade. O foco está no microsserviço `commerce-service`, que implementa a lógica de negócios para veículos, carrinhos, e vendas (incluindo o método `marcarComoVendido`), com menções ao `auth-service` quando relevante.

## Visão Geral da Layered Architecture

A **Layered Architecture** divide o sistema em camadas com responsabilidades específicas:

- **Controller Layer**: Manipula requisições HTTP, valida entradas, e retorna respostas.
- **Service Layer**: Contém a lógica de negócios, como cálculo de preços, gerenciamento de carrinhos, reservas, e marcação de veículos como vendidos (ex.: `marcarComoVendido`), usando `@Transactional` para transações.
- **Repository Layer**: Gerencia o acesso a dados, interagindo com o banco de dados via Spring Data JPA.

**Benefícios no Projeto**:
- **Separação de Responsabilidades**: Cada camada foca em uma tarefa específica (ex.: roteamento, lógica, persistência).
- **Manutenibilidade**: Alterações em uma camada (ex.: mudar o banco de dados) não afetam as demais.
- **Testabilidade**: Camadas podem ser testadas isoladamente (ex.: testes unitários para `marcarComoVendido`).
- **Suporte a Concorrência**: A camada de serviço usa `@Transactional` e bloqueios pessimistas para operações críticas, como vendas.

## Estrutura do Projeto

O projeto é composto por microsserviços, com o `commerce-service` sendo o principal foco da Layered Architecture:

- **`commerce-service/`**:
  - `src/main/java/com/example/commerce/controller/`: Controladores REST (ex.: `VehicleController`, `CartController`).
  - `src/main/java/com/example/commerce/service/`: Serviços com lógica de negócios (ex.: `VehicleService`, `CartService`).
  - `src/main/java/com/example/commerce/repository/`: Repositórios JPA (ex.: `VehicleRepository`, `CartRepository`).
- **`auth-service/`**: Gerencia autenticação, também seguindo a Layered Architecture, mas com foco em autenticação JWT.
- **`ui-service/`**: Interface web (não detalhada aqui).

## Camadas e Suas Responsabilidades

### 1. Controller Layer

- **Pacote**: `com.example.commerce.controller`
- **Responsabilidade**:
  - Recebe requisições HTTP (ex.: GET, POST) via endpoints REST.
  - Valida entradas (ex.: parâmetros, corpo JSON).
  - Chama a camada de serviço para processar a lógica, como marcar um veículo como vendido.
  - Formata e retorna respostas HTTP (ex.: JSON, códigos de status).
- **Características**:
  - Usa anotações como `@RestController`, `@GetMapping`, `@PostMapping`.
  - Não contém lógica de negócios ou operações transacionais.
  - Protege endpoints com autenticação JWT (ex.: `@PreAuthorize` ou filtros de segurança).
- **Exemplo de Código** (`VehicleController`):

```java
package com.example.commerce.controller;

import com.example.commerce.dto.VehicleDTO;
import com.example.commerce.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping("/available")
    public ResponseEntity<List<VehicleDTO>> getAvailableVehicles() {
        List<VehicleDTO> vehicles = vehicleService.getAvailableVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<VehicleDTO> reserveVehicle(@PathVariable Long id, @RequestHeader("X-User-Id") String userId) {
        VehicleDTO vehicle = vehicleService.reservarVeiculo(id, userId);
        return ResponseEntity.ok(vehicle);
    }

    @PostMapping("/{id}/sell")
    public ResponseEntity<VehicleDTO> sellVehicle(@PathVariable Long id) {
        VehicleDTO vehicle = vehicleService.marcarComoVendido(id);
        return ResponseEntity.ok(vehicle);
    }
}
```

- **Explicação**:
  - Mapeia endpoints como `/api/vehicles/available`, `/api/vehicles/{id}/reserve`, e `/api/vehicles/{id}/sell`.
  - O endpoint `/sell` chama `marcarComoVendido` na camada de serviço, delegando a lógica de venda.
  - Usa `ResponseEntity` para formatar respostas HTTP.
  - O header `X-User-Id` passa o ID do usuário autenticado (validado via JWT).

### 2. Service Layer

- **Pacote**: `com.example.commerce.service`
- **Responsabilidade**:
  - Implementa a lógica de negócios, incluindo cálculo de preços, gerenciamento de carrinhos, reservas, e marcação de veículos como vendidos (`marcarComoVendido`).
  - Orquestra operações entre a Controller Layer e a Repository Layer.
  - Gerencia transações com `@Transactional` para garantir atomicidade e consistência.
- **Características**:
  - Usa `@Service` para marcar classes como componentes Spring.
  - Contém métodos que implementam regras do cliente (ex.: acréscimos por cor, validade de carrinhos, vendas permanentes).
  - Usa `@Transactional` em métodos que modificam o banco ou requerem exclusividade, como `marcarComoVendido`.
- **Exemplo de Código** (`VehicleService` com `marcarComoVendido`):

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

- **Explicação**:
  - **Lógica de Negócios**:
    - `reservarVeiculo`: Reserva um veículo, verificando disponibilidade e adicionando ao carrinho.
    - `marcarComoVendido`: Marca um veículo como vendido, tornando-o indisponível permanentemente.
  - **Uso de `@Transactional` em `marcarComoVendido`**:
    - Garante que a busca com bloqueio pessimista (`findByIdWithLock`), verificação (`isVendido`), e atualizações (`setVendido`, `setDisponivel`) sejam atômicas.
    - Evita que dois vendedores marquem o mesmo veículo como vendido simultaneamente.
  - **Interação com Camadas**:
    - Chamado pela **Controller Layer** (ex.: `VehicleController` via `/api/vehicles/{id}/sell`).
    - Usa a **Repository Layer** (`VehicleRepository`) para operações no banco.
    - Coordena com `CartService` em `reservarVeiculo`, mas `marcarComoVendido` é independente.

### 3. Repository Layer

- **Pacote**: `com.example.commerce.repository`
- **Responsabilidade**:
  - Fornece métodos para acessar e manipular dados no banco de dados (MySQL).
  - Abstrai operações CRUD e consultas personalizadas usando Spring Data JPA.
  - Suporta transações iniciadas na camada de serviço, como em `marcarComoVendido`, com bloqueios pessimistas.
- **Características**:
  - Usa `@Repository` para marcar interfaces como componentes Spring.
  - Estende `JpaRepository` para operações padrão (ex.: `findById`, `save`).
  - Inclui métodos anotados com `@Lock` para bloqueios pessimistas em operações críticas.
- **Exemplo de Código** (`VehicleRepository`):

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

- **Explicação**:
  - Fornece o método `findByIdWithLock` para busca com bloqueio pessimista, usado em `marcarComoVendido` e `reservarVeiculo`.
  - Suporta a camada de serviço em operações críticas, como marcar veículos como vendidos.
  - Abstrai detalhes do banco, permitindo que a camada de serviço foque na lógica de negócios.

## Fluxo na Layered Architecture

**Exemplo de Fluxo** (Marcação de Veículo como Vendido):
1. **Controller Layer**: Uma requisição `POST /api/vehicles/1/sell` chega ao `VehicleController`, que extrai o `vehicleId`.
2. **Service Layer**: O controlador chama `VehicleService.marcarComoVendido`, que:
   - Usa `@Transactional` para iniciar uma transação.
   - Chama `VehicleRepository.findByIdWithLock` para bloquear o veículo.
   - Verifica se o veículo já foi vendido (`isVendido`).
   - Marca como vendido e indisponível (`setVendido(true)`, `setDisponivel(false)`).
   - Salva as alterações no banco.
3. **Repository Layer**: O `VehicleRepository` executa a consulta com bloqueio e salva as alterações no banco.
4. **Controller Layer**: O serviço retorna um `VehicleDTO`, que o controlador encapsula em um `ResponseEntity` com status `200 OK`.

**Diagrama Simplificado**:
```
[Requisição HTTP: POST /api/vehicles/1/sell] -> [Controller Layer: VehicleController]
                                                 |
                                                 v
                                            [Service Layer: VehicleService.marcarComoVendido]
                                                 |        @Transactional
                                                 v
                                       [Repository Layer: VehicleRepository.findByIdWithLock]
                                                 |
                                                 v
                                             [Banco de Dados: MySQL]
```

## Uso de `@Transactional` na Layered Architecture

A anotação `@Transactional` é usada exclusivamente na **Service Layer**, onde a lógica de negócios requer transações atômicas. Exemplos no projeto:

- **`VehicleService.marcarComoVendido`**:
  - **Camada**: Service Layer.
  - **Propósito**: Marca um veículo como vendido, garantindo exclusividade via bloqueio pessimista.
  - **Por que `@Transactional`?**
    - Garante que a busca com bloqueio (`findByIdWithLock`), verificação (`isVendido`), e atualizações sejam atômicas.
    - Evita que dois vendedores marquem o mesmo veículo como vendido simultaneamente.
  - **Cenário de Concorrência**: Dois vendedores tentam vender o mesmo veículo. O bloqueio pessimista e `@Transactional` garantem que apenas um consiga.
- **`VehicleService.reservarVeiculo`**:
  - **Camada**: Service Layer.
  - **Propósito**: Reserva um veículo, verificando disponibilidade e adicionando ao carrinho.
  - **Por que `@Transactional`?** Agrupa operações de busca, verificação, e atualização, evitando reservas parciais.
- **`CartService.addToCart`**:
  - **Camada**: Service Layer.
  - **Propósito**: Adiciona um veículo a um carrinho, definindo validade.
  - **Por que `@Transactional`?** Garante consistência em operações de busca e atualização.

**Por que na Service Layer?**
- A **Service Layer** é o local natural para `@Transactional`, pois é onde a lógica de negócios orquestra múltiplas operações no banco, como em `marcarComoVendido`.
- A **Repository Layer** suporta essas transações com métodos como `findByIdWithLock`.
- A **Controller Layer** não usa `@Transactional`, pois apenas roteia requisições, delegando a lógica transacional aos serviços.

## Benefícios da Layered Architecture no Projeto

- **Modularidade**: A lógica de negócios, como `marcarComoVendido`, é isolada na Service Layer, facilitando manutenção.
- **Gerenciamento de Concorrência**: A combinação de `@Transactional` (Service Layer) e `@Lock` (Repository Layer) garante operações seguras, como vendas.
- **Flexibilidade**: Facilita a substituição de componentes (ex.: trocar MySQL por PostgreSQL na Repository Layer).
- **Escalabilidade**: Permite adicionar novos endpoints (ex.: `/sell`) ou regras de negócios sem afetar a estrutura geral.

## Conclusão

A **Layered Architecture** no projeto da concessionária organiza o código de forma clara e modular, com cada camada (Controller, Service, Repository) desempenhando um papel específico. A **Service Layer** é o coração da lógica de negócios, implementando métodos críticos como `marcarComoVendido` com `@Transactional` para gerenciar transações e concorrência. A **Repository Layer** suporta com acesso eficiente aos dados, enquanto a **Controller Layer** atua como a interface com o mundo externo via APIs REST seguras. Esta estrutura torna o projeto robusto, manutenível e adequado para cenários educacionais e reais.

## Recursos Adicionais

- **Spring Boot Layered Architecture**: [Baeldung - Spring Boot Architecture](https://www.baeldung.com/spring-boot-layered-architecture)
- **Spring Data JPA**: [Transactions](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions)
- **Bloqueio Pessimista**: [Baeldung - JPA Locking](https://www.baeldung.com/jpa-optimistic-pessimistic-locking)