package com.estantedigital;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.estantedigital.repository.BookRepository;
import com.estantedigital.repository.CustomerRepository;
import com.estantedigital.repository.EmployeeRepository;
import com.estantedigital.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "app.security.username=test-user",
        "app.security.password=test-password",
        "app.security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private BookRepository bookRepository;

    private String accessToken;

    @BeforeEach
    void clearDatabase() throws Exception {
        saleRepository.deleteAll();
        customerRepository.deleteAll();
        employeeRepository.deleteAll();
        bookRepository.deleteAll();

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"test-user","password":"test-password"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @Test
    void loginShouldIssueSignedTokenWhileApiRoutesRemainPublic() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Protected","author":"Test Author","price":10.0}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"test-user","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(authenticated(get("/customers")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/customers")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0LXVzZXIifQ."))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dtoValidationShouldReturnFieldSpecificErrors() throws Exception {
        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana","cpf":"123","status":"ACTIVE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("cpf"))
                .andExpect(jsonPath("$.message").value("cpf: CPF must contain exactly 11 digits"));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Invalid price","author":"Test Author","price":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("price"))
                .andExpect(jsonPath("$.message").value("price: Price must be greater than zero"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":" ","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("password"))
                .andExpect(jsonPath("$.message").value(
                        "password: Password is required; username: Username is required"));
    }

    @Test
    void malformedRequestBodyShouldReturnStandardError() throws Exception {
        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerCpf":"12345678901","employeeRegistrationNumber":1,
                                 "bookTitle":"Title","bookAuthor":"Author","saleDate":"not-a-date"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.field").value("Body"))
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
    }

    @Test
    void saleShouldPersistPurchasedBookOnCustomer() throws Exception {
        JsonNode book = createResource("/books", """
                {"title":"Book for sale","author":"Test Author","price":12.5}
                """);
        JsonNode customer = createResource("/customers", """
                {"name":"Ana","cpf":"12345678901","status":"ACTIVE"}
                """);
        JsonNode employee = createResource("/employees", """
                {"name":"Joao","cpf":"12345678902","registrationNumber":1001,"position":"Seller","status":"ACTIVE"}
                """);

        String sale = """
                {
                  "customerCpf":"12345678901",
                  "employeeRegistrationNumber":1001,
                  "bookTitle":"Book for sale",
                  "bookAuthor":"Test Author",
                  "saleDate":"2026-10-10"
                }
                """;

        MvcResult saleResult = mockMvc.perform(authenticated(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sale)))
                .andExpect(status().isCreated())
                .andReturn();
        long saleId = objectMapper.readTree(saleResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(authenticated(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sale.replace("12345678901", "99999999999"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Customer does not exist"));
        mockMvc.perform(authenticated(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sale.replace("Book for sale", "Missing book"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Book does not exist"));
        mockMvc.perform(authenticated(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sale.replace("\"employeeRegistrationNumber\":1001", "\"employeeRegistrationNumber\":1002"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Employee does not exist"));

        mockMvc.perform(authenticated(get("/sales")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(saleId));
        mockMvc.perform(authenticated(get("/sales/{id}", saleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saleId));
        mockMvc.perform(authenticated(get("/sales/{id}", saleId + 1)))
                .andExpect(status().isNotFound());

        mockMvc.perform(authenticated(get("/customers/{id}", customer.get("id").asLong())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purchases[0].id").value(book.get("id").asLong()));
    }

    @Test
    void bookEndpointsSupportCrudAndRejectInvalidOrDuplicateBooks() throws Exception {
        JsonNode book = createResource("/books", """
                {"title":"Clean Code","author":"Robert Martin","price":100.0}
                """);
        long bookId = book.get("id").asLong();

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(bookId));
        mockMvc.perform(get("/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"));
        mockMvc.perform(get("/books/{id}", bookId + 1))
                .andExpect(status().isNotFound());

        mockMvc.perform(authenticated(put("/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Clean Code Updated","author":"Robert Martin","price":110.0}
                                """)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code Updated"));

        mockMvc.perform(authenticated(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Clean Code Updated","author":"Robert Martin","price":110.0}
                                """)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(authenticated(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Invalid Book","author":"Test Author","price":0}
                                """)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(authenticated(delete("/books/{id}", bookId)))
                .andExpect(status().isNoContent());
        mockMvc.perform(authenticated(delete("/books/{id}", bookId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void customerEndpointsSupportCrudAndRejectDuplicateCpf() throws Exception {
        JsonNode customer = createResource("/customers", """
                {"name":"Ana","cpf":"12345678901","status":"ACTIVE"}
                """);
        long customerId = customer.get("id").asLong();

        mockMvc.perform(authenticated(get("/customers")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(customerId));
        mockMvc.perform(authenticated(get("/customers/{id}", customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("12345678901"));
        mockMvc.perform(authenticated(get("/customers/{id}", customerId + 1)))
                .andExpect(status().isNotFound());

        mockMvc.perform(authenticated(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Another Name","cpf":"12345678901","status":"ACTIVE"}
                                """)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(authenticated(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Invalid CPF","cpf":"123","status":"ACTIVE"}
                                """)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(authenticated(put("/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana","cpf":"12345678901","status":"INACTIVE"}
                                """)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
        mockMvc.perform(authenticated(delete("/customers/{id}", customerId)))
                .andExpect(status().isNoContent());
        mockMvc.perform(authenticated(delete("/customers/{id}", customerId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void employeeEndpointsSupportCrudAndRejectInvalidCpf() throws Exception {
        JsonNode employee = createResource("/employees", """
                {"name":"Joao","cpf":"12345678902","registrationNumber":1001,"position":"Seller","status":"ACTIVE"}
                """);
        long employeeId = employee.get("id").asLong();

        mockMvc.perform(authenticated(get("/employees")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(employeeId));
        mockMvc.perform(authenticated(get("/employees/{id}", employeeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value(1001));
        mockMvc.perform(authenticated(get("/employees/{id}", employeeId + 1)))
                .andExpect(status().isNotFound());

        mockMvc.perform(authenticated(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Invalid CPF","cpf":"123","registrationNumber":1002,"position":"Seller","status":"ACTIVE"}
                                """)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(authenticated(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Duplicate registration","cpf":"12345678903","registrationNumber":1001,"position":"Seller","status":"ACTIVE"}
                                """)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.field").value("registrationNumber"));
        mockMvc.perform(authenticated(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Duplicate CPF","cpf":"12345678902","registrationNumber":1003,"position":"Seller","status":"ACTIVE"}
                                """)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.field").value("CPF"));
        mockMvc.perform(authenticated(put("/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Joao","cpf":"12345678902","registrationNumber":1001,"position":"Seller","status":"INACTIVE"}
                                """)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
        mockMvc.perform(authenticated(delete("/employees/{id}", employeeId)))
                .andExpect(status().isNoContent());
        mockMvc.perform(authenticated(delete("/employees/{id}", employeeId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void healthEndpointReportsApplicationAvailability() throws Exception {
        mockMvc.perform(get("/health"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("UP"))
                                .andExpect(jsonPath("$.application").value("estantedigital"));
    }

    @Test
    void emptyCustomerCollectionShouldReturnAnEmptyList() throws Exception {
        mockMvc.perform(authenticated(get("/customers")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void bookListingShouldSupportPaginationAndFilters() throws Exception {
        createResource("/books", """
                {"title":"Clean Code","author":"Robert Martin","price":100.0}
                """);
        createResource("/books", """
                {"title":"Clean Architecture","author":"Robert Martin","price":120.0}
                """);
        createResource("/books", """
                {"title":"Effective Java","author":"Joshua Bloch","price":90.0}
                """);

        mockMvc.perform(get("/books")
                        .param("author", "robert")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].author").value("Robert Martin"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/books")
                        .param("title", "effective")
                        .param("author", "bloch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Effective Java"));

        mockMvc.perform(get("/books")
                        .param("author", "robert")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Clean Architecture"))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void customerListingShouldSupportFilterAndRejectInvalidPageSize() throws Exception {
        createResource("/customers", """
                {"name":"Ana Silva","cpf":"12345678901","status":"ACTIVE"}
                """);
        createResource("/customers", """
                {"name":"Bruno Lima","cpf":"12345678902","status":"INACTIVE"}
                """);

        mockMvc.perform(authenticated(get("/customers")
                        .param("name", "ana")
                        .param("status", "ACTIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Ana Silva"))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/books").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("size"))
                .andExpect(jsonPath("$.message").value("Size must be between 1 and 100"));
    }

    @Test
    void employeeListingShouldSupportPaginationAndFilters() throws Exception {
        createResource("/employees", """
                {"name":"Joao Silva","cpf":"12345678901","registrationNumber":1001,"position":"Seller","status":"ACTIVE"}
                """);
        createResource("/employees", """
                {"name":"Ana Souza","cpf":"12345678902","registrationNumber":1002,"position":"Manager","status":"INACTIVE"}
                """);

        mockMvc.perform(get("/employees")
                        .param("name", "ana")
                        .param("status", "INACTIVE")
                        .param("registrationNumber", "1002")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Ana Souza"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void saleListingShouldFilterByCustomerBookAndEmployee() throws Exception {
        createResource("/books", """
                {"title":"Clean Code","author":"Robert Martin","price":100.0}
                """);
        createResource("/customers", """
                {"name":"Ana","cpf":"12345678901","status":"ACTIVE"}
                """);
        createResource("/employees", """
                {"name":"Joao","cpf":"12345678902","registrationNumber":1001,"position":"Seller","status":"ACTIVE"}
                """);
        mockMvc.perform(authenticated(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerCpf":"12345678901","employeeRegistrationNumber":1001,
                                 "bookTitle":"Clean Code","bookAuthor":"Robert Martin","saleDate":"2026-10-10"}
                                """)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/sales")
                        .param("customerCpf", "123")
                        .param("bookTitle", "clean")
                        .param("employeeRegistrationNumber", "1001")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].book.title").value("Clean Code"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void endpointsShouldReturnClearErrorForNonNumericIds() throws Exception {
        mockMvc.perform(get("/books/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be numeric"))
                .andExpect(jsonPath("$.field").value("ID"));
        mockMvc.perform(authenticated(get("/customers/not-a-number")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be numeric"));
        mockMvc.perform(authenticated(get("/employees/not-a-number")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be numeric"));
        mockMvc.perform(authenticated(get("/sales/not-a-number")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ID must be numeric"));
    }

    private JsonNode createResource(String path, String body) throws Exception {
        MvcResult result = mockMvc.perform(authenticated(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder request) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }
}