package com.nexus.user.repository;

import com.nexus.user.entity.Activation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActivationRepository extends JpaRepository<Activation, Long> {
    Optional<Activation> findByCodeAndCompletedFalse(String code);
    
    @Query("SELECT a FROM Activation a WHERE a.user.id = ?1 AND a.completed = false")
    Optional<Activation> findByUserIdAndCompletedFalse(Long userId);
    
    @Query("SELECT COUNT(a) > 0 FROM Activation a WHERE a.user.id = ?1 AND a.completed = true")
    boolean existsByUserIdAndCompletedTrue(Long userId);
}
