package com.example.clb.projecttracker.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCacheNames(Arrays.asList(
                "projects", "projectsPage", "projectsWithNoTasksPage", "projectSummary", "projectSummariesPage",
                "developers", "developersPage",
                "tasks", "tasksPage", "tasksByProjectPages", "tasksByDeveloperPages", 
                "taskSummariesPage", "taskSummariesByProjectPages", "taskSummariesByDeveloperPages",
                "overdueTasksPage", "taskStatusCountsByProject", "taskStatusCountsOverall",
                "usersByRole", "pendingApprovalUsers", "adminDashboard"
        ));
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats();  // Enable statistics for monitoring
    }
} 