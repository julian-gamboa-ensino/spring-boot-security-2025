# 🧪 Teste Manual — Navegação Simultânea em Dois Navegadores

## 🎯 Objetivo

Validar o comportamento do sistema quando **dois usuários distintos** acessam simultaneamente o sistema a partir de **dois navegadores diferentes**, garantindo:

- Sessões independentes com autenticação via JWT
- Controle de disponibilidade dos veículos em tempo real
- Expiração do carrinho após 1 minuto
- Bloqueio da efetivação da compra/venda após o tempo expirar

---

## 🧰 Pré-requisitos

- Dois navegadores distintos (ex: Chrome e Firefox) ou duas janelas anônimas
- Dois usuários cadastrados (ex: `cliente1` e `cliente2`)
- Pelo menos dois veículos disponíveis no sistema
- Sistema rodando com os três microsserviços ativos:
  - `auth-service`
  - `commerce-service`
  - `ui-service`
- Ambiente no perfil `dev` (para uso de dados mockados ou simplificados)

---

## 👣 Passo a Passo do Teste

### 🧍 Navegador A — Usuário CLIENTE 1

1. Acesse `http://localhost:8080`
2. Faça login com `cliente1`
3. Na listagem de veículos, clique em **“Detalhar”** em um dos veículos disponíveis (ex: Veículo A)
4. Clique em **“Adicionar ao Carrinho”**
5. Verifique se o cronômetro de 1 minuto aparece
6. Mantenha a aba aberta e **não finalize ainda**

---

### 🧍 Navegador B — Usuário CLIENTE 2

1. Acesse `http://localhost:8080`
2. Faça login com `cliente2`
3. Verifique que o **Veículo A está indisponível** na listagem de veículos

---

### ⏳ Após 1 minuto

1. No Navegador A:
   - Tente clicar em **Efetivar Compra**
   - Esperado: mensagem de erro informando que o veículo **não está mais disponível**
2. No Navegador B:
   - Verifique que o **Veículo A reapareceu na lista de disponíveis**

---

## ✅ Resultados Esperados

| Ação                                             | Resultado Esperado                                    |
|--------------------------------------------------|--------------------------------------------------------|
| Carrinho criado por CLIENTE 1                    | Veículo fica indisponível para CLIENTE 2              |
| CLIENTE 1 tenta comprar após 1 min               | Erro: veículo expirado                                |
| CLIENTE 2 atualiza lista após 1 min              | Veículo reaparece como disponível                     |
| Sessões em navegadores diferentes                | Comportamento isolado entre usuários                  |

---

## 🧼 Observações

- Em ambiente `dev`, os tokens podem estar mockados
- O cronômetro é apenas visual — não há push de atualização automática (necessário atualizar manualmente a listagem)
- O comportamento de concorrência pode variar em ambientes com persistência real

---

## 📌 Dica

Para repetir o teste com um **vendedor**, basta substituir o login de um dos usuários e validar o comportamento na venda física (deve exigir informar o vendedor).

---

📁 Este teste está vinculado à branch:  
**`feature/ui-service/adicionar-suporte-multi-navegador`**
