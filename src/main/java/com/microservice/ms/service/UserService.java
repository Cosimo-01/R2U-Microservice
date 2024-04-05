package com.microservice.ms.service;

import java.time.LocalDateTime;
import java.util.List;
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

import com.microservice.ms.common.util.jwt.JwtUtils;
import com.microservice.ms.common.util.services.UserDetailsImpl;
import com.microservice.ms.model.Activities;
import com.microservice.ms.model.User;
import com.microservice.ms.payload.requests.auth.LoginRequest;
import com.microservice.ms.payload.requests.auth.RegisterRequest;
import com.microservice.ms.repository.ActivitiesRepository;
import com.microservice.ms.repository.UserRepository;
import com.microservice.ms.response.JwtResponse;
import com.microservice.ms.response.MessageResponse;
import com.microservice.ms.response.ProfileInfoResponse;
import com.microservice.ms.response.UpdateResponse;

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

        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("No user associated with this email"));

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
    public ResponseEntity<?> logoutUser() {
        return ResponseEntity.ok(new JwtResponse(null, null));
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
            JsonNode patched = profileEditReq.apply(mapper.convertValue(toEditUser, JsonNode.class));
            logger.info("patched" + patched);
            User editedUser = mapper.treeToValue(patched, User.class);
            logger.info("edited" + editedUser);

            editedUser.setLastUpdatedAt(LocalDateTime.now().toString());
            logger.info("edited+lastupdated" + editedUser);

            userRepository.save(editedUser);

            return ResponseEntity.status(200).body(new UpdateResponse(toEditUser));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("No edited information sent."));
        }
    }

}
