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
package org.activiti.cloud.services.modeling.jpa.version;

import java.util.Optional;

/**
 * Version generator.
 *
 * TODO: To set VersionGenerationStrategy from application properties, if any.
 */
public class VersionGenerator {

    private VersionGenerationStrategy versionGenerationStrategy = IncrementPatchVersionGenerationStrategy.INSTANCE;

    /**
     * Generate the next version relative to the given latest version.
     *
     * @param latestVersion the latest version
     * @return the next version
     */
    public String generateNextVersion(VersionEntity latestVersion) {
        return versionGenerationStrategy.generateNextVersion(
            Optional.ofNullable(latestVersion).map(VersionEntity::getVersion).orElse(null)
        );
    }
}
