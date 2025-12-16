package com.nexus.common.event.user;

import com.nexus.common.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Event published when a user is updated.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserUpdatedEvent extends BaseEvent {
    
    private Long userId;
    private String email;
    private Map<String, Object> updatedFields;
    
    public UserUpdatedEvent(Long userId, String email, Map<String, Object> updatedFields) {
        super("user.updated", "user-service", userId);
        this.userId = userId;
        this.email = email;
        this.updatedFields = updatedFields;
    }
}
