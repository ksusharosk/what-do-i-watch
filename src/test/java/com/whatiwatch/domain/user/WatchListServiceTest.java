package com.whatiwatch.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.whatiwatch.domain.user.WatchListEntry;
import com.whatiwatch.domain.user.WatchListEntryEntity;
import com.whatiwatch.domain.user.WatchListEntryRepository;

class WatchListServiceTest {

    private WatchListEntryRepository repo;
    private WatchListService service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(WatchListEntryRepository.class);
        service = new WatchListService(repo);
    }

    @Test
    void addNewEntryDefaultsToWantToWatch() {
        when(repo.findByUserId("user1")).thenReturn(List.of());

        WatchListEntry entry = service.addOrUpdate("user1", 550, "Fight Club", null);

        assertEquals(WatchListEntry.Status.WANT_TO_WATCH, entry.status());
        assertNull(entry.watchedAt());   // not watched yet
        verify(repo).save(any(WatchListEntryEntity.class));
    }

    @Test
    void addWithWatchedStatusMarksWatched() {
        when(repo.findByUserId("user1")).thenReturn(List.of());

        WatchListEntry entry = service.addOrUpdate("user1", 550, "Fight Club", "WATCHED");

        assertEquals(WatchListEntry.Status.WATCHED, entry.status());
        assertNotNull(entry.watchedAt());   // timestamp set when marked watched
        verify(repo).save(any(WatchListEntryEntity.class));
    }

    @Test
    void updatingExistingEntryToWatchedSetsTimestamp() {
        // Existing entry is WANT_TO_WATCH, not yet watched.
        WatchListEntry existing = WatchListEntry.create("user1", 550, "Fight Club");
        WatchListEntryEntity existingEntity = WatchListEntryEntity.fromDomain(existing);
        when(repo.findByUserId("user1")).thenReturn(List.of(existingEntity));

        WatchListEntry result = service.addOrUpdate("user1", 550, "Fight Club", "WATCHED");

        assertEquals(WatchListEntry.Status.WATCHED, result.status());
        assertNotNull(result.watchedAt());
        verify(repo).save(existingEntity);   // updated the existing one, not a new entry
    }

    @Test
    void invalidStatusIsRejected() {
        when(repo.findByUserId("user1")).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> service.addOrUpdate("user1", 550, "Fight Club", "WACHED"));  // typo

        verify(repo, never()).save(any());
    }

    @Test
    void getWatchListReturnsUsersEntries() {
        WatchListEntry e = WatchListEntry.create("user1", 550, "Fight Club");
        when(repo.findByUserId("user1")).thenReturn(List.of(WatchListEntryEntity.fromDomain(e)));

        List<WatchListEntry> list = service.getWatchList("user1");

        assertEquals(1, list.size());
        assertEquals(550, list.get(0).movieId());
    }

    @Test
    void removeDeletesExistingEntry() {
        WatchListEntry e = WatchListEntry.create("user1", 550, "Fight Club");
        WatchListEntryEntity entity = WatchListEntryEntity.fromDomain(e);
        when(repo.findByUserId("user1")).thenReturn(List.of(entity));

        service.remove("user1", 550);

        verify(repo).delete(entity);
    }
}