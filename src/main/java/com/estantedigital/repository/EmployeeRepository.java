package com.estantedigital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.estantedigital.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

}
