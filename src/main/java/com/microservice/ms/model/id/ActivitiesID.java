package com.microservice.ms.model.id;

import java.io.Serializable;

import lombok.Data;

@Data
public class ActivitiesID implements Serializable {

    private Long activityId;

    private Long userId;

}
