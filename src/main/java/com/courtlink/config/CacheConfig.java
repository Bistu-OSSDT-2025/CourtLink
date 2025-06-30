package com.courtlink.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * 配置Caffeine缓存以提升系统性能
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置Caffeine缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 配置缓存实例
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 最大缓存条目数
                .maximumSize(1000)
                // 写入后过期时间
                .expireAfterWrite(5, TimeUnit.MINUTES)
                // 访问后过期时间
                .expireAfterAccess(2, TimeUnit.MINUTES)
                // 初始容量
                .initialCapacity(100)
                // 启用统计
                .recordStats()
        );
        
        // 设置缓存名称
        cacheManager.setCacheNames("courts", "court-search", "court-statistics");
        
        return cacheManager;
    }

    /**
     * 配置场地查询专用缓存
     */
    @Bean("courtQueryCache")
    public CacheManager courtQueryCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .refreshAfterWrite(8, TimeUnit.MINUTES)
                .recordStats()
        );
        
        return cacheManager;
    }

    /**
     * 配置统计数据缓存
     */
    @Bean("statisticsCache")
    public CacheManager statisticsCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
        );
        
        return cacheManager;
    }
} 