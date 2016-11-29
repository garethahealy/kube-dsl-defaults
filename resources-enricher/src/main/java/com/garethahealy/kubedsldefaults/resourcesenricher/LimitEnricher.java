/*-
 * #%L
 * GarethHealy :: Kube DSL Defaults :: Resources Enricher
 * %%
 * Copyright (C) 2013 - 2016 Gareth Healy
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
package com.garethahealy.kubedsldefaults.resourcesenricher;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.maven.enricher.api.BaseEnricher;
import io.fabric8.maven.enricher.api.EnricherContext;

public class LimitEnricher extends BaseEnricher {

    public LimitEnricher(EnricherContext buildContext) {
        super(buildContext, "custom-limits");
    }

    @Override
    public void addMissingResources(KubernetesListBuilder builder) {
        log.info("addMissingResources");

        List<HasMetadata> items = builder.getItems();
        for (HasMetadata item : items) {
            if (item instanceof Deployment) {
                Deployment deployment = (Deployment)item;
                List<Container> containers = getCandidateContainers(deployment);
                for (Container container : containers) {
                    if (container.getResources() == null) {
                        ResourceRequirements resourceRequirements = buildResourceRequirements();

                        log.info(describe(resourceRequirements));

                        container.setResources(resourceRequirements);
                    }
                }
            }
        }

        builder.withItems(items);
    }

    private List<Container> getCandidateContainers(Deployment deployment) {
        if (deployment.getSpec() != null
            && deployment.getSpec().getTemplate() != null
            && deployment.getSpec().getTemplate().getSpec() != null
            && deployment.getSpec().getTemplate().getSpec().getContainers() != null
            && deployment.getSpec().getTemplate().getSpec().getContainers().size() > 0) {

            List<Container> containers = deployment.getSpec().getTemplate().getSpec().getContainers();
            if (containers != null && containers.size() > 0) {
                return Collections.unmodifiableList(containers);
            }
        }

        return Collections.emptyList();
    }

    private String describe(ResourceRequirements resourceRequirements) {
        StringBuilder desc = new StringBuilder("resource");
        if (resourceRequirements.getLimits() != null) {
            desc.append(" #limits");

            for (Map.Entry<String, Quantity> current : resourceRequirements.getLimits().entrySet()) {
                desc.append(" for ")
                    .append(current.getKey())
                    .append(" are ")
                    .append(current.getValue().getAmount())
                    .append(";");
            }
        }

        if (resourceRequirements.getRequests() != null) {
            desc.append(" #requests");

            for (Map.Entry<String, Quantity> current : resourceRequirements.getRequests().entrySet()) {
                desc.append(" for ")
                    .append(current.getKey())
                    .append(" are ")
                    .append(current.getValue().getAmount())
                    .append(";");
            }
        }

        return desc.toString();
    }

    private ResourceRequirements buildResourceRequirements() {
        return new ResourceRequirementsBuilder()
            .withLimits(getLimits())
            .withRequests(getRequests())
            .build();
    }

    private Map<String, Quantity> getRequests() {
        Map<String, Quantity> limits = new HashMap<String, Quantity>();
        limits.put("cpu", new Quantity("100m"));
        limits.put("memory", new Quantity("512Mi"));

        return limits;
    }

    private Map<String, Quantity> getLimits() {
        Map<String, Quantity> limits = new HashMap<String, Quantity>();
        limits.put("cpu", new Quantity("500m"));
        limits.put("memory", new Quantity("1024Mi"));

        return limits;
    }
}
