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

package io.openshift.booster;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.openshift.booster.CacheConstants.NAME_CACHE_ID;

@Configuration
public class CacheConfiguration {

    @Bean(NAME_CACHE_ID)
    public org.infinispan.configuration.cache.Configuration smallCache(
            @Value("${cache.expiration.millis:1000}") Integer expirationMillis) {
        return new ConfigurationBuilder()
                .memory()
                    .expiration()
                        .lifespan(expirationMillis)
                .build();
    }
}
