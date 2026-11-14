
package com.roadReady.roadready_backend.mapper;

import org.springframework.stereotype.Component;

import com.roadReady.roadready_backend.dto.CustomerDto;
import com.roadReady.roadready_backend.model.Customer;
import com.roadReady.roadready_backend.model.Users;

@Component
public class CustomerMapper {

    // Convert DTO to Customer entity
    public Customer toEntity(CustomerDto dto, Users user) {

        Customer customer = new Customer();

        customer.setUsers(user);
        customer.setName(dto.name());
        customer.setPhoneNo(dto.phoneNo());
        customer.setAddress(dto.address());
        customer.setCity(dto.city());
        customer.setCountry(dto.country());

        return customer;
    }

    // Convert Customer entity to DTO
    public CustomerDto toDto(Customer customer) {

        return new CustomerDto(
                customer.getUsers().getUsername(),
                null, // Never expose the password or password hash
                customer.getName(),
                customer.getPhoneNo(),
                customer.getAddress(),
                customer.getCity(),
                customer.getCountry()
        );
    }
}
