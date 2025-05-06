# Caminhos dos arquivos docker-compose
COMPOSE_BASE=docker-compose.yml
COMPOSE_DEV=docker-compose.dev.yml
COMPOSE_PROD=docker-compose.prod.yml

# Arquivos de ambiente
ENV_DEV=.env.dev
ENV_PROD=.env.prod

# Comandos principais

up-dev:
	@echo "🔧 Subindo ambiente de desenvolvimento..."
	@docker-compose --env-file $(ENV_DEV) -f $(COMPOSE_BASE) -f $(COMPOSE_DEV) up --build

down-dev:
	@echo "🧹 Derrubando ambiente de desenvolvimento..."
	@docker-compose --env-file $(ENV_DEV) -f $(COMPOSE_BASE) -f $(COMPOSE_DEV) down

up-prod:
	@echo "🚀 Subindo ambiente de produção..."
	@docker-compose --env-file $(ENV_PROD) -f $(COMPOSE_BASE) -f $(COMPOSE_PROD) up -d --build

down-prod:
	@echo "🧹 Derrubando ambiente de produção..."
	@docker-compose --env-file $(ENV_PROD) -f $(COMPOSE_BASE) -f $(COMPOSE_PROD) down

logs:
	@docker-compose logs -f

ps:
	@docker-compose ps

restart:
	@docker-compose restart

# Comando padrão
.DEFAULT_GOAL := up-dev
