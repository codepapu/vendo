package com.vendo.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_username", columnList = "username")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 191)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;
    
    @Column(length = 120)
    private String password;
    
    @Column(name = "remember_token", length = 100)
    private String rememberToken;
    
    @Column(name = "first_name", length = 120)
    private String firstName;
    
    @Column(name = "last_name", length = 120)
    private String lastName;
    
    @Column(unique = true, length = 60)
    private String username;
    
    @Column(name = "avatar_id")
    private Long avatarId;
    
    @Column(name = "super_user", nullable = false)
    @Builder.Default
    private Boolean superUser = false;
    
    @Column(name = "manage_supers", nullable = false)
    @Builder.Default
    private Boolean manageSupers = false;
    
    @Column(columnDefinition = "TEXT")
    private String permissions;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_users",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private java.util.Set<Role> roles = new java.util.HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<Activation> activations = new java.util.ArrayList<>();
    
    // Helper methods
    public boolean isActivated() {
        return activations.stream()
                .anyMatch(a -> a.getCompleted() != null && a.getCompleted());
    }
    
    public boolean hasRole(String roleSlug) {
        return roles.stream()
                .anyMatch(role -> role.getSlug().equals(roleSlug));
    }
    
    public boolean hasPermission(String permission) {
        if (superUser) {
            return true;
        }
        // Check permissions in roles
        return roles.stream()
                .anyMatch(role -> role.getPermissions() != null && 
                        role.getPermissions().contains(permission));
    }
}
