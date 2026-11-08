package com.estantedigital.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.estantedigital.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query(value = """
            select s from Sale s
            where (:customerCpf is null or s.customer.cpf like concat('%', :customerCpf, '%'))
              and (:bookTitle is null or lower(s.book.title) like lower(concat('%', :bookTitle, '%')))
              and (:employeeRegistrationNumber is null
                   or s.employee.registrationNumber = :employeeRegistrationNumber)
            """,
            countQuery = """
                    select count(s) from Sale s
                    where (:customerCpf is null or s.customer.cpf like concat('%', :customerCpf, '%'))
                      and (:bookTitle is null or lower(s.book.title) like lower(concat('%', :bookTitle, '%')))
                      and (:employeeRegistrationNumber is null
                           or s.employee.registrationNumber = :employeeRegistrationNumber)
                    """)
    Page<Sale> search(
            @Param("customerCpf") String customerCpf,
            @Param("bookTitle") String bookTitle,
            @Param("employeeRegistrationNumber") Integer employeeRegistrationNumber,
            Pageable pageable);

}
