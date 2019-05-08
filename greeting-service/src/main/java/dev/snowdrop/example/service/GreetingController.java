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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    private final NameService nameService;
    private final NameCacheUtil nameCacheUtil;

    public GreetingController(NameService nameService, NameCacheUtil nameCacheUtil) {
        this.nameService = nameService;
        this.nameCacheUtil = nameCacheUtil;
    }

    @GetMapping("/api/greeting")
    public Greeting getGreeting() {
        return new Greeting(String.format("Hello, %s!", nameService.getName()));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/cached")
    public void deleteCache() {
        nameCacheUtil.clear();
    }

    @GetMapping("/api/cached")
    public CacheStatus isCached() {
        return new CacheStatus(!nameCacheUtil.isEmpty());
    }

    static class Greeting {
        private final String message;

        public Greeting(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    static class CacheStatus {
        private final boolean cached;

        public CacheStatus(boolean cached) {
            this.cached = cached;
        }

        public boolean isCached() {
            return cached;
        }
    }
}
