/**
 * Configuração de Segurança JWT
 * ==========================
 * 
 * Define as configurações para geração e validação de tokens JWT.
 * 
 * Parâmetros de Segurança:
 * ----------------------
 * 1. Token
 *    - Tempo de expiração: 24 horas
 *    - Algoritmo: HS256
 *    - Chave secreta: 256 bits
 * 
 * 2. Claims Incluídas
 *    - Username
 *    - Roles (perfis)
 *    - Data de emissão
 *    - Data de expiração
 * 
 * 3. Validações
 *    - Assinatura
 *    - Expiração
 *    - Integridade
 */ 