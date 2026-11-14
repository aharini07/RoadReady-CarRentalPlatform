package com.roadReady.roadready_backend.mapper;

import org.springframework.stereotype.Component;

import com.roadReady.roadready_backend.dto.UserRequestDto;
import com.roadReady.roadready_backend.dto.UserResponseDto;
import com.roadReady.roadready_backend.model.Users;

@Component
public class UserMapper {

    // Convert request DTO into Users entity
    public Users toEntity(UserRequestDto dto) {

        Users user = new Users();

        user.setUsername(dto.username());
        user.setPassword(dto.password());
        user.setRole(dto.role());

        return user;
    }

    // Convert Users entity into response DTO
    public UserResponseDto toResponseDto(Users user) {

        return new UserResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getRole()
        );
    }
}
