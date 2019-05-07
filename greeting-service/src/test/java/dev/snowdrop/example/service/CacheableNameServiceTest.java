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

import dev.snowdrop.example.AbstractSpringCachingTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CacheableNameServiceTest extends AbstractSpringCachingTest {

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private NameService nameService;

    @Before
    public void setUp() {
        when(nameService.getName()).thenReturn("dummy");
    }

    @Test
    public void verifyCachingBehavior() {
        //invoke the first time
        nameService.getName();
        //assert no cache hit
        verifyCacheMiss();

        //reset the mock so we can test if there were any new invocations
        reset(restTemplate);

        //invoke the second time
        nameService.getName();
        //assert cache hit
        verify(restTemplate, never()).getForObject(anyString(), any(Class.class));

        sleepLongEnoughForCacheToExpire();

        //invoke the first time
        nameService.getName();
        //assert no cache hit
        verifyCacheMiss();
    }

    private void verifyCacheMiss() {
        verify(restTemplate, times(1)).getForObject(anyString(), any(Class.class));
    }

}
