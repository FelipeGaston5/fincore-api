# 💰 FinCore API v0.1.0

> API REST desenvolvida em Java com Spring Boot para gerenciamento financeiro, com arquitetura modular, autenticação JWT e controle seguro de acesso a recursos financeiros.

---

## 📋 Sumário

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Arquitetura e Tecnologias](#-arquitetura-e-tecnologias)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Banco de Dados](#-banco-de-dados)
- [Segurança](#-segurança)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Execução](#-instalação-e-execução)
- [Variáveis de Ambiente](#-variáveis-de-ambiente)
- [Endpoints](#-endpoints)
- [Testes](#-testes)
- [Licença](#-licença)

---

## 🚀 Sobre o Projeto

O **FinCore API** é um projeto backend voltado ao gerenciamento de operações financeiras, desenvolvido com Java e Spring Boot.

Seu objetivo é oferecer uma API REST centralizada para administrar contas, receitas, despesas, transferências e planejamento orçamentário.

O projeto utiliza arquitetura modular, separação de responsabilidades e ferramentas de persistência e versionamento de banco de dados.

A aplicação foi concebida para funcionar de maneira independente, permitindo futuras integrações com aplicações web, mobile e outros sistemas.

---

## ⚙️ Funcionalidades

O desenvolvimento do FinCore é organizado em módulos independentes, permitindo evolução gradual e manutenção simplificada.

### Implementadas

| Módulo | Descrição |
|---|---|
| 👤 Usuários | Cadastro e gerenciamento de usuários |
| 🔐 Autenticação | Login e autenticação utilizando JWT |
| 🛡️ Segurança | Proteção de rotas e controle de acesso com Spring Security |
| 🏦 Contas | Cadastro, consulta, atualização e exclusão lógica de contas financeiras |

### Planejadas

| Módulo | Descrição |
|---|---|
| 🗂️ Categorias | Organização de receitas e despesas por categorias |
| 💸 Transações | Registro e gerenciamento de receitas e despesas |
| 🔄 Transferências | Controle de transferências entre contas |
| 📊 Orçamentos | Planejamento e acompanhamento de limites financeiros |
| 🔁 Transações recorrentes | Gerenciamento de lançamentos financeiros recorrentes |

---

## 🛠️ Arquitetura e Tecnologias

### Backend

- **Java 21** — Linguagem principal.
- **Spring Boot 4.1.2** — Framework para desenvolvimento da aplicação.
- **Spring Data JPA** — Persistência e gerenciamento de entidades.
- **Spring Security** — Autenticação e autorização.
- **JWT** — Autenticação baseada em tokens.
- **Maven** — Gerenciamento de dependências e build.
- **Maven Wrapper** — Padronização da execução do Maven.

### Banco de Dados

- **PostgreSQL 17** — Banco de dados relacional.
- **Flyway** — Versionamento e gerenciamento de migrations.

### Infraestrutura

- **Docker** — Containerização da aplicação.
- **Docker Compose** — Orquestração dos containers da API e do banco de dados.

---

## 📂 Estrutura do Projeto

O repositório está organizado separando o backend dos arquivos de infraestrutura.

```text
fincore-api/
│
├── .github/
├── .vscode/
│
├── back-end/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/fincore/fincore_api/
│   │   │   │       ├── common/
│   │   │   │       │
│   │   │   │       ├── modules/
│   │   │   │       │   ├── user/
│   │   │   │       │   │   ├── controller/
│   │   │   │       │   │   ├── dto/
│   │   │   │       │   │   ├── entity/
│   │   │   │       │   │   ├── repository/
│   │   │   │       │   │   └── service/
│   │   │   │       │   │
│   │   │   │       │   ├── account/
│   │   │   │       │   │   ├── controller/
│   │   │   │       │   │   ├── dto/
│   │   │   │       │   │   ├── entity/
│   │   │   │       │   │   ├── repository/
│   │   │   │       │   │   └── service/
│   │   │   │       │   │
│   │   │   │       │   └── auth/
│   │   │   │       │
│   │   │   │       ├── security/
│   │   │   │       │   ├── config/
│   │   │   │       │   ├── jwt/
│   │   │   │       │   └── ...
│   │   │   │       │
│   │   │   │       └── FincoreApiApplication.java
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── db/
│   │   │       │   └── migration/
│   │   │       └── application.properties
│   │   │
│   │   └── test/
│   │       └── java/
│   │
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── pom.xml
│   └── mvnw
│
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

*Observação: a estrutura apresenta a organização modular do projeto. Novos módulos e componentes serão incorporados conforme o desenvolvimento.*

---

## 🗄️ Banco de Dados

O FinCore utiliza PostgreSQL 17 como sistema de gerenciamento de banco de dados.

A estrutura do banco é gerenciada pelo Flyway, permitindo controlar a evolução do esquema por meio de migrations versionadas.

### Tabelas principais

| Tabela | Finalidade |
|---|---|
| `users` | Armazenamento dos usuários |
| `accounts` | Contas bancárias e carteiras |
| `categories` | Categorias financeiras |
| `transactions` | Receitas e despesas |
| `transfers` | Transferências entre contas |
| `budgets` | Orçamentos financeiros |
| `recurring_transactions` | Lançamentos recorrentes |

As migrations ficam armazenadas em:

```text
back-end/src/main/resources/db/migration/
```

O Hibernate utiliza validação do esquema, enquanto o Flyway controla as alterações estruturais do banco de dados.

---

## 🔐 Segurança

A API utiliza Spring Security e autenticação baseada em JSON Web Tokens (JWT).

Entre os mecanismos implementados estão:

- Autenticação de usuários.
- Geração e validação de tokens JWT.
- Proteção de endpoints.
- Identificação do usuário autenticado.
- Controle de acesso aos recursos financeiros.
- Criptografia de senhas por meio de PasswordEncoder.
- Configuração de CORS para controle de origens autorizadas.

### Autenticação

O fluxo de autenticação utiliza tokens de acesso.

1. O usuário envia suas credenciais para autenticação.
2. A API valida as credenciais.
3. Um token JWT é gerado.
4. O cliente utiliza o token nas requisições protegidas.

Exemplo de cabeçalho:

```http
Authorization: Bearer SEU_TOKEN_JWT
```

---

## 📋 Pré-requisitos

Para executar o projeto localmente, são necessárias:

- Java JDK 21.
- Docker Engine.
- Docker Compose.
- Git.

Não é necessário instalar PostgreSQL diretamente na máquina, pois o banco de dados é executado em um container Docker.

---

## 🚀 Instalação e Execução

### 1. Clonar o repositório

```bash
git clone <URL_DO_REPOSITORIO>
cd fincore-api
```

### 2. Configurar as variáveis de ambiente

Crie o arquivo `.env` a partir do modelo disponibilizado:

```bash
cp .env.example .env
```

Edite o arquivo `.env` e configure as credenciais do banco de dados e a chave JWT.

**Importante:** não compartilhe nem envie o arquivo `.env` para o Git.

### 3. Construir e iniciar os containers

```bash
docker compose up -d --build
```

### 4. Verificar os containers

```bash
docker compose ps
```

### 5. Consultar os logs da aplicação

```bash
docker compose logs -f fincore-api
```

Após a inicialização bem-sucedida, a API estará disponível em:

```text
http://localhost:8080
```

---

## 🔧 Variáveis de Ambiente

As configurações da aplicação são definidas por variáveis de ambiente.

| Variável | Descrição |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil de execução do Spring Boot |
| `DB_HOST` | Endereço do servidor PostgreSQL |
| `DB_PORT` | Porta do PostgreSQL |
| `DB_NAME` | Nome do banco de dados |
| `DB_USER` | Usuário do banco de dados |
| `DB_PASSWORD` | Senha do banco de dados |
| `JWT_SECRET` | Chave secreta utilizada na assinatura dos tokens |
| `JWT_ISSUER` | Emissor dos tokens JWT |
| `JWT_ACCESS_TOKEN_TTL_SECONDS` | Tempo de validade do token de acesso, em segundos |
| `CORS_ALLOWED_ORIGINS` | Origens autorizadas para acesso à API |

Os valores padrão e as configurações específicas de cada ambiente podem ser consultados nos arquivos de configuração.

**Nunca armazene credenciais reais diretamente no repositório.**

---

## 🌐 Endpoints

A API utiliza o prefixo:

```text
/api/v1
```

### Autenticação

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/v1/auth/login` | Autenticação do usuário |
| GET | `/api/v1/auth/me` | Consulta do usuário autenticado |
| POST | `/api/v1/auth/logout` | Encerramento da sessão de autenticação |

### Contas

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/v1/accounts` | Listagem de contas |
| GET | `/api/v1/accounts/{id}` | Consulta de uma conta |
| POST | `/api/v1/accounts` | Cadastro de conta |
| PUT | `/api/v1/accounts/{id}` | Atualização de conta |
| DELETE | `/api/v1/accounts/{id}` | Exclusão lógica de conta |

*Os endpoints protegidos exigem autenticação por JWT. A disponibilidade e os métodos exatos devem acompanhar as implementações dos respectivos controllers.*

---

## 🧪 Testes

O projeto utiliza a estrutura de testes do ecossistema Spring Boot.

Para executar os testes utilizando o Maven Wrapper:

```bash
cd back-end
./mvnw test
```

Para compilar o projeto:

```bash
./mvnw clean compile
```

Para gerar o pacote:

```bash
./mvnw clean package
```

---

## 📄 Licença

O FinCore API está em desenvolvimento.

A definição da licença será realizada conforme a estratégia de distribuição e utilização do projeto.

---

## 📌 Status do Projeto

<PRIVATE_PERSON> **FinCore API v0.1.0** encontra-se em desenvolvimento, com estrutura inicial, autenticação, segurança e gerenciamento de contas implementados.

Novos módulos e funcionalidades serão incorporados progressivamente.
