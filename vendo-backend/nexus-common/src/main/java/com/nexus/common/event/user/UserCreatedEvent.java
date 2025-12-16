package com.nexus.common.event.user;

import com.nexus.common.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when a new user is created.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserCreatedEvent extends BaseEvent {
    
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    
    public UserCreatedEvent(Long userId, String email, String firstName, 
                           String lastName, String role) {
        super("user.created", "user-service", userId);
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
}
