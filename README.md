# 📚 Sistema de Intercâmbio de Livros

> Projeto acadêmico — Faculdade  
> **Alunos:** Bruno (1181333) · Lucas (5140155)

Plataforma web que permite a usuários cadastrados listar seus livros e propor trocas com outros usuários da comunidade.

---

## Sumário

- [Descrição](#descrição)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Arquitetura do Projeto](#arquitetura-do-projeto)
- [Banco de Dados](#banco-de-dados)
- [Funcionalidades](#funcionalidades)
- [Como Executar](#como-executar)
- [Estrutura de Pastas](#estrutura-de-pastas)
- [Fluxo de Uso](#fluxo-de-uso)

---

## Descrição

O **Sistema de Intercâmbio de Livros** é uma aplicação web desenvolvida com Java EE (Jakarta EE) que permite:

- Autenticação de usuários cadastrados
- Cadastro de livros disponíveis para troca
- Visualização de livros de outros usuários
- Proposição, aceitação e recusa de trocas entre usuários
- Transferência automática de posse dos livros quando uma troca é aceita

---

## Tecnologias Utilizadas

| Tecnologia | Uso |
|---|---|
| Java 17+ | Linguagem principal (back-end) |
| Jakarta EE (Servlets) | Controladores HTTP |
| JSP + JSTL | Camada de apresentação (front-end) |
| MySQL 8 | Banco de dados relacional |
| JDBC | Acesso ao banco de dados |
| CSS3 | Estilização da interface |
| Apache Tomcat 10+ | Servidor de aplicação |

---

## Arquitetura do Projeto

O projeto segue o padrão **MVC (Model-View-Controller)**:

```
Model       →  Classes Java que representam os dados (Livro, Troca, Usuario...)
View        →  Páginas JSP (index.jsp, principal.jsp, livros.jsp)
Controller  →  Servlets (LivroController, TrocaController, UsuarioController)
```

Também utiliza as camadas **DAO** (acesso ao banco) e **Service** (regras de negócio), separando as responsabilidades:

```
Controller  →  recebe a requisição HTTP
Service     →  aplica validações e regras de negócio
DAO         →  executa as queries no banco de dados
Model       →  transporta os dados entre as camadas
```

---

## Banco de Dados

**Nome do banco:** `intercambio_livros`  
**Porta:** `3307`  
**Usuário/Senha padrão:** `root / root`

### Tabelas

#### `usuarios`
| Coluna | Tipo | Descrição |
|---|---|---|
| id | INT PK AUTO_INCREMENT | Identificador único |
| nome | VARCHAR | Nome do usuário |
| email | VARCHAR | E-mail (usado no login) |
| senha | VARCHAR | Senha do usuário |

#### `livros`
| Coluna | Tipo | Descrição |
|---|---|---|
| id | INT PK AUTO_INCREMENT | Identificador único |
| titulo | VARCHAR | Título do livro |
| autor | VARCHAR | Autor do livro |
| usuario_id | INT FK | Dono atual do livro |

#### `trocas`
| Coluna | Tipo | Descrição |
|---|---|---|
| id | INT PK AUTO_INCREMENT | Identificador único |
| livro_oferecido_id | INT FK | Livro que o solicitante oferece |
| livro_recebido_id | INT FK | Livro que o solicitante deseja receber |
| usuario_solicitante_id | INT FK | Usuário que propôs a troca |
| usuario_solicitado_id | INT FK | Usuário que receberá a proposta |
| status | ENUM | `PENDENTE`, `ACEITA` ou `RECUSADA` |

### Script de criação (exemplo)

```sql
CREATE DATABASE IF NOT EXISTS intercambio_livros;
USE intercambio_livros;

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(100) NOT NULL
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

## Funcionalidades

### Autenticação
- Login com e-mail e senha
- Sessão mantida via `HttpSession`
- Redirecionamento automático para login caso o usuário não esteja autenticado

### Gerenciamento de Livros
- Cadastro de livros (título e autor)
- Listagem dos próprios livros
- Listagem de livros de outros usuários disponíveis para troca

### Sistema de Trocas
- Propor troca: o usuário seleciona um livro seu para oferecer e um livro de outro usuário que deseja
- Visualizar solicitações pendentes recebidas
- Aceitar ou recusar uma proposta de troca
- Quando a troca é **aceita**, o sistema transfere automaticamente a posse dos dois livros entre os usuários
- Acompanhamento do status das próprias propostas (Pendente / Aceita / Recusada)

---

## Como Executar

### Pré-requisitos

- JDK 17 ou superior
- Apache Tomcat 10.x
- MySQL 8.x rodando na porta `3307`
- IDE com suporte a Jakarta EE (Eclipse, IntelliJ IDEA)

### Passos

1. **Clone ou copie o projeto** para sua IDE.

2. **Configure o banco de dados:**
   - Crie o banco `intercambio_livros` no MySQL (porta 3307)
   - Execute o script de criação das tabelas acima
   - Insira ao menos dois usuários para testar as trocas

3. **Verifique as credenciais** em `MysqlSingleton.java`:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3307/intercambio_livros...";
   private static final String USER = "root";
   private static final String PASSWORD = "root";
   ```

4. **Adicione o driver MySQL** (`mysql-connector-j-x.x.x.jar`) ao classpath do projeto (pasta `WEB-INF/lib`).

5. **Faça o deploy** no Tomcat e acesse:
   ```
   http://localhost:8080/<nome-do-contexto>/index.jsp
   ```

---

## Estrutura de Pastas

```
src/
└── main/
    └── java/
        └── br/com/livros/
            ├── config/
            │   └── MysqlSingleton.java      # Conexão singleton com o banco
            ├── controller/
            │   ├── LivroController.java     # Servlet /livros
            │   ├── TrocaController.java     # Servlet /troca
            │   └── UsuarioController.java   # Servlet /usuarios
            ├── dao/
            │   ├── LivroDAO.java            # Queries da tabela livros
            │   ├── TrocaDAO.java            # Queries da tabela trocas
            │   └── UsuarioDAO.java          # Queries da tabela usuarios
            ├── model/
            │   ├── Livro.java
            │   ├── Troca.java
            │   ├── TrocaDetalhada.java      # DTO com dados enriquecidos de troca
            │   └── Usuario.java
            └── service/
                ├── TrocaService.java        # Validações de troca
                └── UsuarioService.java      # Serviços de usuário

WebContent/ (ou src/main/webapp/)
├── index.jsp          # Tela de login
├── principal.jsp      # Dashboard principal (trocas)
├── livros.jsp         # Listagem e proposta de trocas
└── css/
    └── style.css
```

---

## Fluxo de Uso

```
1. Usuário acessa index.jsp → faz login
        ↓
2. Redirecionado para /troca (principal.jsp)
   → Vê solicitações pendentes (pode Aceitar ou Recusar)
   → Vê o status das suas próprias propostas
   → Pode cadastrar um novo livro
        ↓
3. Clica em "Ver Livros para Troca" → /livros (livros.jsp)
   → Vê livros de outros usuários
   → Seleciona um livro seu para oferecer
   → Propõe a troca
        ↓
4. O outro usuário verá a proposta em "Solicitações Pendentes"
   → Se aceitar: os livros trocam de dono automaticamente
   → Se recusar: status atualizado para RECUSADA
```

---

## Observações Técnicas

- A classe `MysqlSingleton` implementa o padrão **Singleton** para gerenciar a instância de conexão, mas cria uma nova `Connection` a cada query — adequado para um projeto acadêmico.
- A autenticação atual armazena senhas em texto puro no banco. Em produção, seria necessário usar hashing (ex: BCrypt).
- O `TrocaService` possui validação para impedir que um usuário troque livros consigo mesmo.
- O `TrocaDetalhada` é um **DTO** (Data Transfer Object) que agrega informações de múltiplas tabelas para exibição na tela.
