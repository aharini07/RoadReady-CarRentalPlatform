package com.roadReady.roadready_backend.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admin")
@Getter
@Setter
@NoArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long adminId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users users;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone_no", length = 15)
    private String phoneNo;

    @Column(name = "department", length = 50)
    private String department;
}
