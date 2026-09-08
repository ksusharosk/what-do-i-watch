package com.whatiwatch.domain.user;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationHistoryRepository 
        extends JpaRepository<RecommendationHistoryEntity, String> {
    
    // A user's history, most recent first
    List<RecommendationHistoryEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    // A user's most recent N entries
    List<RecommendationHistoryEntity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
