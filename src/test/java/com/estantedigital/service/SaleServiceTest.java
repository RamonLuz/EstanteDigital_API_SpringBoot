package com.estantedigital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyInt;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.estantedigital.entity.Book;
import com.estantedigital.entity.Customer;
import com.estantedigital.entity.Employee;
import com.estantedigital.entity.Sale;
import com.estantedigital.exception.ApiException;
import com.estantedigital.repository.BookRepository;
import com.estantedigital.repository.CustomerRepository;
import com.estantedigital.repository.EmployeeRepository;
import com.estantedigital.repository.SaleRepository;

import io.micrometer.core.instrument.Counter;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    private static final String CUSTOMER_CPF = "12345678901";
    private static final String EMPLOYEE_CPF = "12345678902";
    private static final LocalDate SALE_DATE = LocalDate.of(2026, 10, 11);

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private Counter salesCreatedCounter;

    @InjectMocks
    private SaleService saleService;

    @Test
    void shouldListSales() {
        Sale sale = newSale(newCustomer(), newEmployee(1001), newBook("Clean Code", "Robert Martin"));
        when(saleRepository.findAll()).thenReturn(List.of(sale));

        List<Sale> result = saleService.findAll();

        assertEquals(1, result.size());
        assertSame(sale, result.get(0));
    }

    @Test
    void shouldFindSaleById() {
        Sale sale = newSale(newCustomer(), newEmployee(1001), newBook("Clean Code", "Robert Martin"));
        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        Optional<Sale> result = saleService.findById(1L);

        assertTrue(result.isPresent());
        assertSame(sale, result.get());
    }

    @Test
    void shouldReturnEmptyWhenSaleDoesNotExist() {
        when(saleRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Sale> result = saleService.findById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldSearchSalesByCustomerBookAndEmployee() {
        Pageable pageable = PageRequest.of(0, 10);
        Sale sale = newSale(newCustomer(), newEmployee(1001), newBook("Clean Code", "Robert Martin"));
        Page<Sale> expected = new PageImpl<>(List.of(sale), pageable, 1);
        when(saleRepository.search("123", "clean", 1001, pageable)).thenReturn(expected);

        Page<Sale> result = saleService.search(" 123 ", " clean ", 1001, pageable);

        assertEquals(expected, result);
        verify(saleRepository).search("123", "clean", 1001, pageable);
    }

    @Test
    void shouldRegisterSaleAndTrackMetric() {
        Customer customer = newCustomer();
        Book book = newBook("Clean Code", "Robert Martin");
        Employee employee = newEmployee(1001);
        stubExistingReferences(customer, book, employee);
        when(saleRepository.saveAndFlush(any(Sale.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Sale result = saleService.save(newCommand(CUSTOMER_CPF, 1001, "Clean Code", "Robert Martin"));

        assertSame(customer, result.getCustomer());
        assertSame(book, result.getBook());
        assertSame(employee, result.getEmployee());
        assertEquals(List.of(book), customer.getPurchases());
        verify(salesCreatedCounter).increment();
    }

    @Test
    void shouldRejectSaleWhenCustomerDoesNotExist() {
        when(customerRepository.findByCpf(CUSTOMER_CPF)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> saleService.save(newCommand(CUSTOMER_CPF, 1001, "Clean Code", "Robert Martin")));

        assertEquals(400, exception.getStatus());
        assertEquals("Customer does not exist", exception.getMessage());
        assertEquals("Body", exception.getField());
        verify(bookRepository, never()).findByTitleAndAuthor(any(String.class), any(String.class));
        verify(saleRepository, never()).saveAndFlush(any());
        verify(salesCreatedCounter, never()).increment();
    }

    @Test
    void shouldRejectSaleWhenBookDoesNotExist() {
        Customer customer = newCustomer();
        when(customerRepository.findByCpf(CUSTOMER_CPF)).thenReturn(Optional.of(customer));
        when(bookRepository.findByTitleAndAuthor("Missing", "Author")).thenReturn(List.of());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> saleService.save(newCommand(CUSTOMER_CPF, 1001, "Missing", "Author")));

        assertEquals("Book does not exist", exception.getMessage());
        verify(employeeRepository, never()).findByRegistrationNumber(anyInt());
        verify(saleRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectSaleWhenEmployeeDoesNotExist() {
        Customer customer = newCustomer();
        Book book = newBook("Clean Code", "Robert Martin");
        when(customerRepository.findByCpf(CUSTOMER_CPF)).thenReturn(Optional.of(customer));
        when(bookRepository.findByTitleAndAuthor(book.getTitle(), book.getAuthor())).thenReturn(List.of(book));
        when(employeeRepository.findByRegistrationNumber(1001)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> saleService.save(newCommand(CUSTOMER_CPF, 1001, book.getTitle(), book.getAuthor())));

        assertEquals("Employee does not exist", exception.getMessage());
        verify(saleRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldRejectNullCommand() {
        ApiException exception = assertThrows(ApiException.class, () -> saleService.save(null));

        assertEquals(400, exception.getStatus());
        assertEquals("Sale is invalid", exception.getMessage());
        assertEquals("Body", exception.getField());
        verify(saleRepository, never()).saveAndFlush(any());
    }

    private void stubExistingReferences(Customer customer, Book book, Employee employee) {
        when(customerRepository.findByCpf(customer.getCpf())).thenReturn(Optional.of(customer));
        when(bookRepository.findByTitleAndAuthor(book.getTitle(), book.getAuthor())).thenReturn(List.of(book));
        when(employeeRepository.findByRegistrationNumber(employee.getRegistrationNumber()))
                .thenReturn(Optional.of(employee));
    }

    private CreateSaleCommand newCommand(String cpf, int registrationNumber, String title, String author) {
        return new CreateSaleCommand(cpf, registrationNumber, title, author, SALE_DATE);
    }

    private Customer newCustomer() {
        return new Customer("Ana", CUSTOMER_CPF, "ACTIVE");
    }

    private Book newBook(String title, String author) {
        return new Book(title, author, 100.0);
    }

    private Employee newEmployee(int registrationNumber) {
        return new Employee("Joao", EMPLOYEE_CPF, registrationNumber, "Seller", "ACTIVE");
    }

    private Sale newSale(Customer customer, Employee employee, Book book) {
        return new Sale(customer, employee, book, SALE_DATE);
    }
}
