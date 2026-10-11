package com.estantedigital.service;

import java.util.List;
import java.util.Optional;

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

        if (employee.getCpf() == null || employee.getCpf().length() < 11) {
            throw new ApiException(400, "CPF must have 11 digits", "CPF");
        }

        return employeeRepository.save(employee);
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> updateById(Long id, Employee updatedEmployee) {
        Optional<Employee> existingEmployee = employeeRepository.findById(id);

        if (existingEmployee.isPresent()) {
            Employee employee = existingEmployee.get();
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
}
