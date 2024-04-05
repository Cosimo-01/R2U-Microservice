package com.microservice.ms.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.microservice.ms.model.enums.UserRoles;
import com.microservice.ms.model.enums.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 20)
    @Column(unique = true)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Column(unique = true)
    private String email;

    private String firstName;

    private String lastName;

    @JsonIgnore
    @NotBlank
    @Size(max = 120)
    private String password;

    private String birthday;

    @Enumerated(EnumType.STRING)
    private UserRoles role;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @NotNull
    @CreatedDate
    private String createdAt;

    @NotNull
    private String lastUpdatedAt;

    private String deletedAt;
    private String lockedAt;

    public User(String username, String email, String password, String firstName, String lastName, String birthday) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        role = UserRoles.USER;
        status = UserStatus.NORMAL;
        createdAt = LocalDateTime.now().toString();
        lastUpdatedAt = LocalDateTime.now().toString();
    }

}
