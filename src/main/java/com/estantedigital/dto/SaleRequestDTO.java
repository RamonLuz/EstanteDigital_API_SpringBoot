package com.estantedigital.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SaleRequestDTO {

    @NotBlank(message = "Customer CPF is required")
    private String customerCpf;

    @Min(value = 1, message = "Employee registration number must be greater than zero")
    private int employeeRegistrationNumber;

    @NotBlank(message = "Book title is required")
    private String bookTitle;

    @NotBlank(message = "Book author is required")
    private String bookAuthor;

    @NotNull(message = "Sale date is required")
    private LocalDate saleDate;

    public SaleRequestDTO() {
    }

    public String getCustomerCpf() {
        return customerCpf;
    }

    public void setCustomerCpf(String customerCpf) {
        this.customerCpf = customerCpf;
    }

    public int getEmployeeRegistrationNumber() {
        return employeeRegistrationNumber;
    }

    public void setEmployeeRegistrationNumber(int employeeRegistrationNumber) {
        this.employeeRegistrationNumber = employeeRegistrationNumber;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }
}
