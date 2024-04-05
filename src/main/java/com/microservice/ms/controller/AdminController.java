package com.microservice.ms.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservice.ms.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/admin")
@Secured({ "ADMIN" })
public class AdminController {
    static final Logger logger = LogManager.getLogger(AdminController.class);

    @Autowired
    private UserService UserService;

    @GetMapping("hello")
    public String getHello() {
        return new String("Hello Admin");
    }

}
