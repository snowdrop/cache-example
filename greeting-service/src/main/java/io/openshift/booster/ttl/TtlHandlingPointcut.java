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

package io.openshift.booster.ttl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.infinispan.commons.api.BasicCache;
import org.infinispan.spring.provider.SpringCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.openshift.booster.CacheConstants.NAME_CACHE_ID;

@Aspect
public class TtlHandlingPointcut {

    private static final Logger log = LoggerFactory.getLogger(TtlHandlingPointcut.class);

    private final TtlProperties ttlProperties;

    public TtlHandlingPointcut(TtlProperties ttlProperties) {
        this.ttlProperties = ttlProperties;
    }

    /**
     * Every time Spring tries to get a Cache from the Cache manager, we return an implementation
     * that enforces the configured TTL
     */
    @Around("execution(* org.springframework.cache.CacheManager.getCache(..)) && args(cacheName)")
    public Object cachePut(ProceedingJoinPoint point, String cacheName) throws Throwable {
        Object value = point.proceed();

        if (!cacheName.equals(NAME_CACHE_ID)) {
            return value;
        }

        final BasicCache originalNativeCache = ((SpringCache) value).getNativeCache();

        log.debug("Changing cache implementation of cache: {} to enforce lifespan", cacheName);
        
        return new SpringCache(new LifespanEnforcingBasicCache<Object, Object>(originalNativeCache, ttlProperties));
    }

    /**
     * Enforces the ttl specified by the TtlProperties to each cache entry
     */
    private static class LifespanEnforcingBasicCache<K, V> implements BasicCache<K,V>{

        private final BasicCache<K,V> delegate;
        private final TtlProperties ttlProperties;

        public LifespanEnforcingBasicCache(BasicCache<K, V> delegate, TtlProperties ttlProperties) {
            this.delegate = delegate;
            this.ttlProperties = ttlProperties;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public String getVersion() {
            return delegate.getVersion();
        }

        @Override
        public V put(K key, V value) {
            return delegate.put(key, value, ttlProperties.getValue(), ttlProperties.timeUnit);
        }

        @Override
        public V put(K key, V value, long lifespan, TimeUnit unit) {
            return delegate.put(key, value, ttlProperties.getValue(), ttlProperties.timeUnit);
        }

        @Override
        public V putIfAbsent(K key, V value, long lifespan, TimeUnit unit) {
            return delegate.putIfAbsent(key, value, ttlProperties.getValue(), ttlProperties.timeUnit);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> map, long lifespan, TimeUnit unit) {
            delegate.putAll(map, ttlProperties.getValue(), ttlProperties.timeUnit);
        }

        @Override
        public V replace(K key, V value, long lifespan, TimeUnit unit) {
            return delegate.replace(key, value, ttlProperties.getValue(), ttlProperties.timeUnit);
        }

        @Override
        public boolean replace(K key, V oldValue, V value, long lifespan, TimeUnit unit) {
            return delegate.replace(key, oldValue, value, ttlProperties.getValue(), ttlProperties.timeUnit);
        }

        @Override
        public V put(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
            return delegate.put(key, value, ttlProperties.getValue(), ttlProperties.timeUnit, maxIdleTime, maxIdleTimeUnit);
        }

        @Override
        public V putIfAbsent(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
            return delegate.putIfAbsent(key, value, ttlProperties.getValue(), ttlProperties.timeUnit, maxIdleTime, maxIdleTimeUnit);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> map, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
            delegate.putAll(map, ttlProperties.getValue(), ttlProperties.timeUnit, maxIdleTime, maxIdleTimeUnit);
        }

        @Override
        public V replace(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
            return delegate.replace(key, value, ttlProperties.getValue(), ttlProperties.timeUnit, maxIdleTime, maxIdleTimeUnit);
        }

        @Override
        public boolean replace(K key, V oldValue, V value, long lifespan, TimeUnit lifespanUnit, long maxIdleTime, TimeUnit maxIdleTimeUnit) {
            return delegate.replace(key, oldValue, value, ttlProperties.getValue(), ttlProperties.timeUnit, maxIdleTime, maxIdleTimeUnit);
        }

        @Override
        public V remove(Object key) {
            return delegate.remove(key);
        }

        @Override
        public CompletableFuture<V> putAsync(K key, V value) {
            return delegate.putAsync(key, value);
        }

        @Override
        public CompletableFuture<V> putAsync(K key, V value, long lifespan, TimeUnit unit) {
            return delegate.putAsync(key, value, ttlProperties.getValue(), ttlProperties.timeUnit);
        }

        @Override
        public CompletableFuture<V> putAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
            return delegate.putAsync(key, value, ttlProperties.getValue(), ttlProperties.timeUnit, maxIdle, maxIdleUnit);
        }

        @Override
        public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> data) {
            return delegate.putAllAsync(data);
        }

        @Override
        public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> data, long lifespan, TimeUnit unit) {
            return delegate.putAllAsync(data, ttlProperties.getValue(), ttlProperties.timeUnit);
        }

        @Override
        public CompletableFuture<Void> putAllAsync(Map<? extends K, ? extends V> data, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
            return delegate.putAllAsync(data, ttlProperties.getValue(), ttlProperties.timeUnit, maxIdle, maxIdleUnit);
        }

        @Override
        public CompletableFuture<Void> clearAsync() {
            return delegate.clearAsync();
        }

        @Override
        public CompletableFuture<V> putIfAbsentAsync(K key, V value) {
            return delegate.putIfAbsentAsync(key, value);
        }

        @Override
        public CompletableFuture<V> putIfAbsentAsync(K key, V value, long lifespan, TimeUnit unit) {
            return delegate.putIfAbsentAsync(key, value, ttlProperties.getValue(), ttlProperties.timeUnit);
        }

        @Override
        public CompletableFuture<V> putIfAbsentAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
            return delegate.putIfAbsentAsync(key, value, ttlProperties.getValue(), ttlProperties.timeUnit, maxIdle, maxIdleUnit);
        }

        @Override
        public CompletableFuture<V> removeAsync(Object key) {
            return delegate.removeAsync(key);
        }

        @Override
        public CompletableFuture<Boolean> removeAsync(Object key, Object value) {
            return delegate.removeAsync(key, value);
        }

        @Override
        public CompletableFuture<V> replaceAsync(K key, V value) {
            return delegate.replaceAsync(key, value);
        }

        @Override
        public CompletableFuture<V> replaceAsync(K key, V value, long lifespan, TimeUnit unit) {
            return delegate.replaceAsync(key, value, ttlProperties.getValue(), ttlProperties.timeUnit);
        }

        @Override
        public CompletableFuture<V> replaceAsync(K key, V value, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
            return delegate.replaceAsync(key, value, ttlProperties.getValue(), ttlProperties.timeUnit, maxIdle, maxIdleUnit);
        }

        @Override
        public CompletableFuture<Boolean> replaceAsync(K key, V oldValue, V newValue) {
            return delegate.replaceAsync(key, oldValue, newValue);
        }

        @Override
        public CompletableFuture<Boolean> replaceAsync(K key, V oldValue, V newValue, long lifespan, TimeUnit unit) {
            return delegate.replaceAsync(key, oldValue, newValue, ttlProperties.getValue(), ttlProperties.timeUnit);
        }

        @Override
        public CompletableFuture<Boolean> replaceAsync(K key, V oldValue, V newValue, long lifespan, TimeUnit lifespanUnit, long maxIdle, TimeUnit maxIdleUnit) {
            return delegate.replaceAsync(key, oldValue, newValue, ttlProperties.getValue(), ttlProperties.timeUnit, maxIdle, maxIdleUnit);
        }

        @Override
        public CompletableFuture<V> getAsync(K key) {
            return delegate.getAsync(key);
        }

        @Override
        public V getOrDefault(Object key, V defaultValue) {
            return delegate.getOrDefault(key, defaultValue);
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            delegate.forEach(action);
        }

        @Override
        public V putIfAbsent(K key, V value) {
            return delegate.putIfAbsent(key, value);
        }

        @Override
        public boolean remove(Object key, Object value) {
            return delegate.remove(key, value);
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            return delegate.replace(key, oldValue, newValue);
        }

        @Override
        public V replace(K key, V value) {
            return delegate.replace(key, value);
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            delegate.replaceAll(function);
        }

        @Override
        public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
            return delegate.computeIfAbsent(key, mappingFunction);
        }

        @Override
        public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            return delegate.computeIfPresent(key, remappingFunction);
        }

        @Override
        public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            return delegate.compute(key, remappingFunction);
        }

        @Override
        public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            return delegate.merge(key, value, remappingFunction);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return delegate.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return delegate.get(key);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            delegate.putAll(m);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public Set<K> keySet() {
            return delegate.keySet();
        }

        @Override
        public Collection<V> values() {
            return delegate.values();
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return delegate.entrySet();
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public void start() {
            delegate.start();
        }

        @Override
        public void stop() {
            delegate.stop();
        }
    }
}
