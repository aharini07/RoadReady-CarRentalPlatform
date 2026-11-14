
package com.roadReady.roadready_backend.mapper;

import org.springframework.stereotype.Component;

import com.roadReady.roadready_backend.dto.RentalAgentDto;
import com.roadReady.roadready_backend.model.RentalAgent;
import com.roadReady.roadready_backend.model.Users;

@Component
public class RentalAgentMapper {

    // Convert DTO to entity
    public RentalAgent toEntity(RentalAgentDto dto, Users user) {

        RentalAgent agent = new RentalAgent();

        agent.setUsers(user);
        agent.setName(dto.name());
        agent.setPhone(dto.phone());
        agent.setShift(dto.shift());

        return agent;
    }

    // Convert entity to DTO
    public RentalAgentDto toDto(RentalAgent agent) {

        return new RentalAgentDto(
                agent.getUsers().getUsername(),
                null, // Never return the password or password hash
                agent.getName(),
                agent.getPhone(),
                agent.getShift()
        );
    }
}
