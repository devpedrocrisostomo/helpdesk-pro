# HelpDesk Pro

API REST para gestao de chamados tecnicos usando Java 21, Jakarta EE, JAX-RS, JPA/Hibernate, CDI, PostgreSQL, JWT e Docker.

## Stack

- Java 21
- Jakarta EE 10
- JAX-RS
- JPA com Hibernate
- CDI
- Maven
- PostgreSQL
- JWT
- Docker e Docker Compose

## Estrutura

```text
src/main/java/com/helpdeskpro/
  config/       Aplicacao JAX-RS, CORS e configuracoes por env
  dto/          Contratos de entrada e saida
  exception/    Tratamento global de erros
  model/        Entidades JPA e enums
  repository/   Acesso a dados com EntityManager
  resource/     Controllers REST
  security/     JWT, contexto autenticado e hashing de senha
  service/      Regras de negocio
```

## Rodar com Docker

```bash
cp .env.example .env
docker compose up --build
```

Servicos locais:

- Frontend web: http://localhost:8081
- API: http://localhost:8081/api
- Health check: http://localhost:8081/api/health
- PostgreSQL: `localhost:5433`

O container cria automaticamente um usuario admin inicial quando nao houver usuario com o e-mail configurado.

Credenciais padrao de desenvolvimento:

- E-mail: `admin@helpdeskpro.local`
- Senha: `admin123`

Altere esses valores em `.env` antes de usar fora do ambiente local.

## Endpoints iniciais

### Health

```bash
curl http://localhost:8081/api/health
```

### Login

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@helpdeskpro.local\",\"password\":\"admin123\"}"
```

Resposta:

```json
{
  "token": "jwt",
  "tokenType": "Bearer",
  "expiresAt": "2026-06-09T15:00:00Z",
  "user": {
    "id": 1,
    "name": "HelpDesk Admin",
    "email": "admin@helpdeskpro.local",
    "role": "ADMIN",
    "createdAt": "2026-06-09T13:00:00Z"
  }
}
```

### Usuario autenticado

```bash
curl http://localhost:8081/api/auth/me \
  -H "Authorization: Bearer SEU_TOKEN"
```

### Usuarios

Todos os endpoints abaixo exigem usuario `ADMIN`.

```bash
curl -X POST http://localhost:8081/api/users \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Tecnico 1\",\"email\":\"tecnico@example.com\",\"password\":\"tecnico123\",\"role\":\"TECHNICIAN\"}"
```

Endpoints:

- `GET /api/users`
- `POST /api/users`
- `GET /api/users/{id}`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`

### Clientes

Todos os endpoints abaixo exigem `Authorization: Bearer SEU_TOKEN`.

```bash
curl -X POST http://localhost:8081/api/clients \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Cliente Teste\",\"email\":\"cliente@example.com\",\"phone\":\"11999999999\",\"document\":\"DOC-001\"}"
```

Endpoints:

- `GET /api/clients`
- `POST /api/clients`
- `GET /api/clients/{id}`
- `PUT /api/clients/{id}`
- `DELETE /api/clients/{id}`

### Chamados

Todos os endpoints abaixo exigem `Authorization: Bearer SEU_TOKEN`.

```bash
curl -X POST http://localhost:8081/api/tickets \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Falha no notebook\",\"description\":\"Notebook nao liga.\",\"priority\":\"HIGH\",\"clientId\":1,\"assignedTechnicianId\":1}"
```

Endpoints:

- `GET /api/tickets`
- `POST /api/tickets`
- `GET /api/tickets/{id}`
- `PUT /api/tickets/{id}`
- `PATCH /api/tickets/{id}/status`
- `PATCH /api/tickets/{id}/assignment`
- `DELETE /api/tickets/{id}`
- `GET /api/tickets/dashboard`

### Comentarios

Todos os endpoints abaixo exigem `Authorization: Bearer SEU_TOKEN`.

```bash
curl -X POST http://localhost:8081/api/tickets/1/comments \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"message\":\"Cliente informou que o problema continua.\"}"
```

Endpoints:

- `GET /api/tickets/{ticketId}/comments`
- `POST /api/tickets/{ticketId}/comments`
- `DELETE /api/tickets/{ticketId}/comments/{commentId}`

## Permissoes por perfil

Perfis disponiveis:

- `ADMIN`: acesso completo a usuarios, clientes, chamados, atribuicoes, status e comentarios.
- `TECHNICIAN`: visualiza clientes em modo leitura e atua somente nos chamados atribuidos a ele.
- `CLIENT`: visualiza o proprio cadastro de cliente, abre chamados para si mesmo e comenta apenas nos proprios chamados.

Regras principais:

- Apenas `ADMIN` gerencia usuarios.
- Apenas `ADMIN` cria, edita ou remove clientes.
- `TECHNICIAN` nao acessa CRUD de usuarios e nao remove chamados.
- `CLIENT` nao acessa usuarios, nao altera status e nao visualiza chamados de outros clientes.
- Ao criar um usuario `CLIENT`, o sistema cria automaticamente um cadastro de cliente vinculado ao mesmo e-mail.

## Variaveis de ambiente

- `JWT_SECRET`: segredo HMAC para assinar tokens. Use pelo menos 32 bytes.
- `JWT_EXPIRATION_MINUTES`: tempo de expiracao do token.
- `ADMIN_NAME`: nome do admin inicial.
- `ADMIN_EMAIL`: e-mail do admin inicial.
- `ADMIN_PASSWORD`: senha do admin inicial.
- `CORS_ALLOWED_ORIGINS`: origem permitida para chamadas do frontend.
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`: conexao usada pelo datasource do WildFly.

## Estado atual

Implementado nesta base inicial:

- Projeto Maven WAR com Java 21.
- Frontend web estatico servido pelo WildFly.
- Entidades JPA: `User`, `Client`, `Ticket`, `Comment`, `TicketHistory`.
- Enums de role, prioridade e status.
- Repositorios base.
- `persistence.xml` com datasource JTA `java:/jdbc/HelpDeskDS`.
- Hash de senha com BCrypt.
- Login com JWT.
- Filtro Bearer para endpoints protegidos.
- Endpoint `GET /api/auth/me`.
- CRUD inicial de clientes.
- CRUD inicial de chamados.
- CRUD inicial de usuarios.
- Atribuicao de tecnico.
- Mudanca de status com registro em `TicketHistory`.
- Comentarios por chamado.
- Dashboard de totais por status e prioridade.
- Tratamento global de erros em JSON.
- Docker com PostgreSQL e WildFly.
- Script SQL inicial com tabelas e indices.

Proximas etapas naturais:

- Filtros e paginacao em listagens.
- Testes automatizados de permissao por perfil.
- Testes automatizados de API.
