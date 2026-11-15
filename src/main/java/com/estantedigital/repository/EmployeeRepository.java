package com.estantedigital.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.estantedigital.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByRegistrationNumber(int registrationNumber);

    boolean existsByCpf(String cpf);

    boolean existsByRegistrationNumber(int registrationNumber);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByRegistrationNumberAndIdNot(int registrationNumber, Long id);

    @Query("""
            select e from Employee e
            where (:name is null or lower(e.name) like lower(concat('%', :name, '%')))
              and (:cpf is null or e.cpf like concat('%', :cpf, '%'))
              and (:status is null or e.status = :status)
              and (:registrationNumber is null or e.registrationNumber = :registrationNumber)
            """)
    Page<Employee> search(
            @Param("name") String name,
            @Param("cpf") String cpf,
            @Param("status") String status,
            @Param("registrationNumber") Integer registrationNumber,
            Pageable pageable);
}
