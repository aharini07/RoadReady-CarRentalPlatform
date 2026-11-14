package com.roadReady.roadready_backend.controller;

import java.util.List;

import com.roadReady.roadready_backend.model.Customer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.roadReady.roadready_backend.dto.CustomerDto;
import com.roadReady.roadready_backend.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // REGISTER A CUSTOMER
    @PostMapping("/add")
    public ResponseEntity<CustomerDto> createCustomer(
            @Valid @RequestBody CustomerDto dto) {

        CustomerDto response =
                customerService.createCustomer(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // GET CUSTOMER BY ID
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDto> getCustomerById(
            @PathVariable Long customerId) {

        CustomerDto response =
                customerService.getCustomerById(customerId);

        return ResponseEntity.ok(response);
    }

    // GET ALL CUSTOMERS - ADMIN ONLY
    @GetMapping("/all")
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {

        List<CustomerDto> customers =
                customerService.getAllCustomers();

        return ResponseEntity.ok(customers);
    }

    // UPDATE CUSTOMER PROFILE - ADMIN ONLY
    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerDto> updateCustomer(
            @PathVariable Long customerId,
            @RequestBody CustomerDto dto) {

        CustomerDto response =
                customerService.updateCustomer(customerId, dto);

        return ResponseEntity.ok(response);
    }

    // DELETE CUSTOMER PROFILE - ADMIN ONLY
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable Long customerId) {

        customerService.deleteCustomer(customerId);

        return ResponseEntity.noContent().build();
    }
    // GET MY PROFILE
    @GetMapping("/me")
    public ResponseEntity<Customer> getMyProfile() {
        return ResponseEntity.ok(customerService.getCurrentCustomerProfile());
    }

    // UPDATE MY PROFILE
    // UPDATE MY PROFILE
    @PutMapping("/me")
    public ResponseEntity<CustomerDto> updateMyProfile(@RequestBody CustomerDto dto) {
        Customer updated = customerService.updateCurrentCustomerProfile(dto);
        return ResponseEntity.ok(customerService.getCustomerById(updated.getCustomerId()));
    }
}
