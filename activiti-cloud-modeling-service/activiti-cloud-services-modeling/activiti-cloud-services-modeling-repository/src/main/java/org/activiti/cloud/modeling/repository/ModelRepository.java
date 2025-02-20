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
package org.activiti.cloud.modeling.repository;

import java.util.Optional;
import org.activiti.cloud.modeling.api.Model;
import org.activiti.cloud.modeling.api.ModelType;
import org.activiti.cloud.modeling.api.Project;
import org.activiti.cloud.services.common.file.FileContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface for {@link Model} entities repository
 */
public interface ModelRepository<P extends Project, M extends Model<P, ?>> {
    Page<M> getModels(P project, ModelType modelTypeFilter, Pageable pageable);

    Page<M> getModelsByName(P project, String name, Pageable pageable);

    Optional<M> findModelByNameInProject(P project, String modelName, String modelTypeFilter);

    Optional<M> findGlobalModelByNameAndType(String modelName, String modelTypeFilter);

    Optional<M> findModelById(String modelId);

    byte[] getModelContent(M model);

    byte[] getModelExport(M model);

    M createModel(M model);

    M updateModel(M modelToUpdate, M newModel);

    M copyModel(M model, P project);

    M updateModelContent(M modelToBeUpdate, FileContent fileContent);

    void deleteModel(M model);

    Class<M> getModelType();

    Page<M> getGlobalModels(ModelType modelTypeFilter, boolean includeOrphans, Pageable pageable);
}
