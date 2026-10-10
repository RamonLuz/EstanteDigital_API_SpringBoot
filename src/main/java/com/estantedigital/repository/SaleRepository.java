package com.estantedigital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.estantedigital.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long>{

}
