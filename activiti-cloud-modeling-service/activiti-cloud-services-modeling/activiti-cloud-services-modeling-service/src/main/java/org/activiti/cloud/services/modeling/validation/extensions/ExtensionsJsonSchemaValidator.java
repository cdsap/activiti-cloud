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
import java.util.Collection;
import java.util.List;
import org.activiti.cloud.modeling.api.Model;
import org.activiti.cloud.modeling.api.ModelExtensionsValidator;
import org.activiti.cloud.modeling.api.ModelValidationError;
import org.activiti.cloud.modeling.api.ValidationContext;
import org.activiti.cloud.modeling.converter.JsonConverter;
import org.activiti.cloud.modeling.core.error.ModelingException;
import org.activiti.cloud.modeling.core.error.SyntacticModelValidationException;
import org.activiti.cloud.services.modeling.validation.JsonSchemaModelValidator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The generic JSON extensions schema for all the models
 */
public abstract class ExtensionsJsonSchemaValidator
    extends JsonSchemaModelValidator
    implements ModelExtensionsValidator {

    @Autowired
    private JsonConverter<Model> extensionsConverter;

    @Override
    public Collection<ModelValidationError> validate(byte[] bytes, ValidationContext validationContext) {
        Collection<ModelValidationError> parentErrors = new ArrayList<>(super.validate(bytes, validationContext));

        if (!validationContext.isEmpty()) {
            parentErrors.addAll(getValidationErrors(convertBytesToModel(bytes), validationContext));
        }
        return parentErrors;
    }

    protected abstract List<ModelValidationError> getValidationErrors(Model model, ValidationContext validationContext);

    protected Model convertBytesToModel(byte[] bytes) {
        try {
            return extensionsConverter.convertToEntity(bytes);
        } catch (ModelingException ex) {
            throw new SyntacticModelValidationException("Cannot convert json extensions to a model", ex);
        }
    }
}
