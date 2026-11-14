package com.roadReady.roadready_backend.service;

import com.roadReady.roadready_backend.dto.UserRequestDto;
import com.roadReady.roadready_backend.dto.UserResponseDto;
import com.roadReady.roadready_backend.enums.Role;
import com.roadReady.roadready_backend.exception.DuplicateUsernameException;
import com.roadReady.roadready_backend.mapper.UserMapper;
import com.roadReady.roadready_backend.model.Admin;
import com.roadReady.roadready_backend.model.Customer;
import com.roadReady.roadready_backend.model.RentalAgent;
import com.roadReady.roadready_backend.model.Users;
import com.roadReady.roadready_backend.repository.AdminRepository;
import com.roadReady.roadready_backend.repository.CustomerRepository;
import com.roadReady.roadready_backend.repository.RentalAgentRepository;
import com.roadReady.roadready_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CustomerRepository customerRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private RentalAgentRepository rentalAgentRepository;


    @Test
    public void createUserTest_DuplicateUsername() {
        UserRequestDto dto = new UserRequestDto(
                "duplicate_user", "pass123", Role.CUSTOMER,
                "Test Name", "9876543210", "Street", "City", "Country"
        );

        when(userRepository.existsByUsername("duplicate_user")).thenReturn(true);

        assertThrows(DuplicateUsernameException.class, () -> {
            userService.createUser(dto);
        });

        // Verify that nothing was saved
        verify(userRepository, never()).save(any());
        verify(customerRepository, never()).save(any());
        verify(adminRepository, never()).save(any());
        verify(rentalAgentRepository, never()).save(any());
    }


    @Test
    public void createUserTest_Admin_Success() {
        UserRequestDto dto = new UserRequestDto(
                "admin_new", "pass123", Role.ADMIN,
                "Super Admin", "9999999999", null, null, null
        );

        Users mockUser = new Users();
        mockUser.setUserId(1L);
        mockUser.setUsername("admin_new");
        mockUser.setRole(Role.ADMIN);

        when(userRepository.existsByUsername("admin_new")).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(mockUser);
        when(passwordEncoder.encode("pass123")).thenReturn("hashedpass");
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        when(userMapper.toResponseDto(mockUser)).thenReturn(
                new UserResponseDto(1L, "admin_new", Role.ADMIN)
        );

        UserResponseDto response = userService.createUser(dto);

        // Verify the admin profile was created
        ArgumentCaptor<Admin> adminCaptor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository, times(1)).save(adminCaptor.capture());
        Admin savedAdmin = adminCaptor.getValue();

        assertEquals("Super Admin", savedAdmin.getName());
        assertEquals("9999999999", savedAdmin.getPhoneNo());
        assertEquals("General", savedAdmin.getDepartment());
        assertEquals(mockUser, savedAdmin.getUsers());

        assertEquals(Role.ADMIN, response.role());
    }


    @Test
    public void createUserTest_RentalAgent_Success() {
        UserRequestDto dto = new UserRequestDto(
                "agent_new", "pass123", Role.RENTAL_AGENT,
                "Agent Smith", "8888888888", null, null, null
        );

        Users mockUser = new Users();
        mockUser.setUserId(2L);
        mockUser.setUsername("agent_new");
        mockUser.setRole(Role.RENTAL_AGENT);

        when(userRepository.existsByUsername("agent_new")).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(mockUser);
        when(passwordEncoder.encode("pass123")).thenReturn("hashedpass");
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        when(userMapper.toResponseDto(mockUser)).thenReturn(
                new UserResponseDto(2L, "agent_new", Role.RENTAL_AGENT)
        );

        userService.createUser(dto);

        ArgumentCaptor<RentalAgent> agentCaptor = ArgumentCaptor.forClass(RentalAgent.class);
        verify(rentalAgentRepository, times(1)).save(agentCaptor.capture());
        RentalAgent savedAgent = agentCaptor.getValue();

        assertEquals("Agent Smith", savedAgent.getName());
        assertEquals("8888888888", savedAgent.getPhone());
        assertEquals("Morning", savedAgent.getShift());
        assertEquals(mockUser, savedAgent.getUsers());
    }


    @Test
    public void createUserTest_Customer_Success() {
        UserRequestDto dto = new UserRequestDto(
                "cust_new", "pass123", Role.CUSTOMER,
                "John Doe", "7777777777", "Street 1", "Chennai", "India"
        );

        Users mockUser = new Users();
        mockUser.setUserId(3L);
        mockUser.setUsername("cust_new");
        mockUser.setRole(Role.CUSTOMER);

        when(userRepository.existsByUsername("cust_new")).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(mockUser);
        when(passwordEncoder.encode("pass123")).thenReturn("hashedpass");
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        when(userMapper.toResponseDto(mockUser)).thenReturn(
                new UserResponseDto(3L, "cust_new", Role.CUSTOMER)
        );

        userService.createUser(dto);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository, times(1)).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();

        assertEquals("John Doe", savedCustomer.getName());
        assertEquals("7777777777", savedCustomer.getPhoneNo());
        assertEquals("Street 1", savedCustomer.getAddress());
        assertEquals("Chennai", savedCustomer.getCity());
        assertEquals("India", savedCustomer.getCountry());
        assertEquals(mockUser, savedCustomer.getUsers());
    }


    @Test
    public void createUserTest_DefaultRole() {
        // Pass null role
        UserRequestDto dto = new UserRequestDto(
                "defaultuser", "pass123", null,
                "Default", "1234567890", "Addr", "City", "Country"
        );

        Users mockUser = new Users();
        mockUser.setUserId(4L);
        mockUser.setUsername("defaultuser");

        when(userRepository.existsByUsername("defaultuser")).thenReturn(false);
        when(userMapper.toEntity(dto)).thenReturn(mockUser);
        when(passwordEncoder.encode("pass123")).thenReturn("hashedpass");
        when(userRepository.save(mockUser)).thenReturn(mockUser);
        when(userMapper.toResponseDto(mockUser)).thenReturn(
                new UserResponseDto(4L, "defaultuser", Role.CUSTOMER)
        );

        userService.createUser(dto);

        // Verify the user was saved with CUSTOMER role
        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertEquals(Role.CUSTOMER, userCaptor.getValue().getRole());

        // Verify a Customer profile was created (not Admin or Agent)
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(adminRepository, never()).save(any(Admin.class));
        verify(rentalAgentRepository, never()).save(any(RentalAgent.class));
    }
}