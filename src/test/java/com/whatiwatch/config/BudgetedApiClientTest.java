package com.whatiwatch.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class BudgetedApiClientTest {

    @Test
    void freshClientHasFullBudget() {
        BudgetedApiClient client = new BudgetedApiClient("groq", 50);
        assertEquals(50, client.requestsRemaining());
        assertEquals(0, client.requestsMade());
    }

    @Test
    void requestCountIncreasesWithEachCheck() {
        BudgetedApiClient client = new BudgetedApiClient("groq", 50);
        client.checkBudget();
        client.checkBudget();
        assertEquals(2, client.requestsMade());
        assertEquals(48, client.requestsRemaining());
    }

    @Test
    void exceedingBudgetThrowsApiLimitException() {
        BudgetedApiClient client = new BudgetedApiClient("groq", 2);
        client.checkBudget();
        client.checkBudget();
        assertThrows(ApiLimitException.class, client::checkBudget);
    }

    @Test
    void resetRestoresFullBudget() {
        BudgetedApiClient client = new BudgetedApiClient("groq", 10);
        client.checkBudget();
        client.checkBudget();
        client.reset();
        assertEquals(0, client.requestsMade());
        assertEquals(10, client.requestsRemaining());
    }

    @Test
    void isNearLimitWhenLessThan10PercentRemaining() {
        BudgetedApiClient client = new BudgetedApiClient("groq", 10);
        for (int i = 0; i < 9; i++) {
            client.checkBudget();
        }
        assertTrue(client.isNearLimit());
    }

    @Test
    void isNotNearLimitWhenBudgetIsFresh() {
        BudgetedApiClient client = new BudgetedApiClient("groq", 50);
        assertFalse(client.isNearLimit());
    }
}