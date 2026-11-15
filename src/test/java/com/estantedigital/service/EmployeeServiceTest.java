package com.estantedigital.service;

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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.estantedigital.entity.Employee;
import com.estantedigital.exception.ApiException;
import com.estantedigital.repository.EmployeeRepository;

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
        when(employeeRepository.existsByCpf(CPF)).thenReturn(false);
        when(employeeRepository.existsByRegistrationNumber(1001)).thenReturn(false);
        when(employeeRepository.save(employee)).thenReturn(employee);

        Employee result = employeeService.save(employee);

        assertSame(employee, result);
        verify(employeeRepository).save(employee);
    }

    @Test
    void shouldRejectEmployeeWithDuplicateCpf() {
        Employee employee = newEmployee("Joao", CPF, 1001, "Seller", "ACTIVE");
        when(employeeRepository.existsByCpf(CPF)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> employeeService.save(employee));

        assertEquals(409, exception.getStatus());
        assertEquals("CPF already registered", exception.getMessage());
        assertEquals("CPF", exception.getField());
        verify(employeeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectEmployeeWithDuplicateRegistrationNumber() {
        Employee employee = newEmployee("Joao", CPF, 1001, "Seller", "ACTIVE");
        when(employeeRepository.existsByCpf(CPF)).thenReturn(false);
        when(employeeRepository.existsByRegistrationNumber(1001)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> employeeService.save(employee));

        assertEquals(409, exception.getStatus());
        assertEquals("Registration number already registered", exception.getMessage());
        assertEquals("registrationNumber", exception.getField());
        verify(employeeRepository, never()).save(org.mockito.ArgumentMatchers.any());
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
    void shouldRejectNullEmployee() {
        ApiException exception = assertThrows(ApiException.class, () -> employeeService.save(null));

        assertEquals(400, exception.getStatus());
        assertEquals("Employee is invalid", exception.getMessage());
        assertEquals("Body", exception.getField());
        verify(employeeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldListEmployees() {
        Employee employee = newEmployee("Joao", CPF, 1001, "Seller", "ACTIVE");
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<Employee> result = employeeService.findAll();

        assertEquals(1, result.size());
        assertSame(employee, result.get(0));
    }

    @Test
    void shouldSearchEmployeesByFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Employee employee = newEmployee("Joao Silva", CPF, 1001, "Seller", "ACTIVE");
        Page<Employee> expected = new PageImpl<>(List.of(employee), pageable, 1);
        when(employeeRepository.search("joao", CPF, "ACTIVE", 1001, pageable)).thenReturn(expected);

        Page<Employee> result = employeeService.search(" joao ", CPF, " ACTIVE ", 1001, pageable);

        assertEquals(expected, result);
        verify(employeeRepository).search("joao", CPF, "ACTIVE", 1001, pageable);
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
    void shouldReplaceExistingEmployeeFields() {
        Employee employee = newEmployee("Joao", CPF, 1001, "Seller", "ACTIVE");
        Employee updatedEmployee = newEmployee("Updated name", "10987654321", 2002, "Manager", "INACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByCpfAndIdNot("10987654321", 1L)).thenReturn(false);
        when(employeeRepository.existsByRegistrationNumberAndIdNot(2002, 1L)).thenReturn(false);
        when(employeeRepository.save(employee)).thenReturn(employee);

        Optional<Employee> result = employeeService.updateById(1L, updatedEmployee);

        assertTrue(result.isPresent());
        assertSame(employee, result.get());
        assertEquals("INACTIVE", employee.getStatus());
        assertEquals("Updated name", employee.getName());
        assertEquals("10987654321", employee.getCpf());
        assertEquals(2002, employee.getRegistrationNumber());
        assertEquals("Manager", employee.getPosition());
        verify(employeeRepository).save(employee);
    }

    @Test
    void shouldNotUpdateMissingEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Employee> result = employeeService.updateById(1L,
                newEmployee("Joao", CPF, 1001, "Seller", "INACTIVE"));

        assertFalse(result.isPresent());
        verify(employeeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectUpdatingEmployeeToAnotherEmployeesRegistrationNumber() {
        Employee employee = newEmployee("Joao", CPF, 1001, "Seller", "ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByCpfAndIdNot("10987654321", 1L)).thenReturn(false);
        when(employeeRepository.existsByRegistrationNumberAndIdNot(2002, 1L)).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> employeeService.updateById(
                1L, newEmployee("Updated name", "10987654321", 2002, "Manager", "ACTIVE")));

        assertEquals(409, exception.getStatus());
        assertEquals("Registration number already registered", exception.getMessage());
        verify(employeeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldDeleteExistingEmployee() {
        when(employeeRepository.existsById(1L)).thenReturn(true);

        assertTrue(employeeService.deleteById(1L));

        verify(employeeRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteMissingEmployee() {
        when(employeeRepository.existsById(1L)).thenReturn(false);

        assertFalse(employeeService.deleteById(1L));

        verify(employeeRepository, never()).deleteById(1L);
    }

    private Employee newEmployee(String name, String cpf, int registrationNumber, String position, String status) {
        return new Employee(name, cpf, registrationNumber, position, status);
    }
}