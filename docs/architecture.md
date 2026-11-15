# Arquitetura e domínio

A aplicação é um monólito Spring Boot organizado por responsabilidade. As camadas
compartilham o mesmo processo e banco de dados; não são serviços implantáveis
separadamente.

## Camadas

| Camada | Pacote | Responsabilidade |
| --- | --- | --- |
| API | `controller` | Receber requisições HTTP, validar DTOs, definir status HTTP e delegar casos de uso. |
| Contratos | `dto` | Definir formatos de entrada e saída sem expor entidades JPA diretamente. |
| Mapeamento | `mapper` | Converter explicitamente DTOs em entidades e entidades em DTOs. |
| Aplicação | `service` | Executar casos de uso, coordenar repositórios e transações. |
| Domínio | `entity` | Manter invariantes e operações próprias dos agregados. |
| Persistência | `repository` | Consultar e persistir entidades com Spring Data JPA. |
| Suporte | `config`, `exception`, `util` | Configurar segurança, padronizar erros e construir paginação. |

O fluxo normal é:

1. Um controller recebe uma requisição e valida o DTO com Bean Validation.
2. O mapper converte o contrato para uma entidade ou resposta.
3. O service executa o caso de uso e coordena as operações de domínio.
4. O repository acessa o banco; o resultado retorna pelas mesmas camadas.
5. O `GlobalExceptionHandler` converte erros conhecidos em respostas `ApiExceptionDTO`.

## Domínio e consistência

O modelo contém `Book`, `Customer`, `Employee` e `Sale`. Uma venda referencia
um cliente, um funcionário e um livro; registrar venda atualiza a coleção de
compras do cliente na mesma transação.

O domínio é rico nas regras essenciais, em vez de depender de setters públicos
e validações duplicadas nos services. `Book.updateDetails` valida os dados do
livro; `Customer.updateProfile`, `Customer.updateStatus` e
`Customer.registerPurchase` controlam identidade, estado e compras;
`Employee.updateDetails` protege seus dados e número de registro; `Sale`
exige cliente, funcionário, livro e data válidos. Violações lançam
`DomainRuleViolationException`, que o tratamento global converte em HTTP 400.

Os services continuam responsáveis por regras que exigem persistência, como
detectar duplicidades e resolver os registros associados. O banco também impõe
unicidade para livros por título e autor, CPF e número de registro, evitando
duplicidades em chamadas concorrentes. A criação de venda é um caso de uso
transacional e registra as compras pelo comportamento do cliente.

`PUT` substitui os campos editáveis do recurso. Os identificadores e relações
gerenciadas pelo sistema não são aceitos pelo DTO; compras são registradas pelo
comando de criação de venda. O `CreateSaleCommand` separa o contrato HTTP do
caso de uso; controllers, mappers, services e repositories mantêm
responsabilidades próprias.

## DTOs e mapeamento

Os mappers são classes explícitas, sem dependência de geração de código:

* `BookMapper`, `CustomerMapper`, `EmployeeMapper` e `SaleMapper` convertem
  modelos nos contratos da API.
* DTOs de request não incluem identificadores nem relações que o cliente não
  deve controlar.
* DTOs de response limitam a forma exposta; por exemplo, `SaleResponseDTO`
  apresenta os recursos relacionados como DTOs.
* `SaleMapper` converte o DTO em `CreateSaleCommand`; `SaleService` resolve as
  referências persistidas antes de construir a entidade `Sale`.

Manter o mapeamento simples e verificável evita expor detalhes JPA e deixa
explícito onde uma alteração no contrato requer uma alteração no modelo.

## Erros e paginação

`GlobalExceptionHandler` padroniza os erros conhecidos no formato
`{ "status": 400, "message": "...", "field": "..." }`. Requisições de listagem
usam `Pagination` para validar limites e Spring Data para executar a consulta
paginada no banco; `PageResponseDTO` publica conteúdo e metadados, sem expor o
tipo interno `Page`.

## Resiliência e observabilidade

A aplicação não tem dependências de rede downstream; por isso, retry e
circuit-breaker não são aplicados artificialmente ao acesso local ao banco.
As vendas são transacionais, o banco reforça restrições de integridade, o
Actuator expõe health/liveness/readiness e o servidor encerra graciosamente.

Spring Boot Actuator e Micrometer publicam métricas HTTP e da JVM em
`/actuator/metrics`. Um contador `estantedigital.sales.created` incrementa
somente depois que a venda é persistida. O log da criação registra o ID da
venda sem incluir CPF ou outros dados pessoais; o nível SQL não fica em DEBUG
por padrão.

## Migrações e containers

Flyway mantém o schema em `src/main/resources/db/migration` e aplica scripts
versionados antes da validação de schema pelo Hibernate. `ddl-auto: validate`
impede que a aplicação faça alterações implícitas nas tabelas. A imagem Docker
multi-stage continua executando como usuário não privilegiado; o Compose guarda
o banco H2 em arquivo num volume nomeado para preservar dados entre recriações
de container.

## Segurança e execução

O projeto é uma aplicação de estudo: todas as rotas estão públicas por decisão
atual do projeto. `POST /auth/login` e a emissão/validação JWT são demonstração
opcional e não protegem os demais endpoints. Não exponha os dados pessoais nem
essa configuração em uma rede não confiável.

## Evolução recomendada

Ao adicionar uma regra, cubra-a no service e em teste unitário; cubra também a
rota e o contrato HTTP com teste de integração. Se o domínio crescer, extraia
tipos de valor para conceitos com invariantes próprias (como CPF), sem misturar
anotações HTTP ou persistência com as regras do negócio. Para o inventário
detalhado dos contratos e fluxos, veja
[api-service-contracts.md](../.github/modernize/assessment/engines/facts/api-service-contracts.md).
