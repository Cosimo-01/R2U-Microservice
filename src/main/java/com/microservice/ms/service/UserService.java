package com.microservice.ms.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.microservice.ms.common.util.jwt.AuthTokenFilter;
import com.microservice.ms.common.util.jwt.JwtUtils;
import com.microservice.ms.common.util.jwt.TokenBlacklist;
import com.microservice.ms.common.util.services.UserDetailsImpl;
import com.microservice.ms.model.Activities;
import com.microservice.ms.model.User;
import com.microservice.ms.model.enums.UserStatus;
import com.microservice.ms.payload.requests.auth.LoginRequest;
import com.microservice.ms.payload.requests.auth.RegisterRequest;
import com.microservice.ms.repository.ActivitiesRepository;
import com.microservice.ms.repository.UserRepository;
import com.microservice.ms.response.JwtResponse;
import com.microservice.ms.response.MessageResponse;
import com.microservice.ms.response.ProfileInfoResponse;
import com.microservice.ms.response.UpdateResponse;

import jakarta.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserService {
    static final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    ActivitiesRepository activitiesRepository;

    @Autowired(required = true)
    PasswordEncoder encoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    TokenBlacklist tokenBlacklist;

    /**
     * Creates and Saves a new user
     * 
     * @param registerReq -> new user data
     * @return PromiseResponse
     * 
     * @author Cosimo
     */
    public ResponseEntity<?> newUser(RegisterRequest registerReq) {
        //checks the username is NOT already taken
        if (userRepository.existsByUsername(registerReq.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        //check the email is NOT already taken
        if (userRepository.existsByEmail(registerReq.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User(
                registerReq.getUsername(),
                registerReq.getEmail(),
                encoder.encode(registerReq.getPassword()),
                registerReq.getFirstName(),
                registerReq.getLastName(),
                registerReq.getBirthday());
        userRepository.save(user);

        return ResponseEntity.status(200).body(new MessageResponse("User registered successfully"));
    }

    /**
     * Authenticates the user by giving back a bearer Token
     * 
     * @param loginReq -> logging user data (username, password)
     * @returns a JWT Response with User + Token
     * 
     * @Author Cosimo
     */
    public ResponseEntity<?> loginUser(LoginRequest loginReq) {
        if (userRepository.existsByUsername(loginReq.getUsername())) {

            User user = userRepository.findByUsername(loginReq.getUsername()).get();
            if (user.getStatus() == UserStatus.DELETED) {
                return ResponseEntity.status(404).body(new MessageResponse("User Not Found"));

            } else if (user.getStatus() == UserStatus.TEMPORARY_LOCK) {
                return ResponseEntity.badRequest().body(
                        new MessageResponse("User is temporarily locked from access until: " + user.getLockedUntil()));

            } else if (user.getStatus() == UserStatus.PERMANENT_LOCK) {
                return ResponseEntity.badRequest().body(new MessageResponse("User is permanently locked from access!"));

            } else {
                try {
                    Authentication authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginReq.getUsername(),
                                    loginReq.getPassword()));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    String jwt = jwtUtils.generateJwtToken(authentication);
                    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                    return ResponseEntity.ok(new JwtResponse(jwt, userDetails));

                } catch (AuthenticationException e) {
                    return ResponseEntity.badRequest().body(new MessageResponse("Wrong Username or Password"));

                } catch (Exception e) {
                    return ResponseEntity.status(400).body(new MessageResponse(String.format("%s", e)));

                }

            }

        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("No user associated with this username"));

        }

    }

    /**
     * Used for logging out
     * 
     * @param none
     * @returns an empty JWT Response
     * 
     * @Author Cosimo
     */
    public ResponseEntity<?> logoutUser(HttpServletRequest req) {
        String token = AuthTokenFilter.parseJwt(req);
        tokenBlacklist.addToBlacklist(token);

        return ResponseEntity.ok("Logged Out Successfully");
    }

    /**
     * 
     * @return
     */
    public ResponseEntity<?> profileInfo() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();
        List<Activities> allActivities = activitiesRepository.findByUserId(userDetails.getId());

        return ResponseEntity.status(200).body(new ProfileInfoResponse(user, allActivities));
    }

    /**
     * 
     * @param profileEditReq
     * @return
     */
    public ResponseEntity<?> profileEdit(Long userId, JsonPatch profileEditReq)
            throws JsonPatchException, JsonProcessingException {

        User toEditUser;
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            toEditUser = user.get();
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found."));
        }

        if (profileEditReq != null) {
            ObjectMapper mapper = new ObjectMapper();

            User editData = mapper.convertValue(profileEditReq, User.class);
            if (editData.getId() != null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Id can't be changed!"));
            }
            if (editData.getUsername() != null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Username can't be changed!"));
            }

            JsonNode patched = profileEditReq.apply(mapper.convertValue(toEditUser, JsonNode.class));
            User editedUser = mapper.convertValue(patched, User.class);

            editedUser.setPassword(toEditUser.getPassword());
            editedUser.setLastUpdatedAt(LocalDateTime.now().toString());

            userRepository.save(editedUser);
            return ResponseEntity.status(200).body(new UpdateResponse(editedUser));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("No edited information sent."));
        }
    }

    /**
     * 
     * @param profileEditReq
     * @return
     */
    public ResponseEntity<?> profileEdit(JsonPatch profileEditReq) throws JsonPatchException, JsonProcessingException {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        User toEditUser;
        Optional<User> user = userRepository.findById(userDetails.getId());
        if (user.isPresent()) {
            toEditUser = user.get();
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found."));
        }

        if (profileEditReq != null) {
            ObjectMapper mapper = new ObjectMapper();

            User editData = mapper.convertValue(profileEditReq, User.class);
            if (editData.getId() != null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Id can't be changed!"));
            }
            if (editData.getUsername() != null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Username can't be changed!"));
            }
            if (editData.getRole() != null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("You don't have permission to change role!"));
            }
            if (editData.getStatus() != null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("You don't have permission to change the status!"));
            }

            JsonNode patched = profileEditReq.apply(mapper.convertValue(toEditUser, JsonNode.class));
            User editedUser = mapper.convertValue(patched, User.class);

            editedUser.setPassword(toEditUser.getPassword());
            editedUser.setLastUpdatedAt(LocalDateTime.now().toString());

            userRepository.save(editedUser);
            return ResponseEntity.status(200).body(new UpdateResponse(editedUser));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("No edited information sent."));
        }
    }

    /**
     * 
     * @return
     */
    public ResponseEntity<?> getAllUsers() {
        List<User> allUsers = userRepository.findAll();
        return ResponseEntity.status(200).body(allUsers);
    }

    /**
     * 
     * @param userId
     * @return
     * @throws NoSuchElementException
     */
    public ResponseEntity<?> getUserById(Long userId) throws NoSuchElementException {
        User user = userRepository.findById(userId).get();
        return ResponseEntity.status(200).body(user);
    }

    /**
     * 
     * @param userId
     * @return
     */
    public ResponseEntity<?> getUserActivity(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("No User found."));
        }

        List<Activities> userActivity = activitiesRepository.findByUserId(userId);

        if (userActivity.size() == 0) {
            return ResponseEntity.status(404).body(new MessageResponse("User has no activity registered yet."));
        } else {
            return ResponseEntity.status(200).body(userActivity);
        }
    }

    /**
     * 
     * @param userId
     * @return
     */
    public ResponseEntity<?> deleteUser(Long userId) {
        try {
            User user;
            Optional<User> toEditUser = userRepository.findById(userId);
            if (toEditUser.isPresent()) {
                user = toEditUser.get();
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("User not found."));
            }

            user.setStatus(UserStatus.DELETED);
            user.setDeletedAt(LocalDate.now());
            user.setLastUpdatedAt(LocalDateTime.now().toString());

            userRepository.save(user);

            return ResponseEntity.ok()
                    .body(new MessageResponse("User " + user.getUsername() + " successfully deleted."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("No user found with id: " + userId));
        }
    }

}
