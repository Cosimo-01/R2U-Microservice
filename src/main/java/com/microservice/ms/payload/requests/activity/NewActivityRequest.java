package com.microservice.ms.payload.requests.activity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewActivityRequest {

    @NotBlank
    private String startDate;

    @NotBlank
    private String activityDescription;

}
