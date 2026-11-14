package com.roadReady.roadready_backend.controller;

import com.roadReady.roadready_backend.dto.ProfileUpdateDto;
import com.roadReady.roadready_backend.model.Admin;
import com.roadReady.roadready_backend.model.Customer;
import com.roadReady.roadready_backend.model.RentalAgent;
import com.roadReady.roadready_backend.model.Users;
import com.roadReady.roadready_backend.repository.AdminRepository;
import com.roadReady.roadready_backend.repository.CustomerRepository;
import com.roadReady.roadready_backend.repository.RentalAgentRepository;
import com.roadReady.roadready_backend.repository.UserRepository;
import com.roadReady.roadready_backend.security.CustomUserDetailsService;
import com.roadReady.roadready_backend.security.JwtService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AdminRepository adminRepository;
    private final RentalAgentRepository rentalAgentRepository;
    private final CustomerRepository customerRepository;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            AdminRepository adminRepository,
            RentalAgentRepository rentalAgentRepository,
            CustomerRepository customerRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.adminRepository = adminRepository;
        this.rentalAgentRepository = rentalAgentRepository;
        this.customerRepository = customerRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        log.info("Login attempt for username: {}", request.username());

        Users user = userRepository.findByUsername(request.username()).orElse(null);
        if (user == null) {
            log.warn("Login failed: Username not found - {}", request.username());
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed: Incorrect password for username - {}", request.username());
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(userDetails, user.getRole().name());

        log.info("Login successful for username: {} with role: {}", user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @GetMapping("/user-details")
    public ResponseEntity<?> getUserDetails(@AuthenticationPrincipal UserDetails authenticatedUser) {
        if (authenticatedUser == null) {
            log.error("Failed to get user details: Unauthenticated request");
            return ResponseEntity.status(401).body("User is not authenticated");
        }
        Users user = userRepository.findByUsername(authenticatedUser.getUsername()).orElse(null);
        if (user == null) {
            log.error("User details not found for username: {}", authenticatedUser.getUsername());
            return ResponseEntity.status(404).body("User not found");
        }
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole().name()
        ));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getName())) {
            log.warn("Profile fetch failed: Unauthenticated");
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        Users user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Fetching profile for user: {} (Role: {})", user.getUsername(), user.getRole().name());

        if (user.getRole().name().equals("ADMIN")) {
            return ResponseEntity.ok(adminRepository.findAll().stream()
                    .filter(a -> a.getUsers().getUserId().equals(user.getUserId())).findFirst());
        } else if (user.getRole().name().equals("RENTAL_AGENT")) {
            return ResponseEntity.ok(rentalAgentRepository.findAll().stream()
                    .filter(a -> a.getUsers().getUserId().equals(user.getUserId())).findFirst());
        } else {
            return ResponseEntity.ok(customerRepository.findAll().stream()
                    .filter(c -> c.getUsers().getUserId().equals(user.getUserId())).findFirst());
        }
    }

    @PutMapping("/profile")
    @Transactional
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateDto dto) {
        log.info("=== PROFILE UPDATE RECEIVED ===");
        log.info("Name: {}, Phone: {}, Address: {}", dto.name(), dto.phoneNo(), dto.address());
        log.info("City: {}, Country: {}", dto.city(), dto.country());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getName())) {
            log.warn("Profile update failed: Unauthenticated");
            return ResponseEntity.status(401).body("Unauthenticated");
        }

        Users user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole().name().equals("ADMIN")) {
            Admin admin = adminRepository.findAll().stream()
                    .filter(a -> a.getUsers().getUserId().equals(user.getUserId())).findFirst()
                    .orElseThrow(() -> new RuntimeException("Admin profile not found"));
            if (dto.name() != null) admin.setName(dto.name());
            if (dto.phoneNo() != null) admin.setPhoneNo(dto.phoneNo());
            Admin saved = adminRepository.save(admin);
            log.info("Admin updated: {} / {}", saved.getName(), saved.getPhoneNo());
            return ResponseEntity.ok(saved);

        } else if (user.getRole().name().equals("RENTAL_AGENT")) {
            RentalAgent agent = rentalAgentRepository.findAll().stream()
                    .filter(a -> a.getUsers().getUserId().equals(user.getUserId())).findFirst()
                    .orElseThrow(() -> new RuntimeException("Agent profile not found"));
            if (dto.name() != null) agent.setName(dto.name());
            if (dto.phoneNo() != null) agent.setPhone(dto.phoneNo());
            RentalAgent saved = rentalAgentRepository.save(agent);
            log.info("Agent updated: {} / {}", saved.getName(), saved.getPhone());
            return ResponseEntity.ok(saved);

        } else {
            Customer customer = customerRepository.findAll().stream()
                    .filter(c -> c.getUsers().getUserId().equals(user.getUserId())).findFirst()
                    .orElseThrow(() -> new RuntimeException("Customer profile not found"));

            log.debug("Before update: {} / {}", customer.getName(), customer.getPhoneNo());

            if (dto.name() != null) customer.setName(dto.name());
            if (dto.phoneNo() != null) customer.setPhoneNo(dto.phoneNo());
            if (dto.address() != null) customer.setAddress(dto.address());
            if (dto.city() != null) customer.setCity(dto.city());
            if (dto.country() != null) customer.setCountry(dto.country());

            Customer saved = customerRepository.save(customer);
            log.info("Customer updated: {} / {}", saved.getName(), saved.getPhoneNo());
            return ResponseEntity.ok(saved);
        }
    }

    public record AuthRequest(String username, String password) {}
    public record AuthResponse(String token) {}
}