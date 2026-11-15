package com.estantedigital.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;

import com.estantedigital.exception.DomainRuleViolationException;

@MappedSuperclass
public abstract class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    protected String name;

    @NotBlank(message = "CPF is required")
    @Column(nullable = false)
    protected String cpf;

    public Person(String name, String cpf) {
        changeIdentity(name, cpf);
    }

    protected Person() {
    }

    protected void changeIdentity(String name, String cpf) {
        if (name == null || name.isBlank()) {
            throw new DomainRuleViolationException("name", "Name is required");
        }
        if (cpf == null || !cpf.matches("\\d{11}")) {
            throw new DomainRuleViolationException("cpf", "CPF must contain exactly 11 digits");
        }

        this.name = name.trim();
        this.cpf = cpf;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCpf() {
        return cpf;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cpf);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Person other = (Person) obj;
        return Objects.equals(cpf, other.cpf);
    }
}
