# EstanteDigital API

API REST desenvolvida em **Java + Spring Boot** para gerenciamento de livros, clientes, funcionários e vendas.

O projeto foi criado com o objetivo de praticar conceitos de desenvolvimento backend utilizando Spring Boot, arquitetura em camadas e persistência de dados com JPA.

---

## 🚀 Tecnologias utilizadas

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Bean Validation
* H2 Database
* Maven

---

## 📂 Estrutura do Projeto

```
src
└── main
    ├── java
    │   └── com.estantedigital
    │       ├── controller
    │       ├── dto
    │       ├── entity
    │       ├── exception
    │       ├── repository
    │       ├── service
    │       └── EstanteDigitalApplication
    └── resources
        └── application.yml
```

---

## 📖 Funcionalidades

### Livros

* Cadastrar livro
* Listar livros
* Buscar livro por ID
* Atualizar livro
* Remover livro

### Clientes

* Cadastrar cliente
* Listar clientes
* Buscar cliente por ID
* Atualizar status do cliente
* Remover cliente

### Funcionários

* Cadastrar funcionário
* Listar funcionários
* Buscar funcionário por ID

### Vendas

* Registrar venda
* Listar vendas
* Buscar venda por ID
* Associar livros comprados ao cliente

---

## 🏛️ Arquitetura

O projeto segue uma arquitetura em camadas com separação clara entre API, regras de negócio, persistência e domínio:

```
Controller
    ↓
DTO / Mapper
    ↓
Service
    ↓
Repository
    ↓
Banco de Dados
```

Essa organização ajuda a manter o controlador responsável apenas por receber e responder requisições, deixando a conversão e a lógica de negócio em camadas específicas.

---

## � Padrões e padronização do projeto

Para manter o código consistente e fácil de manter, o projeto segue convenções simples e previsíveis:

* Nomes de classes e métodos em inglês, com camelCase e clareza sem abreviações desnecessárias;
* DTOs para entrada e saída de dados, sem expor entidades do domínio diretamente nas rotas;
* Mapeamento explícito entre DTO e entidade por meio de classes Mapper;
* Mensagens de erro em um único padrão de idioma e estilo;
* Validação de entrada feita em DTOs e regras de negócio em services;
* Testes organizados por camada: mapper, service e integração;
* Documentação alinhada com os endpoints reais implementados.

Essas convenções ajudam a manter a base do projeto mais legível, previsível e fácil de evoluir sem misturar responsabilidades.

---

## �🔗 Relacionamentos

* Um cliente pode possuir vários livros comprados.
* Uma venda possui:

  * Cliente
  * Funcionário
  * Livro
  * Data da venda

---

## ⚠️ Tratamento de exceções

A aplicação utiliza um tratamento global de exceções através do:

```java
@ControllerAdvice
```

Fornecendo respostas padronizadas para:

* Campos obrigatórios não preenchidos;
* Body da requisição ausente;
* Tipos inválidos;
* Regras de negócio da aplicação.

Exemplo:

```json
{
  "status": 400,
  "message": "Cpf deve ter 11 digitos",
  "field": "CPF"
}
```

---

## 🗄️ Banco de dados

O projeto utiliza banco de dados em memória H2.

### Configuração

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:estantedigital
    driver-class-name: org.h2.Driver
    username: sa
    password:
```

### Console H2

```
http://localhost:8080/h2-console
```

---

## 📌 Endpoints

### Livros

| Método | Endpoint       |
| ------ | -------------- |
| GET    | /books         |
| GET    | /books/{id}    |
| POST   | /books         |
| PUT    | /books/{id}    |
| DELETE | /books/{id}    |

---

### Clientes

| Método | Endpoint          |
| ------ | ----------------- |
| GET    | /customers        |
| GET    | /customers/{id}   |
| POST   | /customers        |
| PUT    | /customers/{id}   |
| DELETE | /customers/{id}   |

---

### Funcionários

| Método | Endpoint            |
| ------ | ------------------- |
| GET    | /employees          |
| GET    | /employees/{id}     |
| POST   | /employees          |
| PUT    | /employees/{id}     |
| DELETE | /employees/{id}     |

---

### Vendas

| Método | Endpoint      |
| ------ | ------------- |
| GET    | /sales        |
| GET    | /sales/{id}   |
| POST   | /sales        |

---

## ▶️ Executando o projeto

Clone o repositório:

```bash
git clone https://github.com/RamonLuz/EstanteDigital.git
```

Entre na pasta:

```bash
cd EstanteDigital
```

Execute:

```bash
./mvnw spring-boot:run
```

A aplicação será iniciada em:

```
http://localhost:8080
```

---

## 🎯 Objetivo

Projeto desenvolvido para estudos de Java e Spring Boot, com foco em:

* APIs REST;
* Spring Data JPA;
* Relacionamentos entre entidades;
* DTOs;
* Validações;
* Tratamento de exceções;
* Organização em camadas;
* Boas práticas de desenvolvimento backend.

---

## 👨‍💻 Autor

**Ramon Luz**

QA Engineer | Software Quality Analyst

* Java
* Spring Boot
* Selenium
* Playwright
* Cypress
* Rest Assured
* Python
* SQL
* CI/CD
