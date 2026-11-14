package com.roadReady.roadready_backend.mapper;

import org.springframework.stereotype.Component;

import com.roadReady.roadready_backend.dto.AdminRequestDto;
import com.roadReady.roadready_backend.dto.AdminResponseDto;
import com.roadReady.roadready_backend.dto.AdminUpdateDto;
import com.roadReady.roadready_backend.model.Admin;
import com.roadReady.roadready_backend.model.Users;

@Component
public class AdminMapper {

    public Admin toEntity(AdminRequestDto dto, Users user) {
        Admin admin = new Admin();

        admin.setUsers(user);
        admin.setName(dto.name());
        admin.setPhoneNo(dto.phoneNo());
        admin.setDepartment(dto.department());

        return admin;
    }

    public void updateEntity(Admin admin, AdminUpdateDto dto) {
        admin.setName(dto.name());
        admin.setPhoneNo(dto.phoneNo());
        admin.setDepartment(dto.department());
    }

    public AdminResponseDto toResponseDto(Admin admin) {
        return new AdminResponseDto(
                admin.getAdminId(),
                admin.getUsers().getUserId(),
                admin.getUsers().getUsername(),
                admin.getUsers().getRole().name(),
                admin.getName(),
                admin.getPhoneNo(),
                admin.getDepartment()
        );
    }
}
