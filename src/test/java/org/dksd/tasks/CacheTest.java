package org.dksd.tasks;

import org.dksd.tasks.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CacheTest {

    // A simple implementation of Identifier for testing purposes.
    static class TestEntry implements Identifier {
        private final UUID id;
        private final String name;

        TestEntry(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestEntry other = (TestEntry) obj;
            return id.equals(other.id) && name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return id.hashCode() + name.hashCode();
        }
    }

    private Cache<TestEntry> cache;
    private TestEntry entry1;
    private TestEntry entry2;

    @BeforeEach
    void setUp() {
        entry1 = new TestEntry(UUID.randomUUID(), "Entry One");
        entry2 = new TestEntry(UUID.randomUUID(), "Entry Two");

        // Create a Cache with entry1 and entry2 in the allEntries list.
        cache = new Cache<>(Arrays.asList(entry1, entry2));
    }

    @Test
    void testAddAndGet() {
        // Initially, add entry1 to the cache.
        cache.add(entry1);

        // Retrieve it from the cache using its ID.
        TestEntry retrieved = cache.get(entry1.getId());
        assertNotNull(retrieved, "Retrieved entry should not be null");
        assertEquals(entry1, retrieved, "The retrieved entry should match the added entry");
    }

    @Test
    void testLazyLoadingFromAllEntries() {
        // Do not add entry2 explicitly.
        // It should be found in the allEntries list via lazy loading.
        TestEntry retrieved = cache.get(entry2.getId());
        assertNotNull(retrieved, "Retrieved entry should not be null via lazy loading");
        assertEquals(entry2, retrieved, "The retrieved entry should match the entry in allEntries");
    }

    @Test
    void testGetNonExistingEntry() {
        // Generate a random UUID that is not in the cache nor in the allEntries list.
        UUID randomId = UUID.randomUUID();
        TestEntry retrieved = cache.get(randomId);
        assertNull(retrieved, "Retrieving a non-existing entry should return null");
    }
}
