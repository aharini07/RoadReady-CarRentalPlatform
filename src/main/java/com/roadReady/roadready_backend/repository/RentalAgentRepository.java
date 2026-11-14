
package com.roadReady.roadready_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.roadReady.roadready_backend.model.RentalAgent;

public interface RentalAgentRepository
        extends JpaRepository<RentalAgent, Long> {
}
