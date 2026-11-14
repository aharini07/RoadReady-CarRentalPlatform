
package com.roadReady.roadready_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.roadReady.roadready_backend.model.Customer;

@Repository
public interface CustomerRepository
        extends JpaRepository<Customer, Long> {
}
