package com.microservice.ms.repository;

import com.microservice.ms.model.Activities;
import com.microservice.ms.model.id.ActivitiesID;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivitiesRepository extends JpaRepository<Activities, ActivitiesID> {

    List<Activities> findByUserId(Long userId);

    Activities findByUserIdAndActivityId(Long userId, Long activityId);

}
