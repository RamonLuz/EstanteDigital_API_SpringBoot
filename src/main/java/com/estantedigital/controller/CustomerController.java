package com.estantedigital.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.estantedigital.dto.CustomerRequestDTO;
import com.estantedigital.dto.CustomerResponseDTO;
import com.estantedigital.dto.PageResponseDTO;
import com.estantedigital.entity.Customer;
import com.estantedigital.mapper.CustomerMapper;
import com.estantedigital.service.CustomerService;
import com.estantedigital.util.Pagination;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<CustomerResponseDTO>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = Pagination.request(page, size);
        Page<CustomerResponseDTO> result = customerService.search(name, cpf, status, pageable)
                .map(CustomerMapper::toResponse);
        return ResponseEntity.ok(PageResponseDTO.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getById(@PathVariable Long id) {
        Optional<Customer> customer = customerService.findById(id);

        if (customer.isPresent()) {
            return ResponseEntity.ok(CustomerMapper.toResponse(customer.get()));
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create(@Valid @RequestBody CustomerRequestDTO dto) {
        Customer customer = CustomerMapper.toEntity(dto);

        Customer savedCustomer = customerService.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerMapper.toResponse(savedCustomer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> update(@PathVariable Long id, @Valid @RequestBody CustomerRequestDTO dto) {
        Customer customer = CustomerMapper.toEntity(dto);

        Optional<Customer> updatedCustomer = customerService.updateById(id, customer);

        if (updatedCustomer.isPresent()) {
            return ResponseEntity.ok(CustomerMapper.toResponse(updatedCustomer.get()));
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = customerService.deleteById(id);

        if (removed) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
