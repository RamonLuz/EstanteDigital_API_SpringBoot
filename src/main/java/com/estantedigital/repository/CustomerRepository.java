package com.estantedigital.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.estantedigital.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

	Optional<Customer> findByCpf(String cpf);

	@Query("""
			select c from Customer c
			where (:name is null or lower(c.name) like lower(concat('%', :name, '%')))
			  and (:cpf is null or c.cpf like concat('%', :cpf, '%'))
			  and (:status is null or c.status = :status)
			""")
	Page<Customer> search(
			@Param("name") String name,
			@Param("cpf") String cpf,
			@Param("status") String status,
			Pageable pageable);
}
