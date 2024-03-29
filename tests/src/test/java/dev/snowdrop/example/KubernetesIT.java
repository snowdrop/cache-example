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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.AfterAll;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.annotation.KubernetesIntegrationTest;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.LocalPortForward;

@KubernetesIntegrationTest(deployEnabled = false, buildEnabled = false)
public class KubernetesIT extends AbstractTest {

    @Inject
    KubernetesClient client;

    LocalPortForward appPort;

    @Override
    public URL getAppUrl() throws MalformedURLException {
        tearDown();
        appPort = client.services().inNamespace(System.getProperty("kubernetes.namespace"))
                .withName("spring-boot-cache-greeting").portForward(8080);
        return new URL("http://localhost:" + appPort.getLocalPort() + "/");
    }

    @AfterAll
    public void tearDown() {
        if (appPort != null) {
            try {
                appPort.close();
            } catch (IOException ignored) {

            }
        }
    }


}
