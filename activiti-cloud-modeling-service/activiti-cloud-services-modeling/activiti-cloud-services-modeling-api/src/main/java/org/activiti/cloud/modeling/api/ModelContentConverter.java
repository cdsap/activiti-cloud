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
import java.util.Optional;
import org.activiti.cloud.services.common.file.FileContent;

/**
 * Model content converter interface
 */
public interface ModelContentConverter<T extends ModelContent> {
    /**
     * Get handled model type by this converter.
     * @return handled model type
     */
    ModelType getHandledModelType();

    /**
     * Convert a bytes array to the handled model content entity.
     * @param bytes the bytes to convert
     * @return the model content, or {@link Optional#empty()}
     */
    Optional<T> convertToModelContent(byte[] bytes);

    /**
     * Convert an instance of the model content to bytes array
     * @param modelContent the model content instance to convert
     * @return the bytes array
     */
    byte[] convertToBytes(T modelContent);

    /**
     * Converts the file content to the handled model content entity, sets the given id, and converts back to a FileContent.
     * If the id is already the expected one, it returns it unchanged.
     * @param fileContent the FileContent wrapper to convert and fix
     * @param modelIdentifiers the mapping of the old ids to the new ones
     * @return the fixed FileContent
     */
    default FileContent overrideModelId(FileContent fileContent, Map<String, String> modelIdentifiers) {
        return fileContent;
    }
}
