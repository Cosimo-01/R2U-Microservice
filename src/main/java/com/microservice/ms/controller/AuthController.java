package com.microservice.ms.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.ms.common.util.jwt.TokenBlacklist;
import com.microservice.ms.payload.requests.auth.LoginRequest;
import com.microservice.ms.payload.requests.auth.RegisterRequest;
import com.microservice.ms.response.MessageResponse;
import com.microservice.ms.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/auth")
public class AuthController {
    static final Logger logger = LogManager.getLogger(AuthController.class);

    @Autowired
    UserService userService;

    @Autowired
    TokenBlacklist tokenBlacklist;

    @GetMapping("heartbeat")
    public ResponseEntity<?> heartbeat() {
        return ResponseEntity.status(200).body(new MessageResponse("Working..."));
    }

    @PostMapping("v1/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginReq) {
        return userService.loginUser(loginReq);
    }

    @PostMapping("v1/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerReq) {
        return userService.newUser(registerReq);
    }

    @GetMapping("v1/logout")
    public ResponseEntity<?> logout(HttpServletRequest req) {
        return userService.logoutUser(req);
    }

}
