package com.estantedigital.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.estantedigital.exception.DomainRuleViolationException;

@Entity
@Table(name = "employees", uniqueConstraints = {
        @UniqueConstraint(name = "uk_employees_cpf", columnNames = "cpf"),
        @UniqueConstraint(name = "uk_employees_registration_number", columnNames = "registration_number")
})
public class Employee extends Person {

    @Column(nullable = false)
    private int registrationNumber;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String status;

    public Employee(String name, String cpf, int registrationNumber, String position, String status) {
        super();
        updateDetails(name, cpf, registrationNumber, position, status);
    }

    protected Employee() {
    }

    public void updateDetails(String name, String cpf, int registrationNumber, String position, String status) {
        if (registrationNumber < 1) {
            throw new DomainRuleViolationException(
                    "registrationNumber", "Registration number must be greater than zero");
        }
        if (position == null || position.isBlank()) {
            throw new DomainRuleViolationException("position", "Position is required");
        }
        if (!"ACTIVE".equals(status) && !"INACTIVE".equals(status)) {
            throw new DomainRuleViolationException("status", "Status must be ACTIVE or INACTIVE");
        }

        changeIdentity(name, cpf);
        this.registrationNumber = registrationNumber;
        this.position = position.trim();
        this.status = status;
    }

    public int getRegistrationNumber() {
        return registrationNumber;
    }

    public String getPosition() {
        return position;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(registrationNumber);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Employee other = (Employee) obj;
        return registrationNumber == other.registrationNumber;
    }
}
