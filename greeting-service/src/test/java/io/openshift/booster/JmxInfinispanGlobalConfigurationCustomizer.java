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

import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.spring.starter.embedded.InfinispanGlobalConfigurationCustomizer;
import org.springframework.stereotype.Component;

/**
 * This is only needed in the tests where the Cache beans can be created multiple times
 * In such cases we need to allow multiple registration of JMX beans
 */
@Component
public class JmxInfinispanGlobalConfigurationCustomizer implements InfinispanGlobalConfigurationCustomizer {

    @Override
    public void cusomize(GlobalConfigurationBuilder builder) {
        builder.globalJmxStatistics().allowDuplicateDomains(Boolean.TRUE);
    }
}
