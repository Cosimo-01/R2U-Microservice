package com.microservice.ms.payload.requests.activity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateActivityRequest {

    private String endDate;

    private String newDescription;

}
