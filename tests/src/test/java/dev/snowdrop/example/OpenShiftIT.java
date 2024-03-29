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

import java.net.URL;

import io.dekorate.testing.annotation.Inject;
import io.dekorate.testing.annotation.Named;
import io.dekorate.testing.openshift.annotation.OpenshiftIntegrationTest;

@OpenshiftIntegrationTest(deployEnabled = false, buildEnabled = false)
public class OpenShiftIT extends AbstractTest {

    @Named("spring-boot-cache-greeting")
    @Inject
    URL appUrl;

    @Override
    public URL getAppUrl() {
        return appUrl;
    }
}
