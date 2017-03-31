/*-
 * #%L
 * GarethHealy :: Kube DSL Defaults :: BuildConfig Generator
 * %%
 * Copyright (C) 2013 - 2017 Gareth Healy
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.garethahealy.kubedsldefaults.buildconfiggenerator;

import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.maven.enricher.api.BaseEnricher;
import io.fabric8.maven.enricher.api.EnricherContext;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildConfigBuilder;
import io.fabric8.openshift.api.model.BuildTriggerPolicy;
import io.fabric8.openshift.api.model.BuildTriggerPolicyBuilder;

public class BuildConfigEnricher extends BaseEnricher {

    public BuildConfigEnricher(EnricherContext buildContext) {
        super(buildContext, "custom-buildconfig");
    }

    @Override
    public void addMissingResources(KubernetesListBuilder builder) {
        log.info("addMissingResources");

        builder.addToBuildConfigItems(addBuildConfig());
    }

    private BuildConfig addBuildConfig() {
        // @formatter:off
        return new BuildConfigBuilder()
            .withNewMetadata()
                .withName("${APP}")
            .endMetadata()
            .withNewSpec()
                .withTriggers(getTriggers())
                .withNewSource()
                    .withNewGit()
                    .withUri("${GIT}")
                    .endGit()
                    .withType("Git")
                .endSource()
                .withNewStrategy()
                    .withNewSourceStrategy()
                        .withNewFrom()
                            .withKind("ImageStreamTag")
                            .withName("fis-java-openshift:latest")
                            .withNamespace("openshift")
                        .endFrom()
                        .withIncremental(true)
                    .endSourceStrategy()
                    .withType("Source")
                .endStrategy()
                .withNewOutput()
                    .withNewTo()
                        .withKind("ImageStreamTag")
                        .withName("${APP}:${IS_TAG}")
                    .endTo()
                .endOutput()
            .endSpec()
            .build();
        // @formatter:on
    }

    private BuildTriggerPolicy getTriggers() {
        // @formatter:off
        return new BuildTriggerPolicyBuilder()
            .withType("ImageChange")
            .withNewImageChange()
                .withNewFrom()
                    .withName("fis-java-openshift:latest")
                    .withKind("ImageStreamTag")
                    .withNamespace("openshift")
                .endFrom()
            .endImageChange()
            .build();
        // @formatter:on
    }
}
