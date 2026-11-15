package com.estantedigital.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.core.instrument.Counter;

import com.estantedigital.entity.Book;
import com.estantedigital.entity.Customer;
import com.estantedigital.entity.Employee;
import com.estantedigital.entity.Sale;
import com.estantedigital.exception.ApiException;
import com.estantedigital.repository.BookRepository;
import com.estantedigital.repository.CustomerRepository;
import com.estantedigital.repository.EmployeeRepository;
import com.estantedigital.repository.SaleRepository;

@Service
public class SaleService {

    private static final Logger log = LoggerFactory.getLogger(SaleService.class);

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final BookRepository bookRepository;
    private final SaleRepository saleRepository;
    private final Counter salesCreatedCounter;

    public SaleService(SaleRepository saleRepository, CustomerRepository customerRepository,
            BookRepository bookRepository, EmployeeRepository employeeRepository, Counter salesCreatedCounter) {
        this.employeeRepository = employeeRepository;
        this.bookRepository = bookRepository;
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.salesCreatedCounter = salesCreatedCounter;
    }

    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    public Page<Sale> search(
            String customerCpf,
            String bookTitle,
            Integer employeeRegistrationNumber,
            Pageable pageable) {
        return saleRepository.search(
                normalize(customerCpf),
                normalize(bookTitle),
                employeeRegistrationNumber,
                pageable);
    }

    public Optional<Sale> findById(Long id) {
        return saleRepository.findById(id);
    }

    @Transactional
    public Sale save(CreateSaleCommand command) {
        if (command == null) {
            throw new ApiException(400, "Sale is invalid", "Body");
        }

        Customer customer = customerRepository.findByCpf(command.customerCpf())
                .orElseThrow(() -> new ApiException(400, "Customer does not exist", "Body"));

        Book book = bookRepository.findByTitleAndAuthor(command.bookTitle(), command.bookAuthor())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(400, "Book does not exist", "Body"));

        Employee employee = employeeRepository.findByRegistrationNumber(command.employeeRegistrationNumber())
                .orElseThrow(() -> new ApiException(400, "Employee does not exist", "Body"));

        Sale sale = new Sale(customer, employee, book, command.saleDate());
        customer.registerPurchase(book);
        Sale savedSale = saleRepository.saveAndFlush(sale);
        salesCreatedCounter.increment();
        log.info("Sale registered successfully with id={}", savedSale.getId());
        return savedSale;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
