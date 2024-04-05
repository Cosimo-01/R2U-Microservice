package com.microservice.ms.model;

import java.time.LocalDateTime;

import com.microservice.ms.model.id.ActivitiesID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table
@Entity
@IdClass(ActivitiesID.class)
public class Activities {

    @Id
    private Long activityId;

    @Id
    private Long userId;

    @NotNull
    private String startDate;

    @NotNull
    private String endDate;

    @NotNull
    private String activityDescription;

    @NotNull
    private String lastUpdatedAt;

    public Activities(Long userId, String startDate, String activityDescription) {
        this.userId = userId;
        this.startDate = startDate;
        endDate = "In Progress";
        this.activityDescription = activityDescription;
        lastUpdatedAt = LocalDateTime.now().toString();
    }

}
