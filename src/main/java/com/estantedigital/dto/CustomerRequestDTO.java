package com.estantedigital.dto;

import java.util.List;

import com.estantedigital.entity.Book;

import jakarta.validation.constraints.NotBlank;

public class CustomerRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "CPF is required")
    private String cpf;

    @NotBlank(message = "Status is required")
    private String status;

    private List<Book> purchases;

    public CustomerRequestDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Book> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<Book> purchases) {
        this.purchases = purchases;
    }
}
