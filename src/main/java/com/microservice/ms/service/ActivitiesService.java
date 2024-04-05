package com.microservice.ms.service;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.microservice.ms.common.util.services.UserDetailsImpl;
import com.microservice.ms.model.Activities;
import com.microservice.ms.payload.requests.activity.NewActivityRequest;
import com.microservice.ms.payload.requests.activity.UpdateActivityRequest;
import com.microservice.ms.repository.ActivitiesRepository;
import com.microservice.ms.response.MessageResponse;
import com.microservice.ms.response.UpdateResponse;

@Service
public class ActivitiesService {
        static final Logger logger = LogManager.getLogger(ActivitiesService.class);

        @Autowired
        ActivitiesRepository activitiesRepository;

        /**
         * 
         * @param newActivityReq
         * @return
         */
        public ResponseEntity<?> newActivity(NewActivityRequest newActivityReq) {
                UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                                .getPrincipal();

                Activities activity = new Activities(
                                userDetails.getId(),
                                newActivityReq.getStartDate(),
                                newActivityReq.getActivityDescription());

                activitiesRepository.save(activity);

                return ResponseEntity.status(200)
                                .body(new MessageResponse(
                                                String.format(
                                                                "New activity for %s has been registered successfully",
                                                                userDetails.getUsername())));

        }

        /**
         * 
         * @param activityId
         * @param updateActivityReq
         * @return
         */
        public ResponseEntity<?> updateActivity(Long activityId, UpdateActivityRequest updateActivityReq) {
                UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                                .getPrincipal();
                Long userId = userDetails.getId();

                Activities toUpdateActivity = activitiesRepository.findByUserIdAndActivityId(userId, activityId);

                if (updateActivityReq != null) {
                        if (updateActivityReq.getEndDate() != null) {
                                toUpdateActivity.setEndDate(updateActivityReq.getEndDate());
                        }

                        if (updateActivityReq.getNewDescription() != null) {
                                toUpdateActivity.setActivityDescription(updateActivityReq.getNewDescription());
                        }

                        toUpdateActivity.setLastUpdatedAt(LocalDateTime.now().toString());
                        activitiesRepository.save(toUpdateActivity);
                        return ResponseEntity.status(200).body(new UpdateResponse(toUpdateActivity));
                } else {
                        return ResponseEntity.badRequest().body(new MessageResponse("No update information sent."));
                }

        }

}
