package com.chatapp.user.repository;

import com.chatapp.user.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    Optional<UserProfile> findByUserId(String userId);
    boolean existsByUserId(String userId);
    
    @Query("SELECT up FROM UserProfile up WHERE up.userId IN :userIds")
    List<UserProfile> findByUserIds(@Param("userIds") List<String> userIds);
    
    @Query("SELECT up FROM UserProfile up WHERE up.displayName LIKE %:name% OR up.status LIKE %:name%")
    List<UserProfile> searchByDisplayNameOrStatus(@Param("name") String name);
    
    @Query("SELECT up FROM UserProfile up WHERE up.userId IN (SELECT c FROM UserProfile u JOIN u.contacts c WHERE u.userId = :userId)")
    List<UserProfile> findContactsByUserId(@Param("userId") String userId);
}
