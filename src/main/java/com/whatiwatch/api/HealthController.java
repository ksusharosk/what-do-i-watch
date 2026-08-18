package com.whatiwatch.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A minimal endpoint to confirm the web server is running.
 * Temporary 
 */
@RestController
public class HealthController {
    
    @GetMapping("/health")
    public String health() {
        return "what-do-i-watch is running";
    }

}
