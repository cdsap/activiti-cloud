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

import org.activiti.cloud.services.common.file.FileContent;

/**
 * Business logic related with actions to be done when the Model Content changes
 */
public interface ContentUpdateListener {
    /**
     * Perform an action over the model from the file content.
     *
     * @param model       the model to act with
     * @param fileContent the file content that has been updated
     */
    void execute(Model model, FileContent fileContent);

    /**
     * Get handled model type by this listener.
     *
     * @return handled model type
     */
    ModelType getHandledModelType();
}
