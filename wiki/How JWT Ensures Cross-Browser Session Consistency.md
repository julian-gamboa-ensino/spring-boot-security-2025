# How JWT Ensures Cross-Browser Session Consistency in the Car Dealership Microservices Project

## Introduction

In the car dealership microservices project, **JSON Web Tokens (JWT)** are used to enable **multi-browser support**, allowing a user to log in simultaneously from different browsers (e.g., Chrome, Firefox, Safari) or devices while maintaining **session consistency**. Session consistency means that all browser sessions for the same user interact with the same user-specific data (e.g., shopping cart) and respect the system's business rules, such as cart expiration and vehicle availability. This document explains how JWTs achieve this consistency, focusing on four key mechanisms: **Unique Identification**, **Independent Validation**, **Business Rules**, and **Cart Expiration**.

---

## Context

The project consists of three microservices:
- **auth-service**: Generates and validates JWTs for user authentication.
- **commerce-service**: Manages business logic, including carts, vehicles, and sales, using JWTs to identify users.
- **ui-service**: Provides the web interface, storing JWTs in HTTP sessions and interacting with the `commerce-service`.

The system supports a **cart expiration** feature where carts are valid for 1 minute, after which vehicles are released, and checkouts are rejected with the message `"Veículo não está mais disponível no carrinho"`. JWTs play a critical role in ensuring that these features work consistently across multiple browsers.

---

## How JWT Ensures Cross-Browser Session Consistency

The consistency of user sessions across multiple browsers is maintained through the stateless nature of JWTs and the project's architecture. Below are the four key mechanisms that enable this:

### 1. **Unique Identification**

**How it Works**:
- The JWT contains a `sub` claim with the user's **CPF** (e.g., `12345678901`), which serves as the `userId` to uniquely identify the user.
- All user actions (e.g., creating a cart, adding vehicles, checking out) are associated with this `userId` in the `commerce-service` database.
- When a user logs in from different browsers, the `auth-service` generates a JWT with the same `sub` (CPF) for each session, ensuring that all browsers operate under the same user identity.

**Implementation**:
- In `auth-service`, the `JwtService.generateToken` method sets the CPF as the `sub` claim:
  ```java
  public String generateToken(User user) {
      return Jwts.builder()
          .setSubject(user.getUsername()) // CPF as userId
          .claim("roles", user.getRoles().stream().map(Enum::name).toList())
          .setIssuedAt(new Date())
          .setExpiration(new Date(System.currentTimeMillis() + expiration))
          .signWith(getSigningKey(), SignatureAlgorithm.HS512)
          .compact();
  }
  ```
- The `commerce-service` extracts the `userId` from the JWT to associate carts:
  ```java
  String userId = authentication.getName(); // CPF from JWT
  cartService.addToCart(cartId, vehicleId, userId);
  ```

**Impact on Consistency**:
- Since all browsers use a JWT with the same `sub` (CPF), actions in any browser (e.g., adding a vehicle to the cart) update the same user-specific data in the database (e.g., `Cart.userId = 12345678901`).
- This ensures that all browsers see the same cart state, preventing discrepancies.

**Example**:
- User logs in with CPF `12345678901` on Chrome and Firefox.
- In Chrome, they add vehicle ID `1` to the cart. The `commerce-service` creates a cart with `userId = 12345678901`.
- In Firefox, viewing the cart shows vehicle ID `1`, because the `commerce-service` queries the cart by `userId`.

---

### 2. **Independent Validation**

**How it Works**:
- JWTs are **stateless**, meaning each token contains all necessary information (`sub`, `roles`, `exp`) to authenticate a request without server-side session storage.
- The `commerce-service` validates each incoming request independently by checking the JWT's signature and claims (e.g., `exp` for expiration, `sub` for user ID).
- Each browser sends its own JWT in the `Authorization: Bearer <token>` header, and the `commerce-service` processes requests without relying on a shared server-side session state.

**Implementation**:
- The `commerce-service` uses a `JwtAuthenticationFilter` to validate JWTs:
  ```java
  public Claims validateToken(String token) {
      try {
          return Jwts.parserBuilder()
              .setSigningKey(getSigningKey())
              .build()
              .parseClaimsJws(token)
              .getBody();
      } catch (JwtException e) {
          throw new AuthenticationException("Invalid or expired token");
      }
  }
  ```
- The `ui-service` sends the JWT stored in the HTTP session for each API call:
  ```java
  String token = (String) session.getAttribute("token");
  Map<String, Object> cart = cartService.getCart(token != null ? token : "").block();
  ```

**Impact on Consistency**:
- Since validation is independent, the `commerce-service` treats each browser's request consistently, using the same logic to query the database based on `userId`.
- There is no risk of session conflicts (e.g., one browser overwriting another's session), as the JWT itself carries the user identity.

**Example**:
- User adds vehicle ID `2` in Firefox. The `commerce-service` validates the JWT, extracts `userId = 12345678901`, and updates the cart.
- Simultaneously, Chrome requests the cart state. The `commerce-service` validates Chrome's JWT, retrieves the same `userId`, and returns the updated cart with vehicles `1` and `2`.

---

### 3. **Business Rules**

**How it Works**:
- The `commerce-service` enforces **business rules** to maintain consistency, such as allowing only **one active cart per user** and preventing concurrent modifications to vehicle availability.
- These rules are applied consistently across all browsers, using the `userId` from the JWT to scope actions.
- The system uses **ACID transactions** (`@Transactional`) to ensure data integrity, preventing race conditions (e.g., two browsers reserving the same vehicle simultaneously).

**Implementation**:
- **One Active Cart per User**:
  ```java
  @Transactional
  public Cart criarCarrinho(String userId) {
      cartRepository.findByUserIdAndFinalizadoFalse(userId)
          .ifPresent(cart -> { throw new RuntimeException("Usuário já possui um carrinho ativo"); });
      Cart cart = new Cart();
      cart.setUserId(userId);
      return cartRepository.save(cart);
  }
  ```
  - If a user tries to create a new cart in another browser, the system rejects it, ensuring a single cart.

- **Vehicle Reservation**:
  ```java
  @Transactional
  public VehicleDTO reservarVeiculo(Long vehicleId, String userId) {
      Vehicle vehicle = buscarPorId(vehicleId);
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
  ```
  - Ensures that a vehicle can only be reserved by one cart at a time.

**Impact on Consistency**:
- The single-cart rule ensures that all browsers operate on the same cart, preventing fragmented states (e.g., different carts in different browsers).
- ACID transactions guarantee that concurrent actions (e.g., reserving a vehicle) are processed sequentially, maintaining data integrity across browsers.

**Example**:
- User tries to create a new cart in Firefox while a cart is active in Chrome. The `commerce-service` throws an error: `"Usuário já possui um carrinho ativo"`.
- If Chrome and Firefox attempt to reserve the same vehicle simultaneously, the `@Transactional` annotation ensures one request succeeds and the other fails with `"Veículo não está disponível"`.

---

### 4. **Cart Expiration**

**How it Works**:
- The cart expiration feature (1-minute validity) is managed by the `commerce-service` in the database (`Cart.expirationTime`), not in the JWT.
- The `expirationTime` is set when the cart is created or updated:
  ```java
  @PrePersist
  protected void onCreate() {
      createdAt = LocalDateTime.now();
      expirationTime = createdAt.plusMinutes(1);
  }
  ```
- A scheduled task (`cleanExpiredCarts`) runs every minute to mark expired carts as `EXPIRED` and release vehicles:
  ```java
  @Scheduled(fixedRate = 60000)
  @Transactional
  public void cleanExpiredCarts() {
      List<Cart> expiredCarts = cartRepository.findByStatusAndExpirationTimeBefore(CartStatus.ACTIVE, LocalDateTime.now());
      for (Cart cart : expiredCarts) {
          cart.setStatus(CartStatus.EXPIRED);
          cart.getVehicles().clear();
          cartRepository.save(cart);
      }
  }
  ```
- All browsers querying the cart via `ui-service` see the same expiration state, as it is stored centrally in the database.

**Impact on Consistency**:
- Since `expirationTime` is database-driven, all browsers receive the same cart state (e.g., active or expired) when querying `/api/cart`.
- The `ui-service` displays the remaining time (`timeLeft`) based on the cart's `expiresAt`, ensuring a consistent user experience.
- If the cart expires, any checkout attempt from any browser returns `"Veículo não está mais disponível no carrinho"`.

**Example**:
- User adds a vehicle in Chrome, setting `expirationTime = now + 1 minute`.
- Firefox queries the cart 30 seconds later and sees the same `expirationTime`, displaying 30 seconds remaining.
- After 1 minute, the `cleanExpiredCarts` task marks the cart as `EXPIRED`. Both Chrome and Firefox show the error `"Veículo não está mais disponível no carrinho"` on checkout.

---

## Example Workflow

Consider a user with CPF `12345678901`:
1. **Login**:
   - Logs in on Chrome and Firefox via `ui-service` (`/login`).
   - Both browsers receive a JWT with `sub: "12345678901"`, stored in their respective HTTP sessions.

2. **Cart Creation**:
   - In Chrome, the user adds vehicle ID `1` to a new cart. The `commerce-service` creates a cart with `userId = 12345678901` and `expirationTime = now + 1 minute`.
   - In Firefox, the user views the cart and sees vehicle ID `1`, as the `commerce-service` queries by `userId`.

3. **Concurrent Action**:
   - In Firefox, the user tries to add vehicle ID `2`. The `commerce-service` updates the existing cart (same `userId`) and resets `expirationTime`.
   - Chrome refreshes the cart view and sees both vehicles `1` and `2`.

4. **Expiration**:
   - After 1 minute, `cleanExpiredCarts` marks the cart as `EXPIRED`.
   - Both browsers attempt checkout and receive `"Veículo não está mais disponível no carrinho"`.

**Consistency**:
- The cart state is consistent because all actions are tied to `userId = 12345678901`, and the database ensures a single source of truth.

---

## Limitations

1. **Single Cart Restriction**:
   - The rule of one active cart per user prevents browsers from managing separate carts, which may limit flexibility.
   - **Recommendation**: Allow multiple carts per `userId`, differentiated by a session ID or cart ID.

2. **HTTP Session Dependency**:
   - The `ui-service` stores JWTs in `HttpSession`, tying sessions to server-side state. If the server restarts, sessions are lost.
   - **Recommendation**: Store JWTs in HTTP-only cookies or client-side storage (with security measures).

3. **Concurrency Handling**:
   - While `@Transactional` prevents race conditions, the `Vehicle` entity lacks consistent optimistic locking.
   - **Recommendation**: Use the `version` field for optimistic concurrency control:
     ```java
     @Version
     private Long version;
     ```

4. **No Token Revocation**:
   - If a JWT is compromised, it remains valid until its 24-hour expiration.
   - **Recommendation**: Implement a token blacklist or use shorter expiration times with refresh tokens.

---

## Recommendations for Improvement

1. **Multiple Carts**:
   - Modify `CartService` to support multiple carts per `userId`, using a unique session identifier in the JWT or database.
2. **Client-Side JWT Storage**:
   - Use HTTP-only, secure cookies to store JWTs, reducing reliance on `HttpSession`.
3. **Refresh Tokens**:
   - Add a `/api/auth/refresh` endpoint in `auth-service` to extend sessions without re-authentication.
4. **Optimistic Locking**:
   - Enforce optimistic locking in `Vehicle` and `Cart` entities to handle concurrent updates robustly.
5. **Centralized Validation**:
   - Create a shared library for JWT validation to ensure consistent handling across services.

---

## Conclusion

JWTs ensure cross-browser session consistency in the car dealership project by:
- Providing a **unique identifier** (CPF in `sub`) to tie actions to a single user.
- Enabling **independent validation** of requests, eliminating server-side session dependencies.
- Supporting **business rules** like one active cart per user and ACID transactions for data integrity.
- Centralizing **cart expiration** in the database, ensuring all browsers see the same state.

This stateless, scalable approach allows multiple browsers to interact seamlessly with the system, maintaining a consistent user experience. By addressing limitations (e.g., single cart restriction, session dependency), the project can further enhance its multi-browser support.

For further exploration:
- Test concurrent cart updates using tools like Postman to simulate multiple browsers.
- Review `JwtService.java` and `SecurityConfig.java` for JWT handling details.
- Implement recommended improvements, such as refresh tokens or multiple carts.