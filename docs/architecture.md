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
| Aplicação e domínio | `service`, `entity` | Executar casos de uso, aplicar regras e coordenar alterações relacionadas. |
| Persistência | `repository` | Consultar e persistir entidades com Spring Data JPA. |
| Suporte | `config`, `exception`, `util` | Configurar segurança, padronizar erros e construir paginação. |

O fluxo normal é:

1. Um controller recebe uma requisição e valida o DTO com Bean Validation.
2. O mapper converte o contrato para uma entidade ou resposta.
3. O service executa o caso de uso e verifica invariantes de negócio.
4. O repository acessa o banco; o resultado retorna pelas mesmas camadas.
5. O `GlobalExceptionHandler` converte erros conhecidos em respostas `ApiExceptionDTO`.

## Domínio e consistência

O modelo contém `Book`, `Customer`, `Employee` e `Sale`. Uma venda referencia
um cliente, um funcionário e um livro; registrar venda atualiza a coleção de
compras do cliente na mesma transação.

As regras que precisam continuar verdadeiras ficam nos services, não apenas no
controller: livros exigem título, autor e preço positivo; CPF deve conter onze
dígitos; número de registro do funcionário deve ser positivo; CPFs e números de
registro não podem se repetir; e a venda precisa referenciar registros
existentes. Os DTOs também validam formato e obrigatoriedade para rejeitar
requisições inválidas na borda da API.

`PUT` substitui os campos editáveis do recurso. Os identificadores e relações
gerenciadas pelo sistema não são aceitos pelo DTO; compras são registradas pelo
caso de uso de venda. Os services verificam conflitos antes de salvar e a
camada global traduz conflitos de integridade em HTTP `409`.

## DTOs e mapeamento

Os mappers são classes explícitas, sem dependência de geração de código:

* `BookMapper`, `CustomerMapper`, `EmployeeMapper` e `SaleMapper` convertem
  modelos nos contratos da API.
* DTOs de request não incluem identificadores nem relações que o cliente não
  deve controlar.
* DTOs de response limitam a forma exposta; por exemplo, `SaleResponseDTO`
  apresenta os recursos relacionados como DTOs.
* O mapeamento de uma venda cria referências mínimas a partir dos identificadores
  naturais recebidos; `SaleService` resolve e associa as entidades persistidas.

Manter o mapeamento simples e verificável evita expor detalhes JPA e deixa
explícito onde uma alteração no contrato requer uma alteração no modelo.

## Erros e paginação

`GlobalExceptionHandler` padroniza os erros conhecidos no formato
`{ "status": 400, "message": "...", "field": "..." }`. Requisições de listagem
usam `Pagination` para validar limites e Spring Data para executar a consulta
paginada no banco; `PageResponseDTO` publica conteúdo e metadados, sem expor o
tipo interno `Page`.

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
