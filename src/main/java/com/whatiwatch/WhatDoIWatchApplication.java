package com.whatiwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the what-do-i-watch Spring Boot application
 * 
 * @SpringBootApplication bundles three annotations:
 *  - @Configuration - this class can define beans
 *  - @EnableAutoConfiguration - SpringBoot auto-configures based 
 *                               on the dependancies on the classpath
 *  - @ComponentScan - scans this package and sub-packages for SpringBoot component
 *                     (@Service, @RestController, etc.)
 * 
 */
@SpringBootApplication
public class WhatDoIWatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(WhatDoIWatchApplication.class, args);
    }

}
