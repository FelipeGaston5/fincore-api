# 💰 FinCore API V0.1.0

> API REST desenvolvida em Java com Spring Boot para gerenciamento de operações financeiras, oferecendo uma estrutura modular, segura e escalável para controle de contas, receitas, despesas, transferências e planejamento orçamentário.

---

## 📋 Sumário

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Arquitetura e Tecnologias](#-arquitetura-e-tecnologias)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Banco de Dados](#-banco-de-dados)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Execução](#-instalação-e-execução)
- [Variáveis de Ambiente](#-variáveis-de-ambiente)
- [Endpoints](#-endpoints)
- [Testes](#-testes)
- [Licença](#-licença)

---

## 🚀 Sobre o Projeto

O **FinCore API** é um projeto backend voltado ao gerenciamento de operações financeiras, desenvolvido utilizando Java e Spring Boot.

A proposta é centralizar e organizar operações financeiras por meio de uma API REST, permitindo o gerenciamento de contas, receitas, despesas, transferências entre contas e planejamento de orçamentos.

O projeto adota uma arquitetura organizada em camadas, com separação de responsabilidades e utilização de ferramentas voltadas à persistência, versionamento do banco de dados e execução em ambientes containerizados.

O FinCore também foi concebido para servir como backend independente, permitindo futuras integrações com aplicações web, mobile e outros sistemas.

---

## ⚙️ Funcionalidades

O banco de dados e a estrutura inicial do projeto contemplam os seguintes módulos:

| Módulo | Descrição |
|---|---|
| 👤 Usuários | Estrutura para gerenciamento de usuários |
| 🏦 Contas | Cadastro e gerenciamento de contas bancárias e carteiras |
| 🗂️ Categorias | Organização de receitas e despesas por categorias |
| 💸 Transações | Registro de receitas e despesas |
| 🔄 Transferências | Controle de transferências entre contas |
| 📊 Orçamentos | Planejamento e acompanhamento de limites financeiros |
| 🔁 Transações recorrentes | Estrutura para lançamentos financeiros recorrentes |

---

## 🛠️ Arquitetura e Tecnologias

O projeto utiliza tecnologias do ecossistema Java para construção de APIs REST e gerenciamento de dados.

### Backend

- **Java 21** — Linguagem principal.
- **Spring Boot 4.1.2** — Framework para desenvolvimento da aplicação.
- **Spring Data JPA** — Abstração e gerenciamento da persistência de dados.
- **Maven** — Gerenciamento de dependências e build do projeto.
- **Maven Wrapper** — Padronização da execução do Maven.

### Banco de Dados

- **PostgreSQL 17** — Banco de dados relacional.
- **Flyway** — Versionamento e gerenciamento de migrations do banco de dados.

### Infraestrutura

- **Docker** — Containerização da aplicação.
- **Docker Compose** — Orquestração dos containers da API e do banco de dados.

---

## 📂 Estrutura do Projeto

O repositório está organizado separando a aplicação backend dos arquivos de infraestrutura.

```text
fincore-api/
│
├── .github/
│
├── .vscode/
│
├── back-end/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── fincore/
│   │   │   │           └── fincore_api/
│   │   │   │               ├── common/
│   │   │   │               └── FincoreApiApplication.java
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── db/
│   │   │       │   └── migration/
│   │   │       └── application.properties
│   │   │
│   │   └── test/
│   │       └── java/
│   │           └── com/
│   │               └── fincore/
│   │                   └── fincore_api/
│   │                       └── FincoreApiApplicationTests.java
│   │
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── pom.xml
│   └── mvnw
│
├── docker-compose.yml
└── README.md
```

*Observação: a estrutura representa a organização atual do repositório. Novos pacotes e camadas serão incorporados conforme a implementação dos módulos.*

---

## 🗄️ Banco de Dados

O FinCore utiliza PostgreSQL 17 como sistema de gerenciamento de banco de dados.

A estrutura inicial do banco é gerenciada pelo Flyway, permitindo versionar e aplicar alterações no esquema de forma controlada.

### Tabelas principais

- `users`
- `accounts`
- `categories`
- `transactions`
- `transfers`
- `budgets`
- `recurring_transactions`

As migrations ficam armazenadas em:

```text
back-end/src/main/resources/db/migration/
```

---

## 📋 Pré-requisitos

Para executar o projeto localmente, são necessárias as seguintes ferramentas:

- Java JDK 21 ou superior.
- Docker Engine.
- Docker Compose.
- Git.

Não é necessário instalar o PostgreSQL diretamente na máquina, pois o banco de dados é executado em um container Docker.

---

## 🚀 Instalação e Execução

### 1. Clonar o repositório

```bash
git clone <URL_DO_REPOSITORIO>
cd fincore-api
```

### 2. Configurar as variáveis de ambiente

Configure as variáveis necessárias para a conexão com o banco de dados, conforme o arquivo `docker-compose.yml`.

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

Após a inicialização bem-sucedida, a API estará disponível na porta `8080`.

---

## 🔐 Variáveis de Ambiente

As configurações de execução e conexão com o banco de dados são definidas por variáveis de ambiente.

| Variável | Descrição |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil de execução do Spring Boot |
| `DB_HOST` | Endereço do servidor PostgreSQL |
| `DB_PORT` | Porta do PostgreSQL |
| `DB_NAME` | Nome do banco de dados |
| `DB_USER` | Usuário do banco de dados |
| `DB_PASSWORD` | Senha do banco de dados |

Não armazene credenciais reais diretamente no repositório.

---

## 🌐 Endpoints

Os endpoints REST serão documentados conforme a implementação dos respectivos módulos.

A organização da API busca manter os recursos financeiros separados por responsabilidade, facilitando a manutenção e a expansão do sistema.

---

## 🧪 Testes

O projeto utiliza a estrutura de testes do ecossistema Spring Boot.

Para executar os testes utilizando o Maven Wrapper:

```bash
cd back-end
./mvnw test
```

---

## 📄 Licença

Este projeto está em desenvolvimento.

A definição da licença será realizada conforme a estratégia de distribuição e utilização do FinCore API.
