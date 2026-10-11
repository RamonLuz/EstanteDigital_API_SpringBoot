package com.estantedigital.dto;

import java.time.LocalDate;

public class SaleResponseDTO {

    private Long id;
    private CustomerResponseDTO customer;
    private EmployeeResponseDTO employee;
    private BookResponseDTO book;
    private LocalDate saleDate;

    public SaleResponseDTO() {
    }

    public SaleResponseDTO(Long id, CustomerResponseDTO customer, EmployeeResponseDTO employee, BookResponseDTO book,
            LocalDate saleDate) {
        this.id = id;
        this.customer = customer;
        this.employee = employee;
        this.book = book;
        this.saleDate = saleDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CustomerResponseDTO getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerResponseDTO customer) {
        this.customer = customer;
    }

    public EmployeeResponseDTO getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeResponseDTO employee) {
        this.employee = employee;
    }

    public BookResponseDTO getBook() {
        return book;
    }

    public void setBook(BookResponseDTO book) {
        this.book = book;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }
}
