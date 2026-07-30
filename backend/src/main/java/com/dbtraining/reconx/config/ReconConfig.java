package com.dbtraining.reconx.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(objectName = "reconx:type=ReconConfig")
public class ReconConfig {

    private final CacheManager cacheManager;

    private volatile double priceTolerance = 0.01;
    private volatile boolean cachingEnabled = true;

    public ReconConfig(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @ManagedAttribute(description = "Price tolerance used during reconciliation")
    public double getPriceTolerance() {
        return priceTolerance;
    }

    @ManagedAttribute
    public void setPriceTolerance(double priceTolerance) {
        if (priceTolerance < 0 || priceTolerance > 1) {
            throw new IllegalArgumentException("Price tolerance must be between 0 and 1");
        }
        this.priceTolerance = priceTolerance;
    }

    @ManagedAttribute(description = "Enable or disable caching")
    public boolean isCachingEnabled() {
        return cachingEnabled;
    }

    @ManagedAttribute
    public void setCachingEnabled(boolean cachingEnabled) {
        this.cachingEnabled = cachingEnabled;
    }

    @ManagedOperation(description = "Clear all application caches")
    public void clearCache() {
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }
}