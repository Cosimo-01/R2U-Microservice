package com.microservice.ms.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.microservice.ms.common.util.services.UserDetailsImpl;
import com.microservice.ms.payload.requests.activity.NewActivityRequest;
import com.microservice.ms.payload.requests.activity.UpdateActivityRequest;
import com.microservice.ms.service.ActivitiesService;
import com.microservice.ms.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/user")
@Secured({ "USER", "ADMIN" })
public class UserController {
    static final Logger logger = LogManager.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private ActivitiesService activitiesService;

    @GetMapping("/profile/v1/info")
    public ResponseEntity<?> getInfo() {
        return userService.profileInfo();
    }

    @PatchMapping(path = "profile/v1/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> editProfile(@Valid @RequestBody JsonPatch patch) {
        try {
            return userService.profileEdit(patch);
        } catch (JsonPatchException | JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("activity/v1/add")
    public ResponseEntity<?> addNewActivity(@Valid @RequestBody NewActivityRequest newActivityReq) {
        return activitiesService.newActivity(newActivityReq);
    }

    @PostMapping("activity/v1/update/{activityId}")
    public ResponseEntity<?> updateActivity(
            @Valid @RequestBody UpdateActivityRequest updateActivityReq,
            @PathVariable Long activityId) {
        return activitiesService.updateActivity(activityId, updateActivityReq);
    }

    @GetMapping("/profile/v1/deleteUser")
    public ResponseEntity<?> deleteUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        return userService.deleteUser(userDetails.getId());
    }
}
