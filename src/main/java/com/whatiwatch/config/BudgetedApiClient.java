package com.whatiwatch.config;

import java.util.concurrent.atomic.AtomicInteger;

/*
- Wraps any API call with a hard request budget
- Tracks how many requests have been made and stops when the limit is reached
- Uses AtomicInteger for thread safety
*/
public class BudgetedApiClient {
    
    private final String providerName;
    private final int maxRequests;
    private final AtomicInteger requestCount = new AtomicInteger(0);

    public BudgetedApiClient(String providerName, int maxRequests) {
        this.providerName = providerName;
        this.maxRequests = maxRequests;
    }

    // Checks if request can be made within a budget, throws ApiLimitException if not
    public void checkBudget() {
        int count = requestCount.incrementAndGet();
        if (count > maxRequests) {
            throw new ApiLimitException(providerName);
        }
    }

    // returns how many requests have been made so far
    public int requestsMade() {
        return requestCount.get();
    }

    // returns how many requests are remaining in the budget
    public int requestsRemaining() {
        return Math.max(0, maxRequests - requestCount.get());
    }

    // returns true if less than 10% remaining, for warning
    public boolean isNearLimit() {
        return requestsRemaining() <= Math.max(1, maxRequests / 10);
    }

    // resets the request counter, called when user's quota resets
    public void reset() {
        requestCount.set(0);
    }

}
