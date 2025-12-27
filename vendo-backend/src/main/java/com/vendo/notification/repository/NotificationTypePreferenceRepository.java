package com.vendo.notification.repository;

import com.vendo.notification.entity.NotificationTypePreference;
import com.vendo.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTypePreferenceRepository extends JpaRepository<NotificationTypePreference, Long> {
    
    Optional<NotificationTypePreference> findByUserIdAndNotificationType(Long userId, NotificationType notificationType);
    
    List<NotificationTypePreference> findByUserId(Long userId);
    
    void deleteByUserId(Long userId);
}

