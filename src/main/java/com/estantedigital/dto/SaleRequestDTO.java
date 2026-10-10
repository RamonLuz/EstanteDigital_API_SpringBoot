package com.estantedigital.dto;

import java.time.LocalDate;

import com.estantedigital.entity.Book;
import com.estantedigital.entity.Customer;
import com.estantedigital.entity.Employee;

public class SaleRequestDTO {

    private Customer customer;

    private Employee employee;

    private Book book;

    private LocalDate saleDate;

    public SaleRequestDTO() {
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }
}
