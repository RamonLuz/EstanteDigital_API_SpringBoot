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
* Spring Security com JWT Bearer
* Springdoc OpenAPI / Swagger UI
* Flyway para migrações versionadas
* Spring Boot Actuator e Micrometer
* H2 Database
* Docker e Docker Compose
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
* Atualizar cliente
* Remover cliente

### Funcionários

* Cadastrar funcionário
* Listar funcionários
* Buscar funcionário por ID
* Atualizar funcionário
* Remover funcionário

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

Para detalhes sobre responsabilidades, fluxo, contratos e modelo de dados, consulte [docs/architecture.md](docs/architecture.md) e o contrato OpenAPI em [docs/openapi.yaml](docs/openapi.yaml). A documentação interativa gerada pelo Springdoc fica disponível em `http://localhost:8080/swagger-ui/index.html` e o contrato JSON em `http://localhost:8080/v3/api-docs`. O Springdoc é uma dependência open source gratuita; não requer assinatura nem serviço externo.

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

* Campos obrigatórios, formatos inválidos e valores fora do intervalo esperado;
* Body ausente ou JSON inválido;
* Parâmetros de rota com tipos inválidos;
* Conflitos de integridade do banco de dados;
* Regras de negócio da aplicação.

Exemplo:

```json
{
  "status": 400,
  "message": "cpf: CPF must contain exactly 11 digits",
  "field": "cpf"
}
```

As validações são feitas com Jakarta Bean Validation (`@Valid`) nos DTOs. Erros
de validação listam os campos e mensagens no campo `message`; `field` informa o
primeiro campo. A dependência já incluída no projeto é open source e não requer
assinatura de serviço externo.

---

## 🗄️ Banco de dados e migrações

O projeto usa H2. Ao iniciar localmente, o padrão é um banco em memória; no
Docker Compose, o banco usa arquivo no volume nomeado `estantedigital_data`,
preservando os dados entre reinicializações dos containers.

As alterações do schema são versionadas em `src/main/resources/db/migration`
com Flyway. O Hibernate está em modo `validate`: ele verifica a compatibilidade
das entidades com o schema, mas não cria nem altera tabelas automaticamente.
Crie uma nova migração `V2__descricao.sql`, `V3__descricao.sql` etc. para cada
alteração futura; não edite uma migração que já foi aplicada em ambientes
compartilhados.

### Configuração

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:estantedigital;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
```

É possível substituir a URL pelo ambiente `DB_URL`.

### Console H2

O console H2 está desativado; mantenha-o assim fora de um ambiente local
isolado.

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

### Sistema e autenticação demonstrativa

| Método | Endpoint |
| ------ | -------- |
| GET    | /health |
| POST   | /auth/login |

O token retornado por `/auth/login` não é obrigatório nas demais rotas.

---

## 📄 Paginação e filtros

As listagens `GET /books`, `/customers`, `/employees` e `/sales` retornam uma
resposta paginada. `page` começa em `0`; `size` aceita valores de `1` a `100`.
Sem parâmetros, a API retorna a primeira página com até 10 itens, ordenada por ID.

Exemplos:

```text
GET /books?page=0&size=10&title=clean&author=martin
GET /customers?page=0&size=10&name=ana&status=ACTIVE
GET /employees?page=1&size=20&cpf=123&registrationNumber=1001
GET /sales?page=0&size=10&customerCpf=123&bookTitle=clean
```

Filtros de texto por nome, título e autor não diferenciam maiúsculas de
minúsculas e aceitam correspondência parcial. CPF permite correspondência
parcial; status e número de registro são comparações exatas.

A resposta contém `content`, `page`, `size`, `totalElements`, `totalPages`,
`first` e `last`. As consultas são paginadas no banco de dados via Spring Data
JPA, sem serviços externos ou dependências pagas.

---

## 🔐 Autenticação e autorização

A API usa Spring Security com JWT Bearer assinado por HMAC-SHA256. Não são aceitos
tokens sem assinatura. O endpoint `POST /auth/login` valida o usuário configurado
no ambiente e retorna um token válido por uma hora.

Todas as rotas são públicas e podem ser chamadas sem login ou token. O endpoint
de login e o JWT ficam disponíveis apenas para demonstrar autenticação; enviar
um token válido não é necessário para usar a API. Os dados de clientes,
funcionários e vendas também ficam expostos neste modo de estudo.

As variáveis são opcionais no modo de estudo: sem configurá-las, o login
demonstrativo usa `student` / `student` e uma chave local pública, inadequada
para produção. Configure suas próprias variáveis para substituir esses valores:

| Variável | Descrição |
| -------- | --------- |
| `APP_AUTH_USERNAME` | Usuário permitido para autenticação |
| `APP_AUTH_PASSWORD` | Senha do usuário; armazenada em memória com hash BCrypt |
| `APP_JWT_SECRET` | Chave HMAC em Base64 com pelo menos 32 bytes |

Gere uma chave com `openssl rand -base64 32`. Não reutilize a chave de
desenvolvimento nem armazene credenciais privadas no repositório. O console H2
fica desabilitado. Não publique esta configuração em produção: operações e
dados estão acessíveis sem autenticação.

Exemplo PowerShell:

```powershell
$env:APP_AUTH_USERNAME = "admin"
$env:APP_AUTH_PASSWORD = "<sua-senha-segura>"
$env:APP_JWT_SECRET = "<chave-base64-gerada>"
.\mvnw.cmd spring-boot:run
```

Opcionalmente, obtenha um token para demonstrar o JWT:

```http
POST /auth/login
Content-Type: application/json

{"username":"admin","password":"<sua-senha-segura>"}
```

O token retornado pode ser enviado como Bearer para demonstrar o uso de JWT,
mas não é exigido pelas rotas:

```http
Authorization: Bearer <accessToken>
```

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

Execute (o modo público de estudo inicia sem configuração adicional):

```bash
./mvnw spring-boot:run
```

A aplicação será iniciada em:

```
http://localhost:8080
```

### Executando com Docker Compose

Com Docker Desktop (ou Docker Engine com o plugin Compose) instalado, na raiz
do projeto execute:

```powershell
docker compose up --build
```

A API ficará disponível em `http://localhost:8080`. Para parar e remover o
container, pressione `Ctrl+C` e execute `docker compose down`. A configuração
usa H2 em arquivo dentro do volume nomeado, então os dados permanecem após
recriar o container.
As credenciais e a chave JWT padrão são apenas para estudo. Defina as variáveis
de ambiente antes de iniciar se desejar substituí-las. Como os endpoints estão
públicos neste modo, não exponha o serviço a redes não confiáveis.

### Saúde e métricas

O Actuator expõe os endpoints `GET /actuator/health`,
`GET /actuator/health/liveness`, `GET /actuator/health/readiness`,
`GET /actuator/info` e `GET /actuator/metrics`. A métrica
`estantedigital.sales.created` conta vendas persistidas; métricas HTTP e da JVM
são registradas pelo Micrometer. O projeto não usa Resilience4j porque não há
chamadas de rede para outros serviços; vendas usam transação e constraints do
banco para proteger consistência.

### Executando os testes

Para executar testes unitários, de integração e as verificações Maven:

```powershell
.\mvnw.cmd clean verify
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
