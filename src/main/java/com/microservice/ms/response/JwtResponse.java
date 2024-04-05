package com.microservice.ms.response;

import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {

    private String token;
    private String tokenType = "Bearer";

    private UserDetails user;

    public JwtResponse(String token, UserDetails user) {
        this.token = token;
        this.user = user;
    }

}
