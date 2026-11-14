
package com.roadReady.roadready_backend.service;

import java.util.List;

import com.roadReady.roadready_backend.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import com.roadReady.roadready_backend.dto.CustomerDto;
import com.roadReady.roadready_backend.exception.CustomerNotFoundException;
import com.roadReady.roadready_backend.exception.DuplicateUsernameException;
import com.roadReady.roadready_backend.mapper.CustomerMapper;
import com.roadReady.roadready_backend.model.Customer;
import com.roadReady.roadready_backend.model.Users;
import com.roadReady.roadready_backend.repository.CustomerRepository;
import com.roadReady.roadready_backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customers;
    private final UserRepository users;
    private final CustomerMapper mapper;
    private final PasswordEncoder encoder;

    public CustomerService(
            CustomerRepository customers,
            UserRepository users,
            CustomerMapper mapper,
            PasswordEncoder encoder) {

        this.customers = customers;
        this.users = users;
        this.mapper = mapper;
        this.encoder = encoder;
    }

    public CustomerDto createCustomer(CustomerDto dto) {

        if (users.existsByUsername(dto.username())) {
            throw new DuplicateUsernameException(
                    "Username already exists: " + dto.username());
        }

        Users user = new Users();
        user.setUsername(dto.username());
        user.setPassword(encoder.encode(dto.password()));

        // Assign the role on the server
        user.setRole(Role.CUSTOMER);

        Users savedUser = users.save(user);

        Customer customer = mapper.toEntity(dto, savedUser);
        Customer savedCustomer = customers.save(customer);

        return mapper.toDto(savedCustomer);
    }

    private Customer requireCustomer(Long id) {

        return customers.findById(id)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer not found with ID: " + id));
    }

    private void checkOwner(Customer customer) {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getName())) {

            throw new AccessDeniedException(
                    "Authentication required");
        }

        boolean admin = auth.getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN"));

        boolean owner = customer.getUsers() != null
                && customer.getUsers().getUsername() != null
                && customer.getUsers().getUsername()
                .equals(auth.getName());

        if (!admin && !owner) {
            throw new AccessDeniedException(
                    "You can access only your own customer profile");
        }
    }

    public CustomerDto getCustomerById(Long id) {

        Customer customer = requireCustomer(id);
        checkOwner(customer);

        return mapper.toDto(customer);
    }


    public List<CustomerDto> getAllCustomers() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        boolean admin = auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getName())
                && auth.getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN"));

        if (!admin) {
            throw new AccessDeniedException(
                    "Only admins can view all customers");
        }

        return customers.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public CustomerDto updateCustomer(Long id, CustomerDto dto) {

        requireAdmin();

        Customer customer = requireCustomer(id);

        if (dto.name() != null) {
            customer.setName(dto.name());
        }

        if (dto.phoneNo() != null) {
            customer.setPhoneNo(dto.phoneNo());
        }

        if (dto.address() != null) {
            customer.setAddress(dto.address());
        }

        if (dto.city() != null) {
            customer.setCity(dto.city());
        }

        if (dto.country() != null) {
            customer.setCountry(dto.country());
        }

        return mapper.toDto(customers.save(customer));
    }


    public void deleteCustomer(Long id) {

        requireAdmin();

        Customer customer = requireCustomer(id);

        customers.delete(customer);
    }

    private void requireAdmin() {

        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        boolean admin = auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getName())
                && auth.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN"));

        if (!admin) {
            throw new AccessDeniedException(
                    "Only admins can update or delete customer profiles");
        }
    }

    public Customer getCurrentCustomerProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getName())) {
            throw new AccessDeniedException("Authentication required");
        }

        Users user = users.findByUsername(auth.getName())
                .orElseThrow(() -> new CustomerNotFoundException("User not found"));

        return customers.findAll().stream()
                .filter(c -> c.getUsers() != null && c.getUsers().getUserId().equals(user.getUserId()))
                .findFirst()
                .orElseThrow(() -> new CustomerNotFoundException("Customer profile not found"));
    }

    @Transactional
    public Customer updateCurrentCustomerProfile(CustomerDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getName())) {
            throw new AccessDeniedException("Authentication required");
        }

        Users user = users.findByUsername(auth.getName())
                .orElseThrow(() -> new CustomerNotFoundException("User not found"));

        Customer customer = customers.findAll().stream()
                .filter(c -> c.getUsers() != null && c.getUsers().getUserId().equals(user.getUserId()))
                .findFirst()
                .orElseThrow(() -> new CustomerNotFoundException("Customer profile not found"));

        if (dto.name() != null && !dto.name().isBlank()) customer.setName(dto.name());
        if (dto.phoneNo() != null && !dto.phoneNo().isBlank()) customer.setPhoneNo(dto.phoneNo());
        if (dto.address() != null && !dto.address().isBlank()) customer.setAddress(dto.address());
        if (dto.city() != null && !dto.city().isBlank()) customer.setCity(dto.city());
        if (dto.country() != null && !dto.country().isBlank()) customer.setCountry(dto.country());

        return customers.save(customer);
    }

}
