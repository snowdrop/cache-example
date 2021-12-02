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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.openshift.annotation.OpenshiftIntegrationTest;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

@OpenshiftIntegrationTest(deployEnabled = false, buildEnabled = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OpenShiftIT {

    private static final String GREETING_PATH = "api/greeting";
    private static final String CACHED_PATH = "api/cached";

    @Inject
    KubernetesClient kubernetesClient;

    private URL greetingBaseURI;

    @BeforeAll
    public void setup() throws MalformedURLException {
        // TODO: In Dekorate 1.7, we can inject Routes directly, so we won't need to do this:
        Route route = kubernetesClient.adapt(OpenShiftClient.class).routes().withName("spring-boot-cache-greeting").get();
        String protocol = route.getSpec().getTls() == null ? "http" : "https";
        int port = "http".equals(protocol) ? 80 : 443;
        greetingBaseURI = new URL(protocol, route.getSpec().getHost(), port, "/");

        // waits until the route is responding
        Awaitility.await().atMost(30, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals(200, getStatusCodeFromGreetingService()));
    }

    @BeforeEach
    public void clearCache() {
        given()
                .baseUri(greetingBaseURI.toString())
                .delete(CACHED_PATH)
                .then()
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

    private int getStatusCodeFromGreetingService() {
        return given()
                .baseUri(greetingBaseURI.toString())
                .get(GREETING_PATH)
                .then()
                .log().all()
                .extract().statusCode();
    }

    private String getMessageFromGreetingService() {
        final ExtractableResponse<Response> response =
                given()
                  .baseUri(greetingBaseURI.toString())
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
           .baseUri(greetingBaseURI.toString())
           .get(CACHED_PATH)
           .then()
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
