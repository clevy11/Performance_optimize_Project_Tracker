package com.example.clb.projecttracker.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Cache;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "project-tracker");
    }
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    // Register cache metrics with Micrometer
    @Bean
    public CacheManager registerCacheMetrics(CacheManager cacheManager, MeterRegistry registry) {
        // Get the first Caffeine cache to register metrics for
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("projects");
        if (caffeineCache != null) {
            @SuppressWarnings("unchecked")
            Cache<Object, Object> nativeCache = 
                    (Cache<Object, Object>) caffeineCache.getNativeCache();
            
            CaffeineCacheMetrics.monitor(
                    registry, 
                    nativeCache, 
                    "projects"
            );
        }
        return cacheManager;
    }
} 