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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CacheableNameService implements NameService {

    private final RestTemplate restTemplate;
    private final NameCacheUtil nameCacheUtil;
    private final String nameServiceBaseURL;

    public CacheableNameService(RestTemplate restTemplate,
                                NameCacheUtil nameCacheUtil,
                                @Value("${service.name.baseURL:test}") String nameServiceBaseURL) {
        this.restTemplate = restTemplate;
        this.nameCacheUtil = nameCacheUtil;
        this.nameServiceBaseURL = nameServiceBaseURL;
    }

    @Override
    public String getName() {
        if (nameCacheUtil.get() != null) {
            return nameCacheUtil.get();
        }

        final String value = restTemplate.getForObject(nameServiceBaseURL + "/api/name", String.class);
        if (value != null) {
            nameCacheUtil.put(value);
        }
        return value;
    }
}
