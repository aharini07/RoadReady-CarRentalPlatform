
package com.roadReady.roadready_backend.service;

import java.util.List;

import com.roadReady.roadready_backend.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import com.roadReady.roadready_backend.dto.RentalAgentDto;
import com.roadReady.roadready_backend.exception.DuplicateUsernameException;
import com.roadReady.roadready_backend.exception.RentalAgentNotFoundException;
import com.roadReady.roadready_backend.mapper.RentalAgentMapper;
import com.roadReady.roadready_backend.model.RentalAgent;
import com.roadReady.roadready_backend.model.Users;
import com.roadReady.roadready_backend.repository.RentalAgentRepository;
import com.roadReady.roadready_backend.repository.UserRepository;

@Service
public class RentalAgentService {

    private final RentalAgentRepository agents;
    private final UserRepository users;
    private final RentalAgentMapper mapper;
    private final PasswordEncoder encoder;

    public RentalAgentService(
            RentalAgentRepository agents,
            UserRepository users,
            RentalAgentMapper mapper,
            PasswordEncoder encoder) {

        this.agents = agents;
        this.users = users;
        this.mapper = mapper;
        this.encoder = encoder;
    }

    public RentalAgentDto registerRentalAgent(RentalAgentDto dto) {

        if (users.existsByUsername(dto.username())) {
            throw new DuplicateUsernameException("Username already exists: " + dto.username());
        }

        Users user = new Users();
        user.setUsername(dto.username());
        user.setPassword(encoder.encode(dto.password()));

        // Role is assigned by the backend, never by the client
        user.setRole(Role.RENTAL_AGENT);

        Users savedUser = users.save(user);

        RentalAgent agent = mapper.toEntity(dto, savedUser);
        RentalAgent savedAgent = agents.save(agent);

        return mapper.toDto(savedAgent);
    }

    // FIND AGENT OR THROW EXCEPTION
    private RentalAgent requireAgent(Long id) {

        return agents.findById(id).orElseThrow(() ->
                        new RentalAgentNotFoundException("Rental agent not found with ID: " + id));
    }

    // CHECK PROFILE OWNERSHIP
    private void checkOwner(RentalAgent agent) {

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

        boolean owner = agent.getUsers() != null
                && agent.getUsers().getUsername() != null
                && agent.getUsers().getUsername()
                .equals(auth.getName());

        if (!admin && !owner) {
            throw new AccessDeniedException(
                    "You can access only your own rental-agent profile");
        }
    }

    // GET AGENT BY ID
    public RentalAgentDto getRentalAgentById(Long id) {

        RentalAgent agent = requireAgent(id);
        checkOwner(agent);

        return mapper.toDto(agent);
    }

    // GET ALL AGENTS - ADMIN ONLY
    public List<RentalAgentDto> getAllRentalAgents() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Only admins can view all rental agents");
        }

        return agents.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }
    // UPDATE AGENT PROFILE - ADMIN ONLY
    public RentalAgentDto updateRentalAgent(
            Long id, RentalAgentDto dto) {

        requireAdmin();
        RentalAgent agent = requireAgent(id);
        if (dto.name() != null) {
            agent.setName(dto.name());
        }

        if (dto.phone() != null) {
            agent.setPhone(dto.phone());
        }
        if (dto.shift() != null) {
            agent.setShift(dto.shift());
        }
        RentalAgent updatedAgent = agents.save(agent);
        return mapper.toDto(updatedAgent);
    }

    // DELETE AGENT PROFILE - ADMIN ONLY
// Does not delete the linked Users account
    public void deleteRentalAgent(Long id) {
        requireAdmin();
        RentalAgent agent = requireAgent(id);
        agents.delete(agent);
    }


    //CHECK ADMIN PERMISSION
    private void requireAdmin() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

            throw new AccessDeniedException("Only admins can update or delete rental-agent profiles");
        }
    }

}
