/*
 * Copyright 2017-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.cloud.services.modeling.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.activiti.cloud.modeling.api.ModelExtensionsValidator;
import org.activiti.cloud.modeling.api.ModelType;
import org.activiti.cloud.services.modeling.validation.extensions.ExtensionsModelValidator;

/**
 * Service for managing {@link ModelExtensionsService}
 */
public class ModelExtensionsService {

    private final Map<String, List<ModelExtensionsValidator>> modelExtensionsValidatorsMapByModelType;

    public ModelExtensionsService(
        Set<ModelExtensionsValidator> metadataValidators,
        ExtensionsModelValidator extensionsModelValidator,
        ModelTypeService modelTypeService
    ) {
        this.modelExtensionsValidatorsMapByModelType =
            metadataValidators
                .stream()
                .filter(validator -> validator.getHandledModelType() != null)
                .collect(Collectors.groupingBy(validator -> validator.getHandledModelType().getName()));

        // Add the generic JSON extensions schema to all the available model types except PROCESS
        modelTypeService
            .getAvailableModelTypes()
            .stream()
            .forEach(modelType ->
                this.modelExtensionsValidatorsMapByModelType.put(
                        modelType.getName(),
                        this.getExtensionValidators(modelType, extensionsModelValidator)
                    )
            );
    }

    private List<ModelExtensionsValidator> getExtensionValidators(
        ModelType modelType,
        ExtensionsModelValidator extensionsModelValidator
    ) {
        if (modelType.getName().equals(org.activiti.cloud.modeling.api.ProcessModelType.PROCESS)) {
            return this.modelExtensionsValidatorsMapByModelType.get(modelType.getName());
        }

        if (this.modelExtensionsValidatorsMapByModelType.containsKey(modelType.getName())) {
            return Stream
                .concat(
                    this.modelExtensionsValidatorsMapByModelType.get(modelType.getName()).stream(),
                    Stream.of(extensionsModelValidator)
                )
                .collect(Collectors.toList());
        }

        return Collections.singletonList(extensionsModelValidator);
    }

    public List<ModelExtensionsValidator> findExtensionsValidators(String modelType) {
        return modelExtensionsValidatorsMapByModelType.get(modelType);
    }
}
