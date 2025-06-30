package com.courtlink.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * ����������
 * ����Caffeine����������ϵͳ����
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * ����Caffeine���������
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // ���û���ʵ��
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // ��󻺴���Ŀ��
                .maximumSize(1000)
                // д������ʱ��
                .expireAfterWrite(5, TimeUnit.MINUTES)
                // ���ʺ����ʱ��
                .expireAfterAccess(2, TimeUnit.MINUTES)
                // ��ʼ����
                .initialCapacity(100)
                // ����ͳ��
                .recordStats()
        );
        
        // ���û�������
        cacheManager.setCacheNames("courts", "court-search", "court-statistics");
        
        return cacheManager;
    }

    /**
     * ���ó��ز�ѯר�û���
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
     * ����ͳ�����ݻ���
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