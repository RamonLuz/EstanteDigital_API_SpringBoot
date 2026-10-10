package com.livrotech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.livrotech.entity.ApiException;
import com.livrotech.entity.Employee;
import com.livrotech.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    private static final String CPF = "12345678901";

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void shouldSaveEmployeeWithValidCpf() {
        Employee employee = newEmployee("Joao", CPF, 1001, "Seller", "ACTIVE");
        when(employeeRepository.save(employee)).thenReturn(employee);

        Employee result = employeeService.save(employee);

        assertSame(employee, result);
        verify(employeeRepository).save(employee);
    }

    @Test
    void shouldRejectEmployeeWithCpfShorterThanElevenDigits() {
        Employee employee = newEmployee("Joao", "123", 1001, "Seller", "ACTIVE");

        ApiException exception = assertThrows(ApiException.class, () -> employeeService.save(employee));

        assertEquals(400, exception.getStatus());
        assertEquals("CPF must have 11 digits", exception.getMessage());
        assertEquals("CPF", exception.getField());
        verify(employeeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectEmployeeWithoutCpf() {
        Employee employee = newEmployee("Joao", null, 1001, "Seller", "ACTIVE");

        ApiException exception = assertThrows(ApiException.class, () -> employeeService.save(employee));

        assertEquals("CPF must have 11 digits", exception.getMessage());
        verify(employeeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldListEmployees() {
        Employee employee = newEmployee("Joao", CPF, 1001, "Seller", "ACTIVE");
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<Employee> result = employeeService.listAll();

        assertEquals(1, result.size());
        assertSame(employee, result.get(0));
    }

    @Test
    void shouldFindEmployeeById() {
        Employee employee = newEmployee("Joao", CPF, 1001, "Seller", "ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Optional<Employee> result = employeeService.findById(1L);

        assertTrue(result.isPresent());
        assertSame(employee, result.get());
    }

    @Test
    void shouldUpdateExistingEmployeeStatus() {
        Employee employee = newEmployee("Joao", CPF, 1001, "Seller", "ACTIVE");
        Employee updatedEmployee = newEmployee("Ignored name", "ignored-cpf", 9999, "Ignored position", "INACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);

        Optional<Employee> result = employeeService.update(1L, updatedEmployee);

        assertTrue(result.isPresent());
        assertSame(employee, result.get());
        assertEquals("INACTIVE", employee.getStatus());
        assertEquals("Joao", employee.getName());
        assertEquals(CPF, employee.getCpf());
        assertEquals(1001, employee.getRegistrationNumber());
        assertEquals("Seller", employee.getPosition());
        verify(employeeRepository).save(employee);
    }

    @Test
    void shouldNotUpdateMissingEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Employee> result = employeeService.update(1L,
                newEmployee("Joao", CPF, 1001, "Seller", "INACTIVE"));

        assertFalse(result.isPresent());
        verify(employeeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldDeleteExistingEmployee() {
        when(employeeRepository.existsById(1L)).thenReturn(true);

        assertTrue(employeeService.delete(1L));

        verify(employeeRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteMissingEmployee() {
        when(employeeRepository.existsById(1L)).thenReturn(false);

        assertFalse(employeeService.delete(1L));

        verify(employeeRepository, never()).deleteById(1L);
    }

    private Employee newEmployee(String name, String cpf, int registrationNumber, String position, String status) {
        return new Employee(name, cpf, registrationNumber, position, status);
    }
}