package com.estantedigital.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.estantedigital.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

	Optional<Customer> findByCpf(String cpf);
}
