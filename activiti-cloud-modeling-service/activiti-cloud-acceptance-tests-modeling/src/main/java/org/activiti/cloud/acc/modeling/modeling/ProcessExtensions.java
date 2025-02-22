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

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toMap;
import static org.activiti.cloud.modeling.api.process.VariableMappingType.VALUE;
import static org.activiti.cloud.modeling.api.process.VariableMappingType.VARIABLE;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.activiti.cloud.modeling.api.process.Extensions;
import org.activiti.cloud.modeling.api.process.ProcessVariable;
import org.activiti.cloud.modeling.api.process.ProcessVariableMapping;
import org.activiti.cloud.modeling.api.process.TaskVariableMapping;

/**
 * Modeling utils
 */
public class ProcessExtensions {

    public static final String EXTENSIONS_TASK_NAME = "ServiceTask";

    public static final String HOST_VALUE = "${host}";

    public static Extensions extensions(String... processVariables) {
        return extensions(Arrays.asList(processVariables));
    }

    public static Extensions extensions(Collection<String> processVariables) {
        Extensions extensions = new Extensions();
        extensions.setProcessVariables(processVariables(processVariables));
        extensions.setVariablesMappings(variableMappings(processVariables));
        return extensions;
    }

    public static Map<String, ProcessVariable> processVariables(Collection<String> processVariables) {
        return processVariables
            .stream()
            .collect(Collectors.toMap(Function.identity(), ProcessExtensions::toProcessVariable));
    }

    public static Map<String, TaskVariableMapping> variableMappings(Collection<String> processVariables) {
        TaskVariableMapping taskVariableMapping = new TaskVariableMapping();
        taskVariableMapping.setInputs(
            processVariables
                .stream()
                .collect(toMap(Function.identity(), ProcessExtensions::toFixedProcessVariableMapping))
        );
        taskVariableMapping.setOutputs(
            processVariables
                .stream()
                .collect(toMap(Function.identity(), ProcessExtensions::toVariableProcessVariableMapping))
        );

        return singletonMap(EXTENSIONS_TASK_NAME, taskVariableMapping);
    }

    public static ProcessVariable toProcessVariable(String name) {
        ProcessVariable processVariable = new ProcessVariable();
        processVariable.setName(name);
        processVariable.setId(name);
        processVariable.setType("boolean");
        processVariable.setValue(true);
        return processVariable;
    }

    public static ProcessVariableMapping toFixedProcessVariableMapping(String name) {
        ProcessVariableMapping processVariableMapping = new ProcessVariableMapping();
        processVariableMapping.setType(VALUE);
        processVariableMapping.setValue(name);
        return processVariableMapping;
    }

    public static ProcessVariableMapping toVariableProcessVariableMapping(String name) {
        ProcessVariableMapping processVariableMapping = new ProcessVariableMapping();
        processVariableMapping.setType(VARIABLE);
        processVariableMapping.setValue(HOST_VALUE);
        return processVariableMapping;
    }
}
