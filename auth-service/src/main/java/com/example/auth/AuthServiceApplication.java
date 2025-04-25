/**
 * Serviço de Autenticação e Autorização (Auth Service)
 * =================================================
 * 
 * Este é o serviço responsável por toda a gestão de autenticação e autorização
 * do sistema de comércio de veículos. 
 * 
 * Principais Responsabilidades:
 * ---------------------------
 * 1. Autenticação de usuários
 * 2. Geração e validação de tokens JWT
 * 3. Gestão de perfis de acesso
 * 4. Proteção de dados sensíveis
 * 
 * Fluxo de Autenticação:
 * --------------------
 * 1. Usuário envia credenciais
 * 2. Sistema valida e gera token JWT
 * 3. Token é usado para acessar outros serviços
 * 
 * Perfis de Acesso:
 * ---------------
 * - ADMIN: Acesso total ao sistema
 * - VENDOR: Gerenciamento de veículos e vendas
 * - CUSTOMER: Navegação e compras
 */ 