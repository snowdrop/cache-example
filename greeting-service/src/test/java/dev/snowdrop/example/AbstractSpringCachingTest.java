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

package dev.snowdrop.example;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ExampleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public abstract class AbstractSpringCachingTest {

    //needs to be larger than the value specified in infinispan-conf.xml
    protected static final int MILLIS_LARGER_THAN_CACHE_EXPIRATION = 250;

    protected void sleepLongEnoughForCacheToExpire()  {
        try {
            Thread.sleep(MILLIS_LARGER_THAN_CACHE_EXPIRATION);
        } catch (InterruptedException ignored) {}
    }
}
