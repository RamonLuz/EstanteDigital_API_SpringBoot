package com.estantedigital.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final BookRepository bookRepository;
    private final SaleRepository saleRepository;

    public SaleService(SaleRepository saleRepository, CustomerRepository customerRepository,
            BookRepository bookRepository, EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
        this.bookRepository = bookRepository;
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
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
    public Sale save(Sale sale) {
        if (sale == null || sale.getCustomer() == null || sale.getBook() == null || sale.getEmployee() == null) {
            throw new ApiException(400, "Sale is invalid", "Body");
        }

        Customer customer = customerRepository.findByCpf(sale.getCustomer().getCpf())
                .orElseThrow(() -> new ApiException(400, "Customer does not exist", "Body"));

        Book book = bookRepository.findByTitleAndAuthor(sale.getBook().getTitle(), sale.getBook().getAuthor())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(400, "Book does not exist", "Body"));

        Employee employee = employeeRepository.findByRegistrationNumber(sale.getEmployee().getRegistrationNumber())
                .orElseThrow(() -> new ApiException(400, "Employee does not exist", "Body"));

        sale.setCustomer(customer);
        sale.setBook(book);
        sale.setEmployee(employee);

        if (customer.getPurchases() == null) {
            customer.setPurchases(new ArrayList<>());
        }
        customer.getPurchases().add(book);
        return saleRepository.save(sale);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
