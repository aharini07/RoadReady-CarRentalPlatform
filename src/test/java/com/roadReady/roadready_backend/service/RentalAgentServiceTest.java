package com.roadReady.roadready_backend.service;

import com.roadReady.roadready_backend.dto.RentalAgentDto;
import com.roadReady.roadready_backend.enums.Role;
import com.roadReady.roadready_backend.exception.DuplicateUsernameException;
import com.roadReady.roadready_backend.mapper.RentalAgentMapper;
import com.roadReady.roadready_backend.model.RentalAgent;
import com.roadReady.roadready_backend.model.Users;
import com.roadReady.roadready_backend.repository.RentalAgentRepository;
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
public class RentalAgentServiceTest {

    @InjectMocks
    private RentalAgentService rentalAgentService;

    @Mock private RentalAgentRepository agents;
    @Mock private UserRepository users;
    @Mock private RentalAgentMapper mapper;
    @Mock private PasswordEncoder encoder;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
    }

    // FIXED: Added lenient() to all stubs
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
    public void registerRentalAgentTest_Success() {
        RentalAgentDto dto = new RentalAgentDto("agent1", "pass123", "Agent Smith", "9876543210", "Morning");

        Users mockSavedUser = new Users();
        mockSavedUser.setUserId(1L);
        mockSavedUser.setUsername("agent1");
        mockSavedUser.setRole(Role.RENTAL_AGENT);

        RentalAgent mockAgent = new RentalAgent();
        mockAgent.setAgentId(1L);
        mockAgent.setUsers(mockSavedUser);
        mockAgent.setName("Agent Smith");

        when(users.existsByUsername("agent1")).thenReturn(false);
        when(encoder.encode("pass123")).thenReturn("hashedpass");
        when(users.save(any(Users.class))).thenReturn(mockSavedUser);
        when(mapper.toEntity(dto, mockSavedUser)).thenReturn(mockAgent);
        when(agents.save(mockAgent)).thenReturn(mockAgent);
        when(mapper.toDto(mockAgent)).thenReturn(dto);

        RentalAgentDto result = rentalAgentService.registerRentalAgent(dto);

        assertEquals("Agent Smith", result.name());
        verify(users, times(1)).save(any(Users.class));
        verify(agents, times(1)).save(mockAgent);
    }


    @Test
    public void registerRentalAgentTest_DuplicateUsername() {
        RentalAgentDto dto = new RentalAgentDto("agent1", "pass123", "Agent Smith", "9876543210", "Morning");

        when(users.existsByUsername("agent1")).thenReturn(true);

        assertThrows(DuplicateUsernameException.class, () -> {
            rentalAgentService.registerRentalAgent(dto);
        });

        verify(agents, never()).save(any());
    }


    @Test
    public void getAllRentalAgentsTest_NotAdmin() {
        mockSecurityContext("agent1", "RENTAL_AGENT");

        assertThrows(AccessDeniedException.class, () -> {
            rentalAgentService.getAllRentalAgents();
        });

        verify(agents, never()).findAll();
    }


    @Test
    public void getRentalAgentByIdTest_OwnerSuccess() {
        mockSecurityContext("agent1", "RENTAL_AGENT");

        Users user = new Users();
        user.setUsername("agent1");

        RentalAgent agent = new RentalAgent();
        agent.setAgentId(1L);
        agent.setUsers(user);

        RentalAgentDto mockDto = new RentalAgentDto("agent1", null, "Agent Smith", "9876543210", "Morning");

        when(agents.findById(1L)).thenReturn(Optional.of(agent));
        when(mapper.toDto(agent)).thenReturn(mockDto);

        RentalAgentDto result = rentalAgentService.getRentalAgentById(1L);

        assertEquals("Agent Smith", result.name());
    }


    @Test
    public void getRentalAgentByIdTest_AccessDenied() {
        mockSecurityContext("other_agent", "RENTAL_AGENT");

        Users owner = new Users();
        owner.setUsername("agent1");

        RentalAgent agent = new RentalAgent();
        agent.setAgentId(1L);
        agent.setUsers(owner);

        when(agents.findById(1L)).thenReturn(Optional.of(agent));

        assertThrows(AccessDeniedException.class, () -> {
            rentalAgentService.getRentalAgentById(1L);
        });

        verify(mapper, never()).toDto(any());
    }


    @Test
    public void updateRentalAgentTest_NotAdmin() {
        mockSecurityContext("agent1", "RENTAL_AGENT");

        assertThrows(AccessDeniedException.class, () -> {
            rentalAgentService.updateRentalAgent(1L, new RentalAgentDto(null, null, "New Name", null, null));
        });

        verify(agents, never()).save(any());
    }


    @Test
    public void deleteRentalAgentTest_NotAdmin() {
        mockSecurityContext("agent1", "RENTAL_AGENT");

        assertThrows(AccessDeniedException.class, () -> {
            rentalAgentService.deleteRentalAgent(1L);
        });

        verify(agents, never()).delete(any());
    }
}