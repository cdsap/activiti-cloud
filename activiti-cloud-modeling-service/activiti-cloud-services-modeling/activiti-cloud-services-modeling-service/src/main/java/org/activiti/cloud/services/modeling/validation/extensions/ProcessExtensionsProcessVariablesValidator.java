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
import static org.activiti.cloud.modeling.api.process.ServiceTaskActionType.INPUTS;
import static org.activiti.cloud.modeling.api.process.VariableMappingType.VARIABLE;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.activiti.cloud.modeling.api.ModelValidationError;
import org.activiti.cloud.modeling.api.ValidationContext;
import org.activiti.cloud.modeling.api.process.Extensions;
import org.activiti.cloud.modeling.api.process.ProcessVariable;
import org.activiti.cloud.modeling.api.process.ProcessVariableMapping;
import org.activiti.cloud.modeling.api.process.ServiceTaskActionType;
import org.activiti.cloud.services.modeling.converter.BpmnProcessModelContent;

/**
 * Implementation of {@link ProcessExtensionsValidator} for validating process variables
 */
public class ProcessExtensionsProcessVariablesValidator implements ProcessExtensionsValidator {

    public static final String UNKNOWN_PROCESS_VARIABLE_VALIDATION_ERROR_PROBLEM =
        "Unknown process variable in process extensions: %s";
    public static final String UNKNOWN_PROCESS_VARIABLE_VALIDATION_ERROR_DESCRIPTION =
        "The extensions for process '%s' contains mappings for an unknown process variable '%s'";

    @Override
    public Stream<ModelValidationError> validateExtensions(
        Extensions extensions,
        BpmnProcessModelContent bpmnModel,
        ValidationContext validationContext
    ) {
        Set<String> availableProcessVariables = getAvailableProcessVariables(extensions);

        return extensions
            .getVariablesMappings()
            .values()
            .stream()
            .flatMap(actionMappings -> actionMappings.getServiceTaskActionTypeMap().entrySet().stream())
            .flatMap(taskMappingEntry ->
                this.validateProcessVariableMapping(
                        taskMappingEntry.getKey(),
                        taskMappingEntry.getValue(),
                        bpmnModel.getId(),
                        availableProcessVariables
                    )
            );
    }

    private Stream<ModelValidationError> validateProcessVariableMapping(
        ServiceTaskActionType action,
        Map<String, ProcessVariableMapping> processVariableMappings,
        String processId,
        Set<String> availableProcessVariables
    ) {
        return processVariableMappings
            .entrySet()
            .stream()
            .map(valiableMappingEntry ->
                validateProcessVariableMapping(
                    action,
                    valiableMappingEntry.getKey(),
                    valiableMappingEntry.getValue(),
                    processId,
                    availableProcessVariables
                )
            )
            .filter(Optional::isPresent)
            .map(Optional::get);
    }

    private Optional<ModelValidationError> validateProcessVariableMapping(
        ServiceTaskActionType action,
        String processVariableMappingKey,
        ProcessVariableMapping processVariableMapping,
        String processId,
        Set<String> availableProcessVariables
    ) {
        Object variableName = action == INPUTS ? processVariableMapping.getValue() : processVariableMappingKey;
        return processVariableMapping.getType() == VARIABLE && !availableProcessVariables.contains(variableName)
            ? Optional.of(
                new ModelValidationError(
                    format(UNKNOWN_PROCESS_VARIABLE_VALIDATION_ERROR_PROBLEM, variableName),
                    format(UNKNOWN_PROCESS_VARIABLE_VALIDATION_ERROR_DESCRIPTION, processId, variableName)
                )
            )
            : Optional.empty();
    }

    private Set<String> getAvailableProcessVariables(Extensions extensions) {
        return extensions
            .getProcessVariables()
            .values()
            .stream()
            .map(ProcessVariable::getName)
            .collect(Collectors.toSet());
    }
}
