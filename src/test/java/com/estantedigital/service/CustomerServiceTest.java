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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.estantedigital.entity.ApiException;
import com.estantedigital.entity.Customer;
import com.estantedigital.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final String CPF = "12345678901";

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldSaveCustomerWithValidCpf() {
        Customer customer = newCustomer("Ana", CPF, "ACTIVE");
        when(customerRepository.findByCpf(CPF)).thenReturn(Optional.empty());
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer result = customerService.save(customer);

        assertSame(customer, result);
        verify(customerRepository).save(customer);
    }

    @Test
    void shouldRejectCustomerWithCpfShorterThanElevenDigits() {
        Customer customer = newCustomer("Ana", "123", "ACTIVE");
        when(customerRepository.findByCpf("123")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> customerService.save(customer));

        assertEquals(400, exception.getStatus());
        assertEquals("CPF must have 11 digits", exception.getMessage());
        assertEquals("CPF", exception.getField());
        verify(customerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectCustomerWithoutCpf() {
        Customer customer = newCustomer("Ana", null, "ACTIVE");
        when(customerRepository.findByCpf(null)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> customerService.save(customer));

        assertEquals("CPF must have 11 digits", exception.getMessage());
        verify(customerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRejectDuplicateCpf() {
        Customer existingCustomer = newCustomer("Ana", CPF, "ACTIVE");
        Customer duplicateCustomer = newCustomer("Different name", CPF, "INACTIVE");
        when(customerRepository.findByCpf(CPF)).thenReturn(Optional.of(existingCustomer));

        ApiException exception = assertThrows(ApiException.class, () -> customerService.save(duplicateCustomer));

        assertEquals(400, exception.getStatus());
        assertEquals("CPF already registered", exception.getMessage());
        assertEquals("CPF", exception.getField());
        verify(customerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldListCustomers() {
        Customer customer = newCustomer("Ana", CPF, "ACTIVE");
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<Customer> result = customerService.listAll();

        assertEquals(1, result.size());
        assertSame(customer, result.get(0));
    }

    @Test
    void shouldFindCustomerById() {
        Customer customer = newCustomer("Ana", CPF, "ACTIVE");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.findById(1L);

        assertTrue(result.isPresent());
        assertSame(customer, result.get());
    }

    @Test
    void shouldFindCustomerByCpf() {
        Customer customer = newCustomer("Ana", CPF, "ACTIVE");
        when(customerRepository.findByCpf(CPF)).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.findByCpf(CPF);

        assertTrue(result.isPresent());
        assertSame(customer, result.get());
    }

    @Test
    void shouldUpdateExistingCustomerStatus() {
        Customer customer = newCustomer("Ana", CPF, "ACTIVE");
        Customer updatedCustomer = newCustomer("Ignored name", "ignored-cpf", "INACTIVE");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        Optional<Customer> result = customerService.update(1L, updatedCustomer);

        assertTrue(result.isPresent());
        assertSame(customer, result.get());
        assertEquals("INACTIVE", customer.getStatus());
        assertEquals("Ana", customer.getName());
        assertEquals(CPF, customer.getCpf());
        verify(customerRepository).save(customer);
    }

    @Test
    void shouldNotUpdateMissingCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.update(1L, newCustomer("Ana", CPF, "INACTIVE"));

        assertFalse(result.isPresent());
        verify(customerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldDeleteExistingCustomer() {
        when(customerRepository.existsById(1L)).thenReturn(true);

        assertTrue(customerService.delete(1L));

        verify(customerRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteMissingCustomer() {
        when(customerRepository.existsById(1L)).thenReturn(false);

        assertFalse(customerService.delete(1L));

        verify(customerRepository, never()).deleteById(1L);
    }

    private Customer newCustomer(String name, String cpf, String status) {
        return new Customer(name, cpf, status, null);
    }
}