# Sistema de Intercâmbio de Livros — API REST

> Projeto acadêmico  
> **Alunos:** Bruno (1181333) · Lucas (5140155)

API REST para troca de livros entre usuários. Autenticação via JWT, respostas em JSON, sem sessão no servidor.

---

## Sumário

- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Banco de Dados](#banco-de-dados)
- [Como Executar](#como-executar)
- [Autenticação JWT](#autenticação-jwt)
- [Endpoints](#endpoints)
- [Estrutura de Pastas](#estrutura-de-pastas)

---

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 11 | Linguagem principal |
| Jakarta EE 6 (Servlets) | Controllers HTTP |
| Apache Tomcat 10+ | Servidor de aplicação |
| MySQL 8 | Banco de dados relacional |
| JDBC | Acesso ao banco via `MysqlSingleton` |
| Gson 2.10 | Serialização/deserialização JSON |
| java-jwt 4.4 (Auth0) | Geração e validação de tokens JWT |
| Docker Compose | Banco de dados em container |

---

## Arquitetura

Padrão MVC sem camada de View — todos os retornos são JSON.

```
Requisição HTTP
      ↓
 AuthFilter          ← valida o JWT e injeta usuarioId no request
      ↓
 Controller          ← extrai parâmetros da URL e lê o body via InputStream
      ↓
 DAO                 ← executa queries no MySQL via JDBC
      ↓
 Resposta JSON       ← Controller serializa com Gson e define o status HTTP
```

### Fluxo de autenticação

```
POST /api/login  →  valida credenciais  →  retorna JWT
       ↓
Demais rotas  →  Authorization: Bearer <token>  →  AuthFilter injeta usuarioId
```

---

## Banco de Dados

**Nome:** `intercambio_livros` · **Porta:** `3307` · **Usuário/Senha:** `root / root`

### Tabelas

#### `usuarios`
| Coluna | Tipo | Descrição |
|---|---|---|
| id | INT PK AUTO_INCREMENT | Identificador único |
| nome | VARCHAR(100) | Nome do usuário |
| email | VARCHAR(100) UNIQUE | E-mail de login |
| senha | VARCHAR(255) | Senha |

#### `livros`
| Coluna | Tipo | Descrição |
|---|---|---|
| id | INT PK AUTO_INCREMENT | Identificador único |
| titulo | VARCHAR(200) | Título do livro |
| autor | VARCHAR(100) | Autor do livro |
| usuario_id | INT FK | Dono atual do livro |

#### `trocas`
| Coluna | Tipo | Descrição |
|---|---|---|
| id | INT PK AUTO_INCREMENT | Identificador único |
| livro_oferecido_id | INT FK | Livro que o solicitante oferece |
| livro_recebido_id | INT FK | Livro que o solicitante quer receber |
| usuario_solicitante_id | INT FK | Usuário que propôs a troca |
| usuario_solicitado_id | INT FK | Usuário que recebe a proposta |
| status | VARCHAR(20) | `PENDENTE`, `ACEITA` ou `RECUSADA` |

### Script SQL

```sql
CREATE DATABASE IF NOT EXISTS intercambio_livros;
USE intercambio_livros;

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL
);

CREATE TABLE livros (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    autor VARCHAR(100) NOT NULL,
    usuario_id INT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE trocas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    livro_oferecido_id INT NOT NULL,
    livro_recebido_id INT NOT NULL,
    usuario_solicitante_id INT NOT NULL,
    usuario_solicitado_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDENTE',
    FOREIGN KEY (livro_oferecido_id) REFERENCES livros(id),
    FOREIGN KEY (livro_recebido_id) REFERENCES livros(id),
    FOREIGN KEY (usuario_solicitante_id) REFERENCES usuarios(id),
    FOREIGN KEY (usuario_solicitado_id) REFERENCES usuarios(id)
);
```

---

## Como Executar

### Pré-requisitos

- JDK 11+
- Apache Tomcat 10.x
- Docker e Docker Compose

### Passos

```bash
# 1. Subir o banco de dados
docker-compose up -d

# 2. Build do projeto
mvn clean package

# 3. Deploy: copie o WAR gerado para a pasta webapps do Tomcat
cp target/intercambio-livros.war $TOMCAT_HOME/webapps/
```

A API estará disponível em:
```
http://localhost:8080/intercambio-livros/api/
```

---

## Autenticação JWT

A API é **stateless** — o servidor não guarda sessão. O token JWT deve ser enviado em toda requisição protegida.

### Estrutura do token

| Parte | Conteúdo |
|---|---|
| Header | Algoritmo `HS256` |
| Payload | `userId`, `iss`, `iat`, `exp` (8 horas) |
| Signature | HMAC-SHA256 |

### Como usar

1. Faça `POST /api/login` e copie o campo `token` da resposta.
2. Adicione o header em todas as demais requisições:

```
Authorization: Bearer <token>
```

Rotas sem token (ou com token inválido) retornam `401 Unauthorized`.

---

## Endpoints

### Autenticação

#### `POST /api/login`

Autentica o usuário e retorna o JWT.

**Body:**
```json
{ "email": "joao@email.com", "senha": "123456" }
```

**Resposta `200 OK`:**
```json
{
  "token": "eyJ...",
  "id": 1,
  "nome": "João",
  "email": "joao@email.com"
}
```

**Erros:**
- `400 Bad Request` — email ou senha ausentes
- `401 Unauthorized` — credenciais inválidas

---

### Livros

> Todas as rotas abaixo exigem `Authorization: Bearer <token>`

#### `GET /api/livros`

Lista os livros disponíveis de **outros** usuários.

**Resposta `200 OK`:**
```json
[
  { "id": 3, "titulo": "Dom Casmurro", "autor": "Machado de Assis", "usuarioId": 2 }
]
```

---

#### `GET /api/livros/{id}`

Retorna um livro pelo ID.

**Resposta `200 OK`:**
```json
{ "id": 3, "titulo": "Dom Casmurro", "autor": "Machado de Assis", "usuarioId": 2 }
```

**Erros:** `404 Not Found` — livro inexistente

---

#### `POST /api/livros`

Cadastra um novo livro para o usuário logado.

**Body:**
```json
{ "titulo": "Dom Casmurro", "autor": "Machado de Assis" }
```

**Resposta `201 Created`:**
```json
{ "mensagem": "Livro cadastrado com sucesso" }
```

**Erros:** `400 Bad Request` — título ou autor ausentes

---

#### `PUT /api/livros/{id}`

Substitui título e autor do livro (operação idempotente). O livro deve pertencer ao usuário logado.

**Body:**
```json
{ "titulo": "Dom Casmurro", "autor": "Machado de Assis (ed. 2024)" }
```

**Resposta `200 OK`:**
```json
{ "mensagem": "Livro atualizado com sucesso" }
```

**Erros:**
- `400 Bad Request` — campos inválidos
- `403 Forbidden` — livro pertence a outro usuário
- `404 Not Found` — livro inexistente

---

#### `DELETE /api/livros/{id}`

Remove um livro. O livro deve pertencer ao usuário logado.

**Resposta `204 No Content`** — sem corpo.

**Erros:**
- `403 Forbidden` — livro pertence a outro usuário
- `404 Not Found` — livro inexistente

---

### Trocas

> Todas as rotas abaixo exigem `Authorization: Bearer <token>`

#### `GET /api/trocas`

Retorna as trocas pendentes recebidas e as propostas enviadas pelo usuário logado.

**Resposta `200 OK`:**
```json
{
  "pendentes": [ ... ],
  "propostas": [ ... ]
}
```

---

#### `GET /api/trocas/pendentes`

Lista apenas as propostas de troca **recebidas** com status `PENDENTE`.

**Resposta `200 OK`:**
```json
[
  {
    "id": 5,
    "tituloLivroOferecido": "O Alquimista",
    "tituloLivroRecebido": "Dom Casmurro",
    "nomeSolicitante": "Maria",
    "status": "PENDENTE"
  }
]
```

---

#### `GET /api/trocas/propostas`

Lista apenas as propostas **enviadas** pelo usuário logado.

---

#### `POST /api/trocas`

Propõe uma nova troca.

**Body:**
```json
{ "livroOferecidoId": 2, "livroRecebidoId": 5 }
```

**Resposta `201 Created`:**
```json
{ "mensagem": "Proposta de troca enviada com sucesso" }
```

**Erros:**
- `400 Bad Request` — campos ausentes ou troca consigo mesmo
- `403 Forbidden` — livro oferecido não pertence ao usuário logado
- `404 Not Found` — livro desejado não encontrado

---

#### `PUT /api/trocas/{id}`

Aceita ou recusa uma proposta de troca (operação idempotente).

**Body:**
```json
{ "acao": "aceitar" }
```
ou
```json
{ "acao": "recusar" }
```

**Resposta `200 OK`:**
```json
{ "mensagem": "Troca aceita com sucesso" }
```

Quando aceita, o sistema transfere automaticamente a posse dos dois livros entre os usuários.

**Erros:**
- `400 Bad Request` — acao inválida ou ausente
- `404 Not Found` — troca inexistente

---

## Estrutura de Pastas

```
src/main/java/br/com/livros/
├── config/
│   └── MysqlSingleton.java       # Conexão JDBC com o banco
├── controller/
│   ├── LoginApiController.java   # POST /api/login
│   ├── LivroApiController.java   # /api/livros/*
│   └── TrocaApiController.java   # /api/trocas/*
├── dao/
│   ├── LivroDAO.java
│   ├── TrocaDAO.java
│   └── UsuarioDAO.java
├── filter/
│   └── AuthFilter.java           # Valida JWT em todas as rotas /api/*
├── model/
│   ├── Livro.java
│   ├── Troca.java
│   ├── TrocaDetalhada.java       # DTO com dados enriquecidos de troca
│   └── Usuario.java
├── service/
│   ├── TrocaService.java
│   └── UsuarioService.java
└── util/
    └── JwtUtil.java              # Geração e validação de tokens JWT
```
