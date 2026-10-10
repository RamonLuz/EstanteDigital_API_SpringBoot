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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
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

    @BeforeEach
    void clearDatabase() {
        saleRepository.deleteAll();
        customerRepository.deleteAll();
        employeeRepository.deleteAll();
        bookRepository.deleteAll();
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
                  "customer":{"id":%d,"name":"Ana","cpf":"12345678901","status":"ACTIVE"},
                  "employee":{"id":%d,"name":"Joao","cpf":"12345678902","registrationNumber":1001,"position":"Seller","status":"ACTIVE"},
                  "book":{"id":%d,"title":"Book for sale","author":"Test Author","price":12.5},
                  "saleDate":"2026-10-10"
                }
                """.formatted(customer.get("id").asLong(), employee.get("id").asLong(), book.get("id").asLong());

        MvcResult saleResult = mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sale))
                .andExpect(status().isCreated())
                .andReturn();
        long saleId = objectMapper.readTree(saleResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sale.replace("12345678901", "99999999999")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Customer does not exist"));
        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sale.replace("Book for sale", "Missing book")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Book does not exist"));
        mockMvc.perform(post("/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sale.replace("\"registrationNumber\":1001", "\"registrationNumber\":1002")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Employee does not exist"));

        mockMvc.perform(get("/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(saleId));
        mockMvc.perform(get("/sales/{id}", saleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saleId));
        mockMvc.perform(get("/sales/{id}", saleId + 1))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/customers/{id}", customer.get("id").asLong()))
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
                .andExpect(jsonPath("$[0].id").value(bookId));
        mockMvc.perform(get("/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"));
        mockMvc.perform(get("/books/{id}", bookId + 1))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Clean Code Updated","author":"Robert Martin","price":110.0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code Updated"));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Clean Code Updated","author":"Robert Martin","price":110.0}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Invalid Book","author":"Test Author","price":0}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/books/{id}", bookId))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/books/{id}", bookId))
                .andExpect(status().isNotFound());
    }

    @Test
    void customerEndpointsSupportCrudAndRejectDuplicateCpf() throws Exception {
        JsonNode customer = createResource("/customers", """
                {"name":"Ana","cpf":"12345678901","status":"ACTIVE"}
                """);
        long customerId = customer.get("id").asLong();

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(customerId));
        mockMvc.perform(get("/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("12345678901"));
        mockMvc.perform(get("/customers/{id}", customerId + 1))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Another Name","cpf":"12345678901","status":"ACTIVE"}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Invalid CPF","cpf":"123","status":"ACTIVE"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ana","cpf":"12345678901","status":"INACTIVE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
        mockMvc.perform(delete("/customers/{id}", customerId))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/customers/{id}", customerId))
                .andExpect(status().isNotFound());
    }

    @Test
    void employeeEndpointsSupportCrudAndRejectInvalidCpf() throws Exception {
        JsonNode employee = createResource("/employees", """
                {"name":"Joao","cpf":"12345678902","registrationNumber":1001,"position":"Seller","status":"ACTIVE"}
                """);
        long employeeId = employee.get("id").asLong();

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(employeeId));
        mockMvc.perform(get("/employees/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value(1001));
        mockMvc.perform(get("/employees/{id}", employeeId + 1))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Invalid CPF","cpf":"123","registrationNumber":1002,"position":"Seller","status":"ACTIVE"}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Joao","cpf":"12345678902","registrationNumber":1001,"position":"Seller","status":"INACTIVE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
        mockMvc.perform(delete("/employees/{id}", employeeId))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/employees/{id}", employeeId))
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
        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void endpointsShouldReturnClearErrorForNonNumericIds() throws Exception {
        mockMvc.perform(get("/books/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O ID deve ser numérico"))
                .andExpect(jsonPath("$.field").value("ID"));
        mockMvc.perform(get("/customers/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O ID deve ser numérico"));
        mockMvc.perform(get("/employees/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O ID deve ser numérico"));
        mockMvc.perform(get("/sales/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O ID deve ser numérico"));
    }

    private JsonNode createResource(String path, String body) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}