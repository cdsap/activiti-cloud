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
package org.activiti.cloud.modeling.api;

import java.util.Map;
import java.util.Set;
import org.activiti.cloud.modeling.api.process.ModelScope;
import org.activiti.cloud.services.auditable.Auditable;

/**
 * Interface for models
 */
public interface Model<A extends Project, U> extends Auditable<U> {
    String getId();

    void setId(String id);

    String getName();

    void setName(String name);

    String getType();

    void setType(String type);

    Set<A> getProjects();

    void addProject(A project);

    void removeProject(A project);

    void clearProjects();

    String getVersion();

    String getContentType();

    void setContentType(String contentType);

    byte[] getContent();

    void setContent(byte[] content);

    Map<String, Object> getExtensions();

    void setExtensions(Map<String, Object> extensions);

    String getTemplate();

    void setTemplate(String template);

    ModelScope getScope();

    void setScope(ModelScope scope);

    String getCategory();

    void setCategory(String category);

    boolean hasProjects();

    boolean hasMultipleProjects();
}
