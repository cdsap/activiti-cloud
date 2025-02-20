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
package org.activiti.cloud.acc.modeling.modeling;

import java.util.Collection;
import java.util.Optional;
import net.serenitybdd.core.Serenity;
import org.activiti.cloud.modeling.api.Project;
import org.activiti.cloud.services.common.file.FileContent;
import org.springframework.hateoas.EntityModel;

/**
 * ModelingContext handler
 */
public class ModelingContextHandler {

    private static final String MODELING_CURRENT_CONTEXT = "modelingCurrentContext";

    private static final String MODELING_CURRENT_FILE = "modelingCurrentFile";

    private static final String MODELING_CURRENT_PROJECTS = "modelingCurrentProjects";

    public Optional<EntityModel<?>> getCurrentModelingContext() {
        return Optional.ofNullable(Serenity.sessionVariableCalled(MODELING_CURRENT_CONTEXT));
    }

    public void setCurrentModelingObject(EntityModel<?> currentModelingObject) {
        Serenity.setSessionVariable(MODELING_CURRENT_CONTEXT).to(currentModelingObject);
    }

    public Optional<FileContent> getCurrentModelingFile() {
        return Optional.ofNullable(Serenity.sessionVariableCalled(MODELING_CURRENT_FILE));
    }

    public void setCurrentModelingFile(FileContent fileContent) {
        Serenity.setSessionVariable(MODELING_CURRENT_FILE).to(fileContent);
    }

    public void setCurrentProjects(Collection<EntityModel<Project>> projects) {
        Serenity.setSessionVariable(MODELING_CURRENT_PROJECTS).to(projects);
    }

    public Optional<Collection<EntityModel<Project>>> getCurrentProjects() {
        return Optional.ofNullable(Serenity.sessionVariableCalled(MODELING_CURRENT_PROJECTS));
    }

    public static void resetCurrentModelingObject() {
        Serenity.setSessionVariable(MODELING_CURRENT_CONTEXT).to(null);
        Serenity.setSessionVariable(MODELING_CURRENT_FILE).to(null);
    }
}
