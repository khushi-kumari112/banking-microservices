package com.banking.user_service.repository;

import com.banking.user_service.entity.UserActivity;
import com.banking.user_service.enums.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    List<UserActivity> findByInternalUserId(String internalUserId);

    List<UserActivity> findByActivityType(ActivityType activityType);

    List<UserActivity> findByInternalUserIdAndTimestampBetween(String internalUserId, LocalDateTime start, LocalDateTime end);
}

