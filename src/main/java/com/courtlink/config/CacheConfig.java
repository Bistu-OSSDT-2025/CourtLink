package com.courtlink.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 * 使用 Caffeine 提供高性能缓存
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置统一的缓存管理器
     * 使用 @Primary 注解标记为主要缓存管理器
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 配置默认的缓存设置
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 最大缓存大小
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
        
        // 设置缓存名称，不同缓存可以使用不同的配置
        cacheManager.setCacheNames(
            java.util.List.of(
                "courts",           // 球场数据缓存
                "court-search",     // 球场搜索缓存
                "court-statistics", // 球场统计缓存
                "court-query",      // 球场查询缓存 (较长过期时间)
                "statistics"        // 统计数据缓存 (最长过期时间)
            )
        );
        
        return cacheManager;
    }

    /**
     * 为需要特殊配置的缓存提供自定义Caffeine实例
     * 可以根据需要扩展更多特殊配置
     */
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .initialCapacity(100)
                .recordStats();
    }
} 