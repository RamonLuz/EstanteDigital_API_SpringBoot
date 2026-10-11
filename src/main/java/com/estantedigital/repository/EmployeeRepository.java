package com.estantedigital.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.estantedigital.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByRegistrationNumber(int registrationNumber);
}
