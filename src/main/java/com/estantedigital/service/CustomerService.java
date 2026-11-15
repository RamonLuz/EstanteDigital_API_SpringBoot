package com.estantedigital.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.estantedigital.entity.Customer;
import com.estantedigital.exception.ApiException;
import com.estantedigital.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer save(Customer customer) {
        if (customer == null) {
            throw new ApiException(400, "Customer is invalid", "Body");
        }

        validate(customer);
        if (customerRepository.existsByCpf(customer.getCpf())) {
            throw new ApiException(409, "CPF already registered", "CPF");
        }

        return customerRepository.save(customer);
    }

    private void validate(Customer customer) {
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new ApiException(400, "Name is required", "name");
        }
        if (customer.getCpf() == null || !customer.getCpf().matches("\\d{11}")) {
            throw new ApiException(400, "CPF must have 11 digits", "CPF");
        }
        if (!"ACTIVE".equals(customer.getStatus()) && !"INACTIVE".equals(customer.getStatus())) {
            throw new ApiException(400, "Status must be ACTIVE or INACTIVE", "status");
        }
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Page<Customer> search(String name, String cpf, String status, Pageable pageable) {
        return customerRepository.search(normalize(name), normalize(cpf), normalize(status), pageable);
    }

    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> findByCpf(String cpf) {
        return customerRepository.findByCpf(cpf);
    }

    public Optional<Customer> updateById(Long id, Customer updatedCustomer) {
        if (updatedCustomer == null) {
            throw new ApiException(400, "Customer is invalid", "Body");
        }
        validate(updatedCustomer);

        Optional<Customer> existingCustomer = customerRepository.findById(id);

        if (existingCustomer.isPresent()) {
            Customer customer = existingCustomer.get();
            if (!customer.getCpf().equals(updatedCustomer.getCpf())
                    && customerRepository.existsByCpfAndIdNot(updatedCustomer.getCpf(), id)) {
                throw new ApiException(409, "CPF already registered", "CPF");
            }
            customer.setName(updatedCustomer.getName());
            customer.setCpf(updatedCustomer.getCpf());
            customer.setStatus(updatedCustomer.getStatus());
            customerRepository.save(customer);
            return Optional.of(customer);
        }

        return Optional.empty();
    }

    public boolean deleteById(Long id) {
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            return true;
        }

        return false;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}