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

import dev.snowdrop.example.service.GreetingController;
import dev.snowdrop.example.service.NameCacheUtil;
import dev.snowdrop.example.service.NameService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebMvcTest(GreetingController.class)
public class ExampleApplicationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private NameService nameService;

    @MockBean
    private NameCacheUtil nameCacheUtil;

    @Test
    public void getGreeting() throws Exception{
        final String name = "cute";
        given(nameService.getName()).willReturn(name);

        mvc.perform(get("/api/greeting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString(name)));
    }

    @Test
    public void deleteCache() throws Exception{
        mvc.perform(delete("/api/cached"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void isCached() throws Exception{
        given(nameCacheUtil.isEmpty()).willReturn(true);

        mvc.perform(get("/api/cached"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cached", is(false)));
    }
}
