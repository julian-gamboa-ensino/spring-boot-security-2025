#!/bin/bash

# Lista de nomes das features (com prefixo do microserviço)
FEATURES=(
  "auth-service/implementar-validacao-cpf"
  "auth-service/adicionar-suporte-venda-fisica"
  "auth-service/expor-dados-usuario-texto-plano"
  "auth-service/adicionar-metricas-autenticacao"
  "commerce-service/implementar-calculo-preco"
  "commerce-service/adicionar-validacao-venda-fisica"
  "commerce-service/melhorar-controle-concorrencia"
  "commerce-service/adicionar-metricas-vendas"
  "commerce-service/implementar-relatorio-vendas"
  "ui-service/melhorar-tela-carrinho-timer"
  "ui-service/implementar-listagem-usuarios-vendedor"
  "ui-service/adicionar-botao-voltar-detalhes"
  "ui-service/melhorar-redirecionamento-perfil"
  "ui-service/adicionar-suporte-multi-navegador"
)

# Garante que estamos na branch main e atualizada
git checkout main
git pull origin main

# Laço para criar as branches
for FEATURE in "${FEATURES[@]}"; do
  if git show-ref --quiet "refs/heads/feature/$FEATURE"; then
    echo "Branch feature/$FEATURE já existe, pulando..."
  else
    echo "Criando branch feature/$FEATURE..."
    git checkout main
    git checkout -b "feature/$FEATURE"
    # Opcional: git push origin "feature/$FEATURE"
  fi
done

echo "Todas as branches foram processadas!"