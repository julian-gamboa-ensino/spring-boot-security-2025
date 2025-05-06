# Database Dependency in the auth-service of the Car Dealership Microservices Project

## Introduction

In the car dealership microservices project, the **auth-service** is responsible for user authentication and generating **JSON Web Tokens (JWT)** based on credentials (CPF and password). A critical question is whether the `auth-service` requires a **database (DB)** to function. This document explains why the `auth-service` needs a database, how it is used, and explores whether it could operate without one, focusing on the project's implementation as described in the provided microservices architecture.

---

## Why the auth-service Needs a Database

The `auth-service` requires a database to store and manage user data, validate credentials, and generate JWTs with accurate claims (`sub`, `roles`). The key reasons are:

1. **Storage of User Data**:
   - The `auth-service` maintains user records, including:
     - **CPF** (stored as `username`, used as `sub` in the JWT).
     - **Password** (hashed, typically with BCrypt for security).
     - **Roles** (e.g., `ROLE_USER`, `ROLE_VENDOR`, `ROLE_ADMIN`, used in the `roles` claim).
   - These records are stored in a database to enable lookup during authentication.

2. **Credential Validation**:
   - The `/api/auth/login` endpoint validates the CPF and password by querying the database:
     ```java
     @PostMapping("/login")
     public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
         Map<String, Object> response = new HashMap<>();
         User user = userService.findByUsername(username);
         if (user == null || !userService.validatePassword(password, user)) {
             response.put("success", false);
             response.put("message", "Invalid credentials");
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
         }
         String token = jwtService.generateToken(user);
         response.put("success", true);
         response.put("token", token);
         response.put("user", Map.of("id", user.getId(), "username", user.getUsername(), "roles", user.getRoles()));
         return ResponseEntity.ok(response);
     }
     ```
   - The `userService.findByUsername` queries the database to retrieve the user by CPF:
     ```java
     public User findByUsername(String username) {
         return userRepository.findByUsername(username);
     }
     ```

3. **JWT Generation**:
   - The JWT is generated using user data (CPF, roles) from the database:
     ```java
     public String generateToken(User user) {
         return Jwts.builder()
             .setSubject(user.getUsername()) // CPF as sub
             .claim("roles", user.getRoles().stream().map(Enum::name).toList())
             .setIssuedAt(new Date())
             .setExpiration(new Date(System.currentTimeMillis() + expiration))
             .signWith(getSigningKey(), SignatureAlgorithm.HS512)
             .compact();
     }
     ```
   - Without a database, the `auth-service` não teria acesso ao CPF (`sub`) ou aos papéis (`roles`) para incluir no JWT.

4. **User Management**:
   - Although not explicitly detailed, the `auth-service` may support user registration or updates (e.g., `/api/auth/register`), requiring database writes to create or modify user records.

---

## How the Database is Used in the auth-service

The `auth-service` interacts with a relational database (e.g., PostgreSQL, H2 for testing) as follows:

1. **User Entity**:
   - The `User` entity is mapped to a database table using JPA/Hibernate:
     ```java
     @Entity
     public class User {
         @Id
         private Long id;
         private String username; // CPF
         private String password; // Hashed with BCrypt
         @ElementCollection
         private List<Role> roles; // e.g., ROLE_USER
     }
     ```

2. **Repository**:
   - A `UserRepository` (likely a `JpaRepository`) handles database queries:
     ```java
     public interface UserRepository extends JpaRepository<User, Long> {
         Optional<User> findByUsername(String username);
     }
     ```

3. **Password Validation**:
   - The `userService.validatePassword` uses a `BCryptPasswordEncoder` to compare the provided password with the hashed password from the database:
     ```java
     public boolean validatePassword(String password, User user) {
         return passwordEncoder.matches(password, user.getPassword());
     }
     ```

4. **Configuration**:
   - The database is configured in `application.yml`:
     ```yaml
     spring:
       datasource:
         url: jdbc:postgresql://localhost:5432/authdb
         username: user
         password: pass
       jpa:
         hibernate:
           ddl-auto: update
     ```
   - The `ddl-auto: update` ensures that the schema is created/updated automatically.

5. **Dependency**:
   - The `/api/auth/login` endpoint relies on the database to:
     - Find the user by CPF (`findByUsername`).
     - Validate the password (`validatePassword`).
     - Retrieve roles for the JWT (`user.getRoles()`).

---

## Could the auth-service Operate Without a Database?

While the current implementation requires a database, it’s worth exploring whether the `auth-service` could function without one. Below are possible alternatives and their limitations:

### 1. **External Identity Provider (e.g., OAuth2)**
- **Approach**:
  - Delegate authentication to an external provider (e.g., Keycloak, Auth0, Google).
  - The `auth-service` receives a token (JWT or opaque) from the provider, validates it, and generates its own JWT with `sub` and `roles` mapped from the external token.
  - No local database is needed for user storage, as user data is managed by the provider.
- **Implementation**:
  - Configure the `auth-service` as an OAuth2 client:
    ```yaml
    spring:
      security:
        oauth2:
          client:
            registration:
              keycloak:
                client-id: auth-service
                client-secret: secret
                authorization-grant-type: password
                provider: keycloak
    ```
  - Map external claims to local JWT:
    ```java
    public String generateTokenFromExternalToken(String externalToken) {
        // Validate external token and extract user data
        String cpf = externalToken.getClaim("preferred_username");
        List<String> roles = externalToken.getClaimAsList("roles");
        return Jwts.builder()
            .setSubject(cpf)
            .claim("roles", roles)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }
    ```
- **Limitation**:
  - The external provider still uses a database to store users, so the dependency é transferida, não eliminada.
  - Requires integration with a third-party service, adding complexity.

### 2. **Static Users in Memory or Configuration**
- **Approach**:
  - Store users in memory or a configuration file (e.g., `application.yml`) for testing or small-scale systems:
    ```yaml
    auth:
      users:
        - username: "12345678901"
          password: "$2a$10$..." # Hashed password
          roles: ["ROLE_USER"]
        - username: "98765432109"
          password: "$2a$10$..."
          roles: ["ROLE_ADMIN"]
    ```
  - The `auth-service` valida credenciais contra essa configuração.
- **Implementation**:
  - Load users into memory:
    ```java
    @Configuration
    public class StaticUserConfig {
        @Bean
        public Map<String, User> users() {
            Map<String, User> users = new HashMap<>();
            users.put("12345678901", new User("12345678901", "$2a$10$...", List.of(Role.ROLE_USER)));
            return users;
        }
    }
    ```
  - Validate credentials:
    ```java
    public User findByUsername(String username) {
        return users.get(username);
    }
    ```
- **Limitation**:
  - Não escalável para muitos usuários.
  - Não suporta atualizações dinâmicas (ex.: registro de novos usuários).
  - Inseguro para produção, pois senhas hasheadas ficam em arquivos de configuração.

### 3. **Pre-Generated Tokens**
- **Approach**:
  - Generate JWTs manually and distribute them to users, with the `auth-service` only validating tokens.
  - No authentication (login) é necessária, eliminando a necessidade de um banco.
- **Implementation**:
  - Validate pre-generated JWTs:
    ```java
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    ```
- **Limitation**:
  - Não atende ao requisito de login dinâmico com CPF/senha.
  - Impraticável para gerenciar usuários em um sistema real.
  - Risco de segurança, pois tokens pré-gerados são difíceis de revogar.

**Conclusion**: The current project requires a database for dynamic user authentication and JWT generation. Alternatives like external providers or static users shift or limit the dependency but don’t eliminate the need for a database in a production-grade system.

---

## Advantages of Using a Database

1. **Dynamic User Management**:
   - Supports registration, updates, and deletion of users (e.g., via `/api/auth/register`).
   - Scales to handle thousands of users.

2. **Security**:
   - Stores passwords securely (hashed with BCrypt).
   - Allows role-based access control with persistent role assignments.

3. **Consistency**:
   - Ensures accurate `sub` (CPF) and `roles` in JWTs, maintaining consistency across services.

4. **Flexibility**:
   - Enables integration with other features (e.g., password reset, user profiles) that require persistent storage.

---

## Limitations of Database Dependency

1. **Overhead**:
   - Requires maintaining a database (e.g., PostgreSQL), increasing infrastructure costs and complexity.
   - Needs proper configuration for high availability and backups.

2. **Latency**:
   - Database queries (e.g., `findByUsername`) add slight latency to login requests.
   - Can be mitigated with caching (e.g., Redis for frequent users).

3. **Single Point of Failure**:
   - If the database is unavailable, the `auth-service` cannot authenticate users.
   - Requires robust database replication and failover strategies.

---

## Recommendations for Improvement

1. **Caching User Data**:
   - Use Redis to cache frequently accessed users, reducing database load:
     ```java
     @Cacheable("users")
     public User findByUsername(String username) {
         return userRepository.findByUsername(username);
     }
     ```

2. **External Identity Provider**:
   - Integrate with Keycloak or Auth0 to offload user management:
     ```yaml
     spring:
       security:
         oauth2:
           client:
             registration:
               keycloak:
                 client-id: auth-service
                 client-secret: secret
     ```
   - Reduces local database dependency but requires external service management.

3. **Database Optimization**:
   - Index the `username` column for faster lookups:
     ```sql
     CREATE INDEX idx_user_username ON users(username);
     ```
   - Use connection pooling (e.g., HikariCP) for efficient database access.

4. **Hybrid Approach**:
   - Store critical users in memory for testing but use a database for production:
     ```java
     @Profile("test")
     @Bean
     public Map<String, User> staticUsers() {
         return Map.of("12345678901", new User("12345678901", "$2a$10$...", List.of(Role.ROLE_USER)));
     }
     ```

---

## Conclusion

The `auth-service` in the car dealership project **requires a database** to store user data (CPF, hashed passwords, roles), validate credentials, and generate JWTs with accurate `sub` and `roles` claims. The database is integral to the `/api/auth/login` endpoint, enabling dynamic authentication and supporting multi-browser consistency. While alternatives like external identity providers or static user configurations could reduce or eliminate the local database, they either shift the dependency or are unsuitable for production. Optimizing database performance (e.g., caching, indexing) or integrating with an external provider can enhance scalability and flexibility while maintaining the project’s requirements.

For further exploration:
- Review `UserRepository.java` for database interactions.
- Test login performance with and without caching.
- Explore Keycloak integration for externalized authentication.