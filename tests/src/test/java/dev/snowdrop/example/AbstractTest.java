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

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTest {

    private static final String GREETING_PATH = "api/greeting";
    private static final String CACHED_PATH = "api/cached";

    abstract URL getAppUrl() throws MalformedURLException;

    private String appUrl;

    @BeforeAll
    public void setup() {
        // waits until the route is responding
        appUrl = Awaitility.await().atMost(30, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS)
                .until(this::getAppUrl, url -> 200 == getStatusCodeFromGreetingService(url))
                .toString();
    }

    @BeforeEach
    public void clearCache() {
        given()
                .baseUri(appUrl)
                .delete(CACHED_PATH)
                .then()
                .log().all()
                .statusCode(204);
    }

    @Test
    public void testSimpleInteraction() {
        final String messageFromFirstInvocation = getMessageFromGreetingService();
        final String messageFromSecondInvocation = getMessageFromGreetingService();

        //the two messages should be the same since the name should have been served from the cache
        assertThat(messageFromFirstInvocation).isEqualTo(messageFromSecondInvocation);

        clearCache();

        final String messageFromThirdInvocation = getMessageFromGreetingService();

        //the two messages should be different since the cache was cleared
        assertThat(messageFromThirdInvocation).isNotEqualToIgnoringCase(messageFromSecondInvocation);
    }

    @Test
    public void testWaitForCacheToExpire() {
        final String messageFromFirstInvocation = getMessageFromGreetingService();

        waitForCacheToExpire(6); //since we haven't changed anything, the default ttl is 5 seconds

        final String messageFromSecondInvocation = getMessageFromGreetingService();

        //the two messages should be different since the cache entry expired
        assertThat(messageFromSecondInvocation).isNotEqualToIgnoringCase(messageFromFirstInvocation);
    }

    @Test
    public void firstRequestShouldBeSlow() {
        final long time = measureTime(this::getMessageFromGreetingService);

        assertThat(time).as("Server responded too fast").isGreaterThanOrEqualTo(2000);
    }

    @Test
    public void secondRequestShouldBeFast() {
        getMessageFromGreetingService();

        final long time = measureTime(this::getMessageFromGreetingService);

        assertThat(time).as("Server didn't respond fast enough").isLessThanOrEqualTo(1000);
    }

    @Test
    public void shouldClearCache() {
        getMessageFromGreetingService();
        assertCached(true);

        clearCache();
        assertCached(false);
    }

    private int getStatusCodeFromGreetingService(URL appUrl) {
        return given()
                .baseUri(appUrl.toString())
                .get(GREETING_PATH)
                .then()
                .extract().statusCode();
    }

    private String getMessageFromGreetingService() {
        final ExtractableResponse<Response> response =
                given()
                  .baseUri(appUrl)
                .get(GREETING_PATH)
                .then()
                        .statusCode(200)
                        .extract();

        final String message = response.body().jsonPath().get("message");
        assertThat(message).isNotEmpty();

        return message;
    }

    private void assertCached(boolean cached) {
        given()
           .baseUri(appUrl)
           .get(CACHED_PATH)
           .then()
                .log().all()
                .body("cached", is(cached));
    }

    private void waitForCacheToExpire(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ignored) {}
    }

    private long measureTime(Runnable action) {
        long start = System.currentTimeMillis();
        action.run();
        return System.currentTimeMillis() - start;
    }

}
