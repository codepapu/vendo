package com.vendo.user.repository;

import com.vendo.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByUserId(Long userId);
    
    Optional<Address> findByUserIdAndId(Long userId, Long addressId);
    
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
    
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);
    
    boolean existsByUserIdAndId(Long userId, Long addressId);
    
    long countByUserId(Long userId);
}
