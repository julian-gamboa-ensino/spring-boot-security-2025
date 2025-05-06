# Stateless Nature of JWT in the Car Dealership Microservices Project

## Introduction

In the car dealership microservices project, **JSON Web Tokens (JWT)** leverage their **stateless nature** to enable **multi-browser support** and ensure **consistent request processing** across the `auth-service`, `commerce-service`, and `ui-service`. The stateless nature of JWTs means that each token contains all necessary information, such as the user's CPF (`sub`) and roles (`roles`), eliminating the need for the `commerce-service` to query the `auth-service` or a session database to validate user identity. This allows multiple browsers to send the same JWT, with the system processing requests consistently, supporting features like cart management with 1-minute expiration. This document explains how this works, its implementation, advantages, limitations, and recommendations for improvement.

---

## What is the Stateless Nature of JWT?

A JWT is **stateless** because it is a **self-contained** token that includes:
- **Header**: Specifies the signing algorithm (e.g., `HS512`).
- **Payload**: Contains claims like `sub` (user ID, CPF in this project), `roles` (e.g., `ROLE_USER`), `iat` (issued at), and `exp` (expiration).
- **Signature**: Ensures integrity and authenticity, verifiable with a secret key.

Unlike traditional session-based authentication, where the server stores session data (e.g., in a database or cache), a JWT embeds all authentication and authorization data in the token itself. The server only needs to verify the token's signature and claims to authenticate a request, without additional lookups.

**Key Statement**:
> "As the JWT contains all necessary information (`sub`, `roles`), the `commerce-service` does not need to query the `auth-service` or a session database to validate user identity. This allows multiple browsers to send the same JWT, and the system processes requests consistently."

---

## How It Works in the Project

The stateless nature of JWTs is critical for multi-browser support and consistent request processing in the project. Here's how it is implemented:

### 1. **JWT Generation in `auth-service`**
- During login, the `ui-service` sends the user's CPF and password to the `auth-service` (`/api/auth/login`).
- The `auth-service` validates credentials and generates a JWT with the `JwtService`:
  ```java
  public String generateToken(User user) {
      return Jwts.builder()
          .setSubject(user.getUsername()) // CPF as userId
          .claim("roles", user.getRoles().stream().map(Enum::name).toList())
          .setIssuedAt(new Date())
          .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 24 hours
          .signWith(getSigningKey(), SignatureAlgorithm.HS512)
          .compact();
  }
  ```
- The JWT payload includes:
  ```json
  {
    "sub": "12345678901",
    "roles": ["ROLE_USER"],
    "iat": 1697059200,
    "exp": 1697145600
  }
  ```
- The token is returned to the `ui-service` and stored in the browser's HTTP session:
  ```java
  session.setAttribute("token", response.get("token"));
  ```

- **Stateless Aspect**:
  - The JWT contains the user's CPF (`sub`) and roles, so no server-side session is stored in the `auth-service`.
  - Multiple browsers logging in with the same credentials receive identical JWTs (same `sub`, `roles`, `exp`), as the token is generated based on the user's credentials.

### 2. **JWT Validation in `commerce-service`**
- The `ui-service` includes the JWT in API calls to the `commerce-service` via the `Authorization: Bearer <token>` header:
  ```java
  String token = (String) session.getAttribute("token");
  Map<String, Object> cart = cartService.getCart(token != null ? token : "").block();
  ```
- The `commerce-service` validates the JWT using a `JwtAuthenticationFilter`:
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
- **Stateless Validation**:
  - The `commerce-service` verifies the token's signature using the shared secret key (configured via Jasypt).
  - It extracts the `sub` (CPF) and `roles` directly from the token without querying the `auth-service` or a session store.
  - The `sub` is used as the `userId` to associate actions (e.g., cart operations):
    ```java
    String userId = authentication.getName(); // CPF from JWT
    cartService.addToCart(cartId, vehicleId, userId);
    ```

- **Multi-Browser Consistency**:
  - Each browser sends the same JWT (since it’s generated from the same CPF/password).
  - The `commerce-service` processes each request independently, using the `userId` to query the database, ensuring all browsers interact with the same user-specific data (e.g., the same cart).

### 3. **Cart Management and Consistency**
- The `commerce-service` ties carts to the `userId` (CPF from JWT):
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
- **Single Cart Rule**:
  - Only one active cart is allowed per `userId`, ensuring all browsers operate on the same cart.
- **Cart Expiration**:
  - Carts expire after 1 minute, managed by `Cart.expirationTime`:
    ```java
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expirationTime = createdAt.plusMinutes(1);
    }
    ```
  - A scheduled task clears expired carts:
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
- **Consistency Across Browsers**:
  - Since the cart is tied to `userId` and stored in the database, all browsers querying `/api/cart` receive the same cart state (e.g., active with vehicles or expired).
  - The stateless JWT ensures that each browser’s request is processed identically, without requiring session synchronization.

---

## Example Workflow

Consider a user with CPF `12345678901`:
1. **Login**:
   - The user logs in on Chrome and Firefox via `ui-service` (`/login`).
   - The `auth-service` generates a JWT:
     ```json
     {
       "sub": "12345678901",
       "roles": ["ROLE_USER"],
       "iat": 1697059200,
       "exp": 1697145600
     }
     ```
   - Both browsers store the same JWT in their respective HTTP sessions.

2. **Cart Creation**:
   - In Chrome, the user adds vehicle ID `1` to a cart. The `ui-service` sends the JWT to `commerce-service`, which creates a cart with `userId = 12345678901` and `expirationTime = now + 1 minute`.
   - The `commerce-service` validates the JWT without querying the `auth-service`, using the `sub` to set `userId`.

3. **Concurrent Action**:
   - In Firefox, the user adds vehicle ID `2`. The `commerce-service` validates Firefox’s JWT (same `sub`), updates the same cart (`userId = 12345678901`), and resets `expirationTime`.
   - Chrome refreshes the cart view, sending its JWT. The `commerce-service` retrieves the cart by `userId`, showing vehicles `1` and `2`.

4. **Expiration**:
   - After 1 minute, `cleanExpiredCarts` marks the cart as `EXPIRED`.
   - Both browsers attempt checkout, sending their JWTs. The `commerce-service` validates each JWT, checks the cart state, and returns `"Veículo não está mais disponível no carrinho"`.

**Consistency**:
- The stateless JWT ensures that each browser’s request is validated independently, but all actions affect the same database state (cart with `userId = 12345678901`).
- The database provides a single source of truth, ensuring consistent cart states across browsers.

---

## Advantages of the Stateless Nature

1. **Scalability**:
   - No session storage is needed in the `commerce-service` or `auth-service`, reducing database or cache overhead.
   - The system can handle many simultaneous users across browsers, as validation only requires checking the JWT’s signature.

2. **Consistency**:
   - Multiple browsers send the same JWT (same `sub`), ensuring all requests are tied to the same `userId` and affect the same cart.
   - The database-driven cart state ensures all browsers see the same data.

3. **Simplicity**:
   - The `commerce-service` validates requests without external dependencies (e.g., no calls to `auth-service`), simplifying the architecture.
   - The `ui-service` only needs to forward the JWT, stored in HTTP sessions.

4. **Multi-Browser Support**:
   - Each browser can operate independently, sending the JWT with each request, and the `commerce-service` processes them consistently.

---

## Limitations

1. **HTTP Session Dependency in `ui-service`**:
   - While the `commerce-service` leverages the stateless JWT, the `ui-service` stores JWTs in `HttpSession`, introducing server-side state.
   - This limits scalability and risks session loss on server restarts.
   - **Recommendation**: Store JWTs in HTTP-only, secure cookies:
     ```java
     Cookie cookie = new Cookie("jwt", token);
     cookie.setHttpOnly(true);
     cookie.setSecure(true);
     response.addCookie(cookie);
     ```

2. **Single Cart Restriction**:
   - The one-cart-per-user rule, enforced by `userId`, ensures consistency but prevents browsers from managing separate carts.
   - **Recommendation**: Allow multiple carts by adding a `sessionId` claim to the JWT:
     ```json
     {
       "sub": "12345678901",
       "sessionId": "browser-uuid",
       "roles": ["ROLE_USER"]
     }
     ```

3. **No Token Revocation**:
   - JWTs are valid for 24 hours and cannot be revoked (e.g., on logout), allowing a stolen JWT to be used across browsers.
   - **Recommendation**: Implement a token blacklist in a database or Redis:
     ```java
     public void revokeToken(String token) {
         tokenBlacklistRepository.save(new BlacklistedToken(token, LocalDateTime.now().plusHours(24)));
     }
     ```

4. **Concurrency Risks**:
   - While `@Transactional` prevents race conditions, the `Vehicle` entity’s `version` field is not consistently used for optimistic locking.
   - **Recommendation**: Enforce optimistic locking:
     ```java
     @Version
     private Long version;
     ```

---

## Recommendations for Improvement

1. **Fully Stateless Architecture**:
   - Move JWT storage to the client (e.g., HTTP-only cookies) to eliminate `HttpSession` dependency:
     ```java
     @PostMapping("/login")
     public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password, HttpServletResponse response) {
         Map authResponse = authServiceClient.login(username, password).block();
         if (Boolean.TRUE.equals(authResponse.get("success"))) {
             Cookie cookie = new Cookie("jwt", (String) authResponse.get("token"));
             cookie.setHttpOnly(true);
             cookie.setSecure(true);
             cookie.setPath("/");
             response.addCookie(cookie);
             return ResponseEntity.ok().build();
         }
         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
     }
     ```
   - Add CSRF protection to secure cookie-based requests.

2. **Refresh Tokens**:
   - Implement refresh tokens to allow shorter JWT expiration (e.g., 1 hour):
     ```java
     @PostMapping("/refresh")
     public ResponseEntity<?> refreshToken(@RequestParam String refreshToken) {
         // Validate refresh token and issue new JWT
     }
     ```
   - Improves security by reducing the window for stolen token misuse.

3. **Multiple Carts**:
   - Support multiple carts per user by including a `sessionId` in the JWT or database:
     ```java
     @Transactional
     public Cart criarCarrinho(String userId, String sessionId) {
         Cart cart = new Cart();
         cart.setUserId(userId);
         cart.setSessionId(sessionId);
         return cartRepository.save(cart);
     }
     ```

4. **Enhanced Concurrency**:
   - Use optimistic locking consistently for `Vehicle` and `Cart`:
     ```java
     @Transactional
     public void addToCart(Long vehicleId, String userId) {
         Vehicle vehicle = vehicleRepository.findByIdWithLock(vehicleId)
             .orElseThrow(() -> new BusinessException("Veículo não encontrado"));
         // Update with version check
     }
     ```

---

## Conclusion

The **stateless nature of JWTs** is a cornerstone of the car dealership project’s multi-browser support. By embedding the user’s CPF (`sub`) and roles in the token, the `commerce-service` validates requests without querying the `auth-service` or a session store, ensuring scalability and consistency. Multiple browsers send the same JWT, and the `commerce-service` processes them identically, using the `userId` to manage a single cart in the database. While the `ui-service`’s HTTP session dependency introduces some state, the overall architecture leverages JWTs effectively for consistent request processing. Improvements like client-side JWT storage, refresh tokens, and multiple carts can enhance flexibility and scalability.

For further exploration:
- Test multi-browser scenarios with Postman to simulate concurrent JWT requests.
- Review `JwtService.java` for token generation/validation logic.
- Implement cookie-based JWT storage to eliminate `HttpSession` dependency.