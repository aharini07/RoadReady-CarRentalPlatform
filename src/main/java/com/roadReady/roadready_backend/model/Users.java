package com.roadReady.roadready_backend.model;

import com.roadReady.roadready_backend.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
public class Users implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
    public Users(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

//    @PrePersist
//    public void init(){
//        isActive = true;
//    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = role.name();

        if (!authority.startsWith("ROLE_")) {
            authority = "ROLE_" + authority;
        }

        return List.of(new SimpleGrantedAuthority(authority));
    }

}