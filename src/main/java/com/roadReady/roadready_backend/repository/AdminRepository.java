package com.roadReady.roadready_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.roadReady.roadready_backend.model.Admin;

import java.util.Optional;

@Repository
public interface AdminRepository
        extends JpaRepository<Admin, Long> {

    boolean existsByUsers_UserId(Long userId);

    Optional<Admin> findByUsers_UserId(Long userId);
}
