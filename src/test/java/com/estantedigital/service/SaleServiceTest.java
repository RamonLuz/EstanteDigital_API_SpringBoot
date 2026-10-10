package com.estantedigital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.estantedigital.entity.ApiException;
import com.estantedigital.entity.Book;
import com.estantedigital.entity.Customer;
import com.estantedigital.entity.Employee;
import com.estantedigital.entity.Sale;
import com.estantedigital.repository.BookRepository;
import com.estantedigital.repository.CustomerRepository;
import com.estantedigital.repository.EmployeeRepository;
import com.estantedigital.repository.SaleRepository;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    private static final String CUSTOMER_CPF = "12345678901";
    private static final String EMPLOYEE_CPF = "12345678902";

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private SaleService saleService;

    @Test
    void shouldListSales() {
        Sale sale = new Sale();
        when(saleRepository.findAll()).thenReturn(List.of(sale));

        List<Sale> result = saleService.listAll();

        assertEquals(1, result.size());
        assertSame(sale, result.get(0));
    }

    @Test
    void shouldFindSaleById() {
        Sale sale = new Sale();
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
    void shouldSaveSaleAndAddBookToCustomerPurchases() {
        List<Book> purchases = new ArrayList<>();
        Customer customer = newCustomer(purchases);
        Book book = newBook("Clean Code", "Robert Martin", 100.0);
        Employee employee = newEmployee(1001);
        Sale sale = newSale(
                new Customer("Requested name", CUSTOMER_CPF, "REQUESTED", null),
                newEmployee(1001),
                newBook("Clean Code", "Robert Martin", 120.0));
        stubExistingReferences(customer, book, employee);
        when(bookRepository.findByTitle(book.getTitle())).thenReturn(List.of(book));
        when(saleRepository.save(sale)).thenReturn(sale);

        Sale result = saleService.save(sale);

        assertSame(sale, result);
        assertSame(customer, sale.getCustomer());
        assertSame(book, sale.getBook());
        assertSame(employee, sale.getEmployee());
        assertEquals(List.of(book), purchases);
        verify(saleRepository).save(sale);
    }

    @Test
    void shouldRejectSaleWhenCustomerDoesNotExist() {
        Sale sale = newSale(newCustomer(null), newEmployee(1001), newBook("Title", "Author", 10.0));
        when(customerRepository.findByCpf(CUSTOMER_CPF)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> saleService.save(sale));

        assertEquals(400, exception.getStatus());
        assertEquals("Customer does not exist", exception.getMessage());
        assertEquals("Body", exception.getField());
        verify(bookRepository, never()).findAll();
        verify(saleRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectSaleWhenBookDoesNotExist() {
        Customer customer = newCustomer(null);
        Sale sale = newSale(customer, newEmployee(1001), newBook("Missing", "Author", 10.0));
        when(customerRepository.findByCpf(CUSTOMER_CPF)).thenReturn(Optional.of(customer));
        when(bookRepository.findAll()).thenReturn(List.of());

        ApiException exception = assertThrows(ApiException.class, () -> saleService.save(sale));

        assertEquals("Book does not exist", exception.getMessage());
        verify(employeeRepository, never()).findAll();
        verify(saleRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectSaleWhenEmployeeDoesNotExist() {
        Customer customer = newCustomer(null);
        Book book = newBook("Clean Code", "Robert Martin", 100.0);
        Sale sale = newSale(customer, newEmployee(1001), book);
        when(customerRepository.findByCpf(CUSTOMER_CPF)).thenReturn(Optional.of(customer));
        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(employeeRepository.findAll()).thenReturn(List.of());

        ApiException exception = assertThrows(ApiException.class, () -> saleService.save(sale));

        assertEquals("Employee does not exist", exception.getMessage());
        verify(saleRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldSaveSaleWhenCustomerHasNoPurchaseList() {
        Customer customer = newCustomer(null);
        Book book = newBook("Clean Code", "Robert Martin", 100.0);
        Employee employee = newEmployee(1001);
        Sale sale = newSale(customer, employee, book);
        stubExistingReferences(customer, book, employee);
        when(bookRepository.findByTitle(book.getTitle())).thenReturn(List.of(book));
        when(saleRepository.save(sale)).thenReturn(sale);

        assertSame(sale, saleService.save(sale));

        verify(saleRepository).save(sale);
    }

    @Test
    void shouldSaveSaleWhenBookTitleLookupIsEmpty() {
        List<Book> purchases = new ArrayList<>();
        Customer customer = newCustomer(purchases);
        Book book = newBook("Clean Code", "Robert Martin", 100.0);
        Employee employee = newEmployee(1001);
        Sale sale = newSale(customer, employee, book);
        stubExistingReferences(customer, book, employee);
        when(bookRepository.findByTitle(book.getTitle())).thenReturn(List.of());
        when(saleRepository.save(sale)).thenReturn(sale);

        assertSame(sale, saleService.save(sale));

        assertTrue(purchases.isEmpty());
        verify(saleRepository).save(sale);
    }

    private void stubExistingReferences(Customer customer, Book book, Employee employee) {
        when(customerRepository.findByCpf(customer.getCpf())).thenReturn(Optional.of(customer));
        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
    }

    private Customer newCustomer(List<Book> purchases) {
        return new Customer("Ana", CUSTOMER_CPF, "ACTIVE", purchases);
    }

    private Book newBook(String title, String author, Double price) {
        return new Book(null, title, author, price);
    }

    private Employee newEmployee(int registrationNumber) {
        return new Employee("Joao", EMPLOYEE_CPF, registrationNumber, "Seller", "ACTIVE");
    }

    private Sale newSale(Customer customer, Employee employee, Book book) {
        return new Sale(null, customer, employee, book);
    }
}