package com.estantedigital.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.estantedigital.entity.Employee;
import com.estantedigital.exception.ApiException;
import com.estantedigital.repository.EmployeeRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee save(Employee employee) {
        if (employee == null) {
            throw new ApiException(400, "Employee is invalid", "Body");
        }

        validate(employee);
        if (employeeRepository.existsByCpf(employee.getCpf())) {
            throw new ApiException(409, "CPF already registered", "CPF");
        }
        if (employeeRepository.existsByRegistrationNumber(employee.getRegistrationNumber())) {
            throw new ApiException(409, "Registration number already registered", "registrationNumber");
        }

        return employeeRepository.save(employee);
    }

    private void validate(Employee employee) {
        if (employee.getName() == null || employee.getName().isBlank()) {
            throw new ApiException(400, "Name is required", "name");
        }
        if (employee.getCpf() == null || !employee.getCpf().matches("\\d{11}")) {
            throw new ApiException(400, "CPF must have 11 digits", "CPF");
        }
        if (employee.getRegistrationNumber() < 1) {
            throw new ApiException(400, "Registration number must be greater than zero", "registrationNumber");
        }
        if (employee.getPosition() == null || employee.getPosition().isBlank()) {
            throw new ApiException(400, "Position is required", "position");
        }
        if (!"ACTIVE".equals(employee.getStatus()) && !"INACTIVE".equals(employee.getStatus())) {
            throw new ApiException(400, "Status must be ACTIVE or INACTIVE", "status");
        }
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Page<Employee> search(
            String name,
            String cpf,
            String status,
            Integer registrationNumber,
            Pageable pageable) {
        return employeeRepository.search(
                normalize(name),
                normalize(cpf),
                normalize(status),
                registrationNumber,
                pageable);
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> updateById(Long id, Employee updatedEmployee) {
        if (updatedEmployee == null) {
            throw new ApiException(400, "Employee is invalid", "Body");
        }
        validate(updatedEmployee);

        Optional<Employee> existingEmployee = employeeRepository.findById(id);

        if (existingEmployee.isPresent()) {
            Employee employee = existingEmployee.get();
            if (!employee.getCpf().equals(updatedEmployee.getCpf())
                    && employeeRepository.existsByCpfAndIdNot(updatedEmployee.getCpf(), id)) {
                throw new ApiException(409, "CPF already registered", "CPF");
            }
            if (employee.getRegistrationNumber() != updatedEmployee.getRegistrationNumber()
                    && employeeRepository.existsByRegistrationNumberAndIdNot(
                            updatedEmployee.getRegistrationNumber(), id)) {
                throw new ApiException(409, "Registration number already registered", "registrationNumber");
            }
            employee.setName(updatedEmployee.getName());
            employee.setCpf(updatedEmployee.getCpf());
            employee.setRegistrationNumber(updatedEmployee.getRegistrationNumber());
            employee.setPosition(updatedEmployee.getPosition());
            employee.setStatus(updatedEmployee.getStatus());
            employeeRepository.save(employee);
            return Optional.of(employee);
        }

        return Optional.empty();
    }

    public boolean deleteById(Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return true;
        }

        return false;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
