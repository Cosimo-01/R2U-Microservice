package com.microservice.ms.controller;

import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.microservice.ms.response.MessageResponse;
import com.microservice.ms.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestMapping;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/admin")
@Secured({ "ADMIN" })
public class AdminController {
    static final Logger logger = LogManager.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    @GetMapping("v1/users")
    public ResponseEntity<?> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("v1/user/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        try {
            return userService.getUserById(userId);
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("No User found."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("v1/useractivity/{userId}")
    public ResponseEntity<?> getUserActivity(@PathVariable Long userId) {
        return userService.getUserActivity(userId);
    }

    @PatchMapping(path = "v1/edituser/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> editUserInfo(
            @Valid @RequestBody JsonPatch patch,
            @PathVariable Long userId) {
        try {
            return userService.profileEdit(userId, patch);
        } catch (JsonPatchException | JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("v1/deleteuser/{userId}")
    public ResponseEntity<?> deletUser(@PathVariable Long userId) {
        return userService.deleteUser(userId);
    }

}
