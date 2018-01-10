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

import com.jayway.restassured.response.ExtractableResponse;
import com.jayway.restassured.response.Response;
import org.arquillian.cube.openshift.impl.enricher.RouteURL;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.when;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class OpenShiftIT {

    @RouteURL("spring-boot-cache-greeting")
    private URL greetingBaseURI;

    private String greetingServiceURI;

    @Before
    public void setup() {
        greetingServiceURI = greetingBaseURI + "api/greeting";
        waitForApp(greetingBaseURI + "health");
    }

    @Test
    public void testGreetingEndpoint() {
        final String messageFromFirstInvocation = getMessageFromGreetingService();
        final String messageFromSecondInvocation = getMessageFromGreetingService();

        //the two messages should be the same since the name should have been served from the cache
        assertThat(messageFromFirstInvocation).isEqualTo(messageFromSecondInvocation);

        clearCache();

        final String messageFromThirdInvocation = getMessageFromGreetingService();

        //the two messages should be different since the cache was cleared
        assertThat(messageFromThirdInvocation).isNotEqualToIgnoringCase(messageFromSecondInvocation);

        sleepLongEnoughForCacheToExpire();

        final String messageFromFourthInvocation = getMessageFromGreetingService();

        //the two messages should be different since the cache entry expired
        assertThat(messageFromFourthInvocation).isNotEqualToIgnoringCase(messageFromThirdInvocation);
    }

    private void waitForApp(String uri) {
        await().pollInterval(1, TimeUnit.SECONDS).atMost(5, TimeUnit.MINUTES)
                .until(() -> {
                    try {
                        final Response response = get(uri);
                        return response.getStatusCode() == 200;
                    } catch (final Exception e) {
                        return false;
                    }
                });
    }

    private String getMessageFromGreetingService() {
        final ExtractableResponse<Response> response = when().get(greetingServiceURI)
                .then()
                .statusCode(200)
                .extract();

        final String message = response.body().jsonPath().get("message");
        assertThat(message).isNotEmpty();

        return message;
    }

    private void clearCache() {
        when().delete(greetingServiceURI)
                .then()
                .statusCode(204);
    }

    private void sleepLongEnoughForCacheToExpire() {
        try {
            Thread.sleep(11000);
        } catch (InterruptedException ignored) {}
    }

}
