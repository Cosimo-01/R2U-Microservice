package com.microservice.ms.response;

import lombok.Data;

@Data
public class UpdateResponse {

    private Object updatedObject;

    public UpdateResponse(Object updatedObject) {
        this.updatedObject = updatedObject;
    }

}
