package com.roadReady.roadready_backend.service;

import com.roadReady.roadready_backend.dto.CustomerDto;
import com.roadReady.roadready_backend.enums.Role;
import com.roadReady.roadready_backend.exception.DuplicateUsernameException;
import com.roadReady.roadready_backend.mapper.CustomerMapper;
import com.roadReady.roadready_backend.model.Customer;
import com.roadReady.roadready_backend.model.Users;
import com.roadReady.roadready_backend.repository.CustomerRepository;
import com.roadReady.roadready_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @InjectMocks
    private CustomerService customerService;

    @Mock private CustomerRepository customers;
    @Mock private UserRepository users;
    @Mock private CustomerMapper mapper;
    @Mock private PasswordEncoder encoder;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
    }

    // Helper to mock a logged-in user
    private void mockSecurityContext(String username, String role) {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        lenient().when(auth.getName()).thenReturn(username);
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        lenient().when(auth.getAuthorities()).thenAnswer(invocation ->
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        SecurityContextHolder.setContext(securityContext);
    }



    @Test
    public void createCustomerTest_Success() {
        CustomerDto dto = new CustomerDto("testuser", "pass123", "John Doe", "9876543210", "Street 1", "Chennai", "India");

        Users mockSavedUser = new Users();
        mockSavedUser.setUserId(1L);
        mockSavedUser.setUsername("testuser");
        mockSavedUser.setRole(Role.CUSTOMER);

        Customer mockCustomer = new Customer();
        mockCustomer.setCustomerId(1L);
        mockCustomer.setUsers(mockSavedUser);
        mockCustomer.setName("John Doe");

        when(users.existsByUsername("testuser")).thenReturn(false);
        when(encoder.encode("pass123")).thenReturn("hashedpass");
        when(users.save(any(Users.class))).thenReturn(mockSavedUser);
        when(mapper.toEntity(dto, mockSavedUser)).thenReturn(mockCustomer);
        when(customers.save(mockCustomer)).thenReturn(mockCustomer);
        when(mapper.toDto(mockCustomer)).thenReturn(dto);

        CustomerDto result = customerService.createCustomer(dto);

        assertEquals("John Doe", result.name());
        verify(users, times(1)).save(any(Users.class));
        verify(customers, times(1)).save(mockCustomer);
    }

    @Test
    public void createCustomerTest_DuplicateUsername() {
        CustomerDto dto = new CustomerDto("testuser", "pass123", "John Doe", "9876543210", "Street 1", "Chennai", "India");

        when(users.existsByUsername("testuser")).thenReturn(true);

        assertThrows(DuplicateUsernameException.class, () -> {
            customerService.createCustomer(dto);
        });

        verify(users, never()).save(any(Users.class));
    }

    @Test
    public void getAllCustomersTest_NotAdmin() {
        mockSecurityContext("testuser", "CUSTOMER");

        assertThrows(AccessDeniedException.class, () -> {
            customerService.getAllCustomers();
        });

        verify(customers, never()).findAll();
    }

    @Test
    public void updateCurrentCustomerProfileTest_OwnerSuccess() {
        mockSecurityContext("testuser", "CUSTOMER");

        Users user = new Users();
        user.setUserId(1L);
        user.setUsername("testuser");

        Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setUsers(user);
        customer.setName("Old Name");

        CustomerDto updateDto = new CustomerDto("testuser", null, "New Name", "9876543210", "New Addr", "Chennai", "India");

        when(users.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(customers.findAll()).thenReturn(List.of(customer));
        when(customers.save(customer)).thenReturn(customer);

        Customer result = customerService.updateCurrentCustomerProfile(updateDto);

        assertEquals("New Name", result.getName());
        assertEquals("9876543210", result.getPhoneNo());
        assertEquals("New Addr", result.getAddress());

        verify(customers, times(1)).save(customer);
    }

    @Test
    public void getCustomerByIdTest_AccessDenied() {
        mockSecurityContext("hacker", "CUSTOMER");

        Users ownerUser = new Users();
        ownerUser.setUsername("testuser");

        Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setUsers(ownerUser);

        when(customers.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(AccessDeniedException.class, () -> {
            customerService.getCustomerById(1L);
        });

        verify(mapper, never()).toDto(any());
    }
}