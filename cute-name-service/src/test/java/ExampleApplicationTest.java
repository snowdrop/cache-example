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

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import dev.snowdrop.example.ExampleApplication;

@SpringBootTest(
        classes = ExampleApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {"sleep.millis=0"} //we don't want the test to be slow because of sleeping
)
@AutoConfigureMockMvc
public class ExampleApplicationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void getName() throws Exception{
        mvc.perform(get("/api/name"))
                .andExpect(status().isOk())
                .andExpect(content().string(notNullValue()));
    }
}
