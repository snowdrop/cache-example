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

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * This will also be used a singleton throughout the application to hold the configuration of the caches TTL
 * See /api/ttl
 *
 * This object will be created by Spring as Singleton and we will use to pass around the TTL configuration of the app
 * This solution is definitely not ideal and should never be used in a production app, but it's the easiest way
 * to implement the requirement of a configurable TTL while staying true to the Spring philosophy
 */
@ConfigurationProperties(prefix = "ttl")
public class TtlProperties {

    public final TimeUnit timeUnit = TimeUnit.SECONDS;

    private long value = 5L;

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
