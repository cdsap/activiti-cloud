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

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.activiti.cloud.modeling.api.Model;
import org.activiti.cloud.modeling.api.ModelType;
import org.activiti.cloud.modeling.api.ModelValidationError;
import org.activiti.cloud.modeling.api.ProcessModelType;
import org.activiti.cloud.modeling.api.ValidationContext;
import org.activiti.cloud.modeling.api.process.Extensions;
import org.activiti.cloud.modeling.converter.JsonConverter;
import org.activiti.cloud.modeling.core.error.ModelingException;
import org.activiti.cloud.modeling.core.error.SyntacticModelValidationException;
import org.activiti.cloud.services.modeling.converter.BpmnProcessModelContent;
import org.activiti.cloud.services.modeling.converter.ProcessModelContentConverter;
import org.everit.json.schema.Schema;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessExtensionsModelValidator extends ExtensionsJsonSchemaValidator {

    public static final String UNKNOWN_PROCESS_ID_VALIDATION_ERROR_PROBLEM =
        "Unknown process id in process extensions: %s";
    public static final String UNKNOWN_PROCESS_ID_VALIDATION_ERROR_DESCRIPTION =
        "The process extensions are bound to an unknown process id '%s'";

    private final Schema processExtensionsSchema;

    private final Set<ProcessExtensionsValidator> processExtensionsValidators;

    private final ProcessModelType processModelType;

    private JsonConverter<Extensions> jsonExtensionsConverter;

    private final ProcessModelContentConverter processModelContentConverter;

    @Autowired
    public ProcessExtensionsModelValidator(
        Schema processExtensionsSchema,
        Set<ProcessExtensionsValidator> processExtensionsValidators,
        ProcessModelType processModelType,
        JsonConverter<Extensions> jsonExtensionsConverter,
        ProcessModelContentConverter processModelContentConverter
    ) {
        this.processExtensionsSchema = processExtensionsSchema;
        this.processExtensionsValidators = processExtensionsValidators;
        this.processModelType = processModelType;
        this.jsonExtensionsConverter = jsonExtensionsConverter;
        this.processModelContentConverter = processModelContentConverter;
    }

    @Override
    protected List<ModelValidationError> getValidationErrors(Model model, ValidationContext context) {
        return Optional
            .ofNullable(model.getId())
            .map(modelId -> removeStart(modelId, processModelType.getName().toLowerCase() + "-"))
            .flatMap(modelId -> findProcessModelInContext(modelId, context))
            .map(Model::getContent)
            .flatMap(this::convertToBpmnModel)
            .map(bpmnModel -> validateBpmnModel(model, context, bpmnModel))
            .orElseGet(() ->
                Stream.of(
                    new ModelValidationError(
                        format(UNKNOWN_PROCESS_ID_VALIDATION_ERROR_PROBLEM, model.getId()),
                        format(UNKNOWN_PROCESS_ID_VALIDATION_ERROR_DESCRIPTION, model.getId())
                    )
                )
            )
            .collect(Collectors.toList());
    }

    protected Stream<ModelValidationError> validateBpmnModel(
        Model model,
        ValidationContext validationContext,
        BpmnProcessModelContent bpmnModel
    ) {
        return bpmnModel
            .getBpmnModel()
            .getProcesses()
            .stream()
            .map(process -> this.retrieveExtensionByProcessId(model, process.getId()))
            .flatMap(extensions -> validateProcessExtension(extensions, validationContext, bpmnModel));
    }

    private Map<String, Object> retrieveExtensionByProcessId(Model model, String processId) {
        return model.getExtensions() != null && model.getExtensions().get(processId) != null
            ? (Map<String, Object>) model.getExtensions().get(processId)
            : model.getExtensions();
    }

    private Stream<ModelValidationError> validateProcessExtension(
        Map<String, Object> processExtension,
        ValidationContext validationContext,
        BpmnProcessModelContent bpmnModel
    ) {
        return jsonExtensionsConverter
            .tryConvertToEntity(processExtension)
            .map(extensions ->
                processExtensionsValidators
                    .stream()
                    .flatMap(validator -> validator.validateExtensions(extensions, bpmnModel, validationContext))
            )
            .orElseGet(Stream::empty);
    }

    private Optional<Model> findProcessModelInContext(String modelId, ValidationContext validationContext) {
        return validationContext
            .getAvailableModels(processModelType)
            .stream()
            .filter(model -> Objects.equals(model.getId(), modelId))
            .findFirst();
    }

    private Optional<BpmnProcessModelContent> convertToBpmnModel(byte[] bytes) {
        try {
            return processModelContentConverter.convertToModelContent(bytes);
        } catch (ModelingException ex) {
            throw new SyntacticModelValidationException("Cannot convert to BPMN model", ex);
        }
    }

    @Override
    public ModelType getHandledModelType() {
        return processModelType;
    }

    @Override
    public Schema schema() {
        return processExtensionsSchema;
    }
}
