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
package org.activiti.cloud.services.modeling.validation.extensions;

import java.util.ArrayList;
import java.util.List;
import org.activiti.cloud.modeling.api.Model;
import org.activiti.cloud.modeling.api.ModelType;
import org.activiti.cloud.modeling.api.ModelValidationError;
import org.activiti.cloud.modeling.api.ValidationContext;
import org.everit.json.schema.Schema;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of {@link ExtensionsJsonSchemaValidator} for validating the common extensions schema
 */
public class ExtensionsModelValidator extends ExtensionsJsonSchemaValidator {

    private final Schema modelExtensionsSchema;

    @Autowired
    public ExtensionsModelValidator(Schema modelExtensionsSchema) {
        this.modelExtensionsSchema = modelExtensionsSchema;
    }

    @Override
    public ModelType getHandledModelType() {
        return null;
    }

    @Override
    protected Schema schema() {
        return modelExtensionsSchema;
    }

    @Override
    protected List<ModelValidationError> getValidationErrors(Model model, ValidationContext validationContext) {
        // No further validation needed
        return new ArrayList<>();
    }
}
