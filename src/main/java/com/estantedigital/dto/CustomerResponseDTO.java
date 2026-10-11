package com.estantedigital.dto;

import java.util.List;

public class CustomerResponseDTO {

    private Long id;
    private String name;
    private String cpf;
    private String status;
    private List<BookResponseDTO> purchases;

    public CustomerResponseDTO() {
    }

    public CustomerResponseDTO(Long id, String name, String cpf, String status, List<BookResponseDTO> purchases) {
        this.id = id;
        this.name = name;
        this.cpf = cpf;
        this.status = status;
        this.purchases = purchases;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<BookResponseDTO> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<BookResponseDTO> purchases) {
        this.purchases = purchases;
    }
}
