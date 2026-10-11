package com.estantedigital.dto;

public class EmployeeResponseDTO {

    private Long id;
    private String name;
    private String cpf;
    private int registrationNumber;
    private String position;
    private String status;

    public EmployeeResponseDTO() {
    }

    public EmployeeResponseDTO(Long id, String name, String cpf, int registrationNumber, String position, String status) {
        this.id = id;
        this.name = name;
        this.cpf = cpf;
        this.registrationNumber = registrationNumber;
        this.position = position;
        this.status = status;
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

    public int getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(int registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
