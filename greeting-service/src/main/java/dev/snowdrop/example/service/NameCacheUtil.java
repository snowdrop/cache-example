/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.snowdrop.example.service;

import org.infinispan.commons.api.BasicCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static dev.snowdrop.example.CacheConstants.NAME_CACHE_ID;

/**
 * Provides utility methods relating to the name cache
 */
@Component
public class NameCacheUtil {

    private static final String KEY = "key";

    private final int ttlMillis;
    private final BasicCache<String, String> infinispanCache;

    public NameCacheUtil(@Value("${cache.ttl}") int ttlMillis, CacheManager cacheManager) {
        this.ttlMillis = ttlMillis;
        final Cache springCache = cacheManager.getCache(NAME_CACHE_ID);
        infinispanCache = (BasicCache<String, String>) springCache.getNativeCache();
    }

    public void clear() {
        infinispanCache.clear();
    }

    public boolean isEmpty() {
        return infinispanCache.isEmpty();
    }

    public String get() {
        return infinispanCache.get(KEY);
    }

    public void put(String value) {
        infinispanCache.put(KEY, value, ttlMillis, TimeUnit.MILLISECONDS);
    }
}
