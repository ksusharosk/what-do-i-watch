package com.whatiwatch.api;

// Request to set a user's AI API key
public record ApiKeyRequest(String backend, String apiKey) {}
