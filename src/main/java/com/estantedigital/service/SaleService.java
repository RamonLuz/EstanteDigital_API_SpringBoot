package com.estantedigital.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

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

    public Optional<Sale> findById(Long id) {
        return saleRepository.findById(id);
    }

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

        syncCustomerPurchases(sale);
        return saleRepository.save(sale);
    }

    private void syncCustomerPurchases(Sale sale) {
        Optional<Customer> existingCustomer = customerRepository.findByCpf(sale.getCustomer().getCpf());

        if (existingCustomer.isPresent()) {
            Book book = bookRepository.findByTitleAndAuthor(sale.getBook().getTitle(), sale.getBook().getAuthor())
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (book != null) {
                Customer customer = existingCustomer.get();
                if (customer.getPurchases() != null) {
                    customer.getPurchases().add(book);
                }
            }
        }
    }
}
