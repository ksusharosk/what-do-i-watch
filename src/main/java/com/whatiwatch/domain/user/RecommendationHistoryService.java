package com.whatiwatch.domain.user;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.whatiwatch.domain.user.RecommendationHistoryEntity;
import com.whatiwatch.domain.user.RecommendationHistoryEntry;
import com.whatiwatch.domain.user.RecommendationHistoryRepository;

/** Records and retrieves users' recommendation history */
@Service
public class RecommendationHistoryService {

    private static final int RECENT_LIMIT = 20;

    private final RecommendationHistoryRepository historyRepository;

    public RecommendationHistoryService(RecommendationHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    /** Saves a recommendation session to the user's history */
    public void record(RecommendationHistoryEntry entry) {
        historyRepository.save(RecommendationHistoryEntity.fromDomain(entry));
    }

    /** The user's most recent history entries, newest first */
    public List<RecommendationHistoryEntry> getRecent(String userId) {
        return historyRepository
            .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, RECENT_LIMIT))
            .stream()
            .map(RecommendationHistoryEntity::toDomain)
            .toList();
    }

    /** Deleted all of user's history (when deleting a user) */
    public void deleteAllForUser(String userId) {
        List<RecommendationHistoryEntity> all = 
            historyRepository.findByUserIdOrderByCreatedAtDesc(userId);
        historyRepository.deleteAll();
    }
    
}
