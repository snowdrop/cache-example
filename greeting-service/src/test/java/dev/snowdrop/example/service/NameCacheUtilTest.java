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

import static dev.snowdrop.example.CacheConstants.NAME_CACHE_ID;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import dev.snowdrop.example.AbstractSpringCachingTest;

public class NameCacheUtilTest extends AbstractSpringCachingTest {

    private static final String TEST_KEY = "test";

    @Autowired
    private NameCacheUtil nameCacheUtil;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void setUp() {
        //clear the cache
        final Cache namesCache = getNamesCache();
        namesCache.clear();
        assertThat(namesCache.get(TEST_KEY)).isNull();
    }

    @Test
    public void verifyClearWorks() {
        //given
        final Cache namesCache = getNamesCache();
        namesCache.put(TEST_KEY, "value");
        assertThat(namesCache.get(TEST_KEY)).isNotNull();

        //when
        nameCacheUtil.clear();

        //then
        assertThat(namesCache.get(TEST_KEY)).isNull();
    }

    @Test
    public void verifyIsEmptyWorks() {
        //when
        nameCacheUtil.clear();

        //then
        assertThat(nameCacheUtil.isEmpty()).isTrue();

        //when
        getNamesCache().put(TEST_KEY, "value");

        //then
        assertThat(nameCacheUtil.isEmpty()).isFalse();
    }

    private Cache getNamesCache() {
        return cacheManager.getCache(NAME_CACHE_ID);
    }
}
