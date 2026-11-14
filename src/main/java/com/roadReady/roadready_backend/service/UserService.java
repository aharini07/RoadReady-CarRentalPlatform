package com.roadReady.roadready_backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roadReady.roadready_backend.dto.UserRequestDto;
import com.roadReady.roadready_backend.dto.UserResponseDto;
import com.roadReady.roadready_backend.enums.Role;
import com.roadReady.roadready_backend.exception.DuplicateUsernameException;
import com.roadReady.roadready_backend.exception.UserNotFoundException;
import com.roadReady.roadready_backend.mapper.UserMapper;
import com.roadReady.roadready_backend.model.Admin;
import com.roadReady.roadready_backend.model.Customer;
import com.roadReady.roadready_backend.model.RentalAgent;
import com.roadReady.roadready_backend.model.Users;
import com.roadReady.roadready_backend.repository.AdminRepository;
import com.roadReady.roadready_backend.repository.CustomerRepository;
import com.roadReady.roadready_backend.repository.RentalAgentRepository;
import com.roadReady.roadready_backend.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;
    private final RentalAgentRepository rentalAgentRepository;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            CustomerRepository customerRepository,
            AdminRepository adminRepository,
            RentalAgentRepository rentalAgentRepository) {

        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.adminRepository = adminRepository;
        this.rentalAgentRepository = rentalAgentRepository;
    }

    @Transactional
    public UserResponseDto createUser(UserRequestDto dto) {

        if (userRepository.existsByUsername(dto.username())) {
            throw new DuplicateUsernameException("Username already exists: " + dto.username());
        }

        Users user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));

        // Force the role from the DTO
        if (dto.role() != null) {
            user.setRole(dto.role());
        } else {
            user.setRole(Role.CUSTOMER); // Default
        }

        Users savedUser = userRepository.save(user);

        // Create the respective profile based on the role using the DTO data
        if (savedUser.getRole() == Role.ADMIN) {
            Admin admin = new Admin();
            admin.setUsers(savedUser);
            admin.setName(dto.name() != null ? dto.name() : dto.username());
            admin.setPhoneNo(dto.phoneNo() != null ? dto.phoneNo() : "0000000000");
            admin.setDepartment("General");
            adminRepository.save(admin);

        } else if (savedUser.getRole() == Role.RENTAL_AGENT) {
            RentalAgent agent = new RentalAgent();
            agent.setUsers(savedUser);
            agent.setName(dto.name() != null ? dto.name() : dto.username());
            agent.setPhone(dto.phoneNo() != null ? dto.phoneNo() : "0000000000");
            agent.setShift("Morning");
            rentalAgentRepository.save(agent);

        } else {
            Customer customer = new Customer();
            customer.setUsers(savedUser);
            customer.setName(dto.name() != null ? dto.name() : dto.username());
            customer.setPhoneNo(dto.phoneNo() != null ? dto.phoneNo() : "0000000000");
            customer.setAddress(dto.address() != null ? dto.address() : "N/A");
            customer.setCity(dto.city() != null ? dto.city() : "N/A");
            customer.setCountry(dto.country() != null ? dto.country() : "N/A");
            customerRepository.save(customer);
        }

        return userMapper.toResponseDto(savedUser);
    }

    public UserResponseDto getUserById(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        return userMapper.toResponseDto(user);
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // 1. Delete the profile first based on role
        if (user.getRole() == Role.ADMIN) {
            adminRepository.findByUsers_UserId(userId).ifPresent(adminRepository::delete);
        } else if (user.getRole() == Role.RENTAL_AGENT) {
            rentalAgentRepository.findAll().stream()
                    .filter(a -> a.getUsers().getUserId().equals(userId))
                    .findFirst().ifPresent(rentalAgentRepository::delete);
        } else {
            customerRepository.findAll().stream()
                    .filter(c -> c.getUsers().getUserId().equals(userId))
                    .findFirst().ifPresent(customerRepository::delete);
        }

        // 2. Then delete the user
        userRepository.deleteById(userId);
    }
}