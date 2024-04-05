package com.microservice.ms.response;

import java.util.List;

import com.microservice.ms.model.Activities;
import com.microservice.ms.model.User;

import lombok.Data;

@Data
public class ProfileInfoResponse {

    private User user;
    private List<Activities> activities;

    public ProfileInfoResponse(User user, List<Activities> activities) {
        this.user = user;
        this.activities = activities;
    }

}
