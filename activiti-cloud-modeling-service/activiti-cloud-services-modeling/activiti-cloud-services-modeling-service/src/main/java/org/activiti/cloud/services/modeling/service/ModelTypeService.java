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

import static org.activiti.cloud.services.common.util.ContentTypeUtils.CONTENT_TYPE_XML;
import static org.activiti.cloud.services.common.util.ContentTypeUtils.JSON;
import static org.springframework.data.support.PageableExecutionUtils.getPage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.activiti.cloud.modeling.api.ModelType;
import org.activiti.cloud.modeling.api.ProcessModelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Handler for model types
 */
public class ModelTypeService {

    private Map<String, ModelType> modelTypesMapByName;

    private Map<String, ModelType> modelTypesMapByFolderName;

    @Autowired
    public ModelTypeService(Set<ModelType> availableModelTypes) {
        this.modelTypesMapByName =
            availableModelTypes.stream().collect(Collectors.toMap(ModelType::getName, Function.identity()));

        this.modelTypesMapByFolderName =
            availableModelTypes.stream().collect(Collectors.toMap(ModelType::getFolderName, Function.identity()));
    }

    public Optional<ModelType> findModelTypeByName(String name) {
        return Optional.ofNullable(modelTypesMapByName.get(name));
    }

    public Optional<ModelType> findModelTypeByFolderName(String folderName) {
        return Optional.ofNullable(modelTypesMapByFolderName.get(folderName));
    }

    public Collection<ModelType> getAvailableModelTypes() {
        return modelTypesMapByName.values();
    }

    public Page<ModelType> getModelTypeNames(Pageable pageable) {
        List<ModelType> availableModelTypeNames = new ArrayList<>(getAvailableModelTypes());
        return getPage(availableModelTypeNames, pageable, availableModelTypeNames::size);
    }

    public boolean isJson(ModelType modelType) {
        return JSON.equals(modelType.getContentFileExtension());
    }

    public boolean isContentXML(ModelType modelType) {
        return CONTENT_TYPE_XML.equalsIgnoreCase(modelType.getContentFileExtension());
    }

    public boolean isProcessContent(ModelType modelType) {
        return this.isContentXML(modelType) && ProcessModelType.PROCESS.contains(modelType.getName());
    }
}
