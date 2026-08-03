package com.whatiwatch.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class WatchListEntryTest {

    @Test
    void newEntryStartsAsWantToWatch() {
        WatchListEntry entry = WatchListEntry.create("user1", 1, "Parasite");
        assertEquals(WatchListEntry.Status.WANT_TO_WATCH, entry.status());
        assertFalse(entry.isWatched());
    }

    @Test
    void markingAsWatchedUpdatesStatus() {
        WatchListEntry entry = WatchListEntry.create("user1", 1, "Parasite");
        WatchListEntry watched = entry.markAsWatched();
        assertEquals(WatchListEntry.Status.WATCHED, watched.status());
        assertTrue(watched.isWatched());
    }

    @Test
    void markingAsWatchedSetsTimestamp() {
        WatchListEntry entry = WatchListEntry.create("user1", 1, "Parasite");
        WatchListEntry watched = entry.markAsWatched();
        assertNotNull(watched.watchedAt());
    }

    @Test
    void originalEntryUnchangedAfterMarkingWatched() {
        WatchListEntry entry = WatchListEntry.create("user1", 1, "Parasite");
        entry.markAsWatched();
        assertFalse(entry.isWatched());
    }

    @Test
    void newEntryHasNoWatchedTimestamp() {
        WatchListEntry entry = WatchListEntry.create("user1", 1, "Parasite");
        assertNull(entry.watchedAt());
    }


}
