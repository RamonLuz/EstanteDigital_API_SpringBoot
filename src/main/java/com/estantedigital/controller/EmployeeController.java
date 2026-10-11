package com.estantedigital.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.estantedigital.dto.EmployeeRequestDTO;
import com.estantedigital.dto.EmployeeResponseDTO;
import com.estantedigital.entity.Employee;
import com.estantedigital.mapper.EmployeeMapper;
import com.estantedigital.service.EmployeeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAll() {
        return ResponseEntity.ok(employeeService.findAll().stream()
                .map(EmployeeMapper::toResponse)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getById(@PathVariable Long id) {
        Optional<Employee> employee = employeeService.findById(id);

        if (employee.isPresent()) {
            return ResponseEntity.ok(EmployeeMapper.toResponse(employee.get()));
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> create(@Valid @RequestBody EmployeeRequestDTO dto) {
        Employee employee = EmployeeMapper.toEntity(dto);

        Employee savedEmployee = employeeService.save(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeMapper.toResponse(savedEmployee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequestDTO dto) {
        Employee employee = new Employee();
        employee.setStatus(dto.getStatus());

        Optional<Employee> updatedEmployee = employeeService.updateById(id, employee);

        if (updatedEmployee.isPresent()) {
            return ResponseEntity.ok(EmployeeMapper.toResponse(updatedEmployee.get()));
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean removed = employeeService.deleteById(id);

        if (removed) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
