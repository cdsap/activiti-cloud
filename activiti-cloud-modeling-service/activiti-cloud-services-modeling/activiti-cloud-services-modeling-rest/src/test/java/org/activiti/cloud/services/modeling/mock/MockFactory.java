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
package org.activiti.cloud.services.modeling.mock;

import static java.util.Collections.singletonMap;
import static org.activiti.cloud.modeling.api.ProcessModelType.BPMN20_XML;
import static org.activiti.cloud.modeling.api.ProcessModelType.PROCESS;
import static org.activiti.cloud.modeling.api.process.VariableMappingType.VALUE;
import static org.activiti.cloud.services.common.util.ContentTypeUtils.CONTENT_TYPE_JSON;
import static org.activiti.cloud.services.common.util.ContentTypeUtils.CONTENT_TYPE_XML;
import static org.activiti.cloud.services.common.util.ContentTypeUtils.JSON;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.activiti.cloud.modeling.api.ConnectorModelType;
import org.activiti.cloud.modeling.api.Model;
import org.activiti.cloud.modeling.api.process.Extensions;
import org.activiti.cloud.modeling.api.process.ProcessVariable;
import org.activiti.cloud.modeling.api.process.ProcessVariableMapping;
import org.activiti.cloud.modeling.api.process.TaskVariableMapping;
import org.activiti.cloud.services.common.file.FileContent;
import org.activiti.cloud.services.modeling.entity.ModelEntity;
import org.activiti.cloud.services.modeling.entity.ProjectEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.StringUtils;

/**
 * Mocks factory
 */
public class MockFactory {

    public static ProjectEntity project(String name) {
        return new ProjectEntity(name);
    }

    public static ProjectEntity projectWithDescription(String name, String description) {
        ProjectEntity project = new ProjectEntity(name);
        project.setDescription(description);
        return project;
    }

    public static ProjectEntity projectWithDisplayName(String name, String displayName) {
        ProjectEntity project = new ProjectEntity(name);
        project.setDisplayName(displayName);
        return project;
    }

    public static ModelEntity processModel(String name) {
        return processModelWithExtensions(name, null);
    }

    public static ModelEntity processModel(ProjectEntity parentProject, String name) {
        ModelEntity model = processModel(name);
        model.addProject(parentProject);
        return model;
    }

    public static ModelEntity processModelWithExtensions(String name, Map<String, Extensions> extensions) {
        return processModelWithExtensions(null, name, extensions != null ? extensions.get(name) : null);
    }

    public static ModelEntity processModelWithExtensions(String name, Extensions extensions, byte[] content) {
        return processModelWithExtensions(null, name, extensions, content);
    }

    public static ModelEntity processModelWithExtensions(
        ProjectEntity parentProject,
        String name,
        Extensions extensions
    ) {
        return processModelWithExtensions(parentProject, name, extensions, null);
    }

    public static ModelEntity processModelWithExtensions(
        ProjectEntity parentProject,
        String name,
        Extensions extensions,
        byte[] content
    ) {
        ModelEntity processModel = new ModelEntity(name, PROCESS);
        processModel.addProject(parentProject);
        processModel.setExtensions(extensions != null ? buildExtensions(name, extensions) : null);
        if (content != null) {
            processModel.setContentType(CONTENT_TYPE_XML);
            processModel.setContent(content);
        }
        return processModel;
    }

    private static Map<String, Object> buildExtensions(String name, Extensions extensions) {
        Map<String, Object> generatedExtension = new HashMap<>();
        generatedExtension.put(name, extensions.getAsMap());
        return generatedExtension;
    }

    public static ModelEntity processModelWithContent(String name, String content) {
        return processModelWithContent(null, name, content);
    }

    public static ModelEntity processModelWithContent(ProjectEntity project, String name, byte[] content) {
        return processModelWithContent(project, name, new String(content));
    }

    public static ModelEntity processModelWithContent(ProjectEntity project, String name, String content) {
        return processModelWithContent(project, name, null, content);
    }

    public static ModelEntity processModelWithContent(
        ProjectEntity project,
        String name,
        Extensions extensions,
        byte[] content
    ) {
        return processModelWithContent(project, name, extensions, new String(content));
    }

    public static ModelEntity processModelWithContent(
        ProjectEntity project,
        String name,
        Extensions extensions,
        String content
    ) {
        ModelEntity processModel = processModel(name);
        processModel.addProject(project);
        processModel.setExtensions(extensions != null ? buildExtensions(name, extensions) : null);
        if (content != null) {
            processModel.setContentType(CONTENT_TYPE_XML);
            processModel.setContent(content.getBytes());
        }
        return processModel;
    }

    public static Map<String, Extensions> extensions(byte[] bytes) {
        try {
            return getExtensionMapFromJson(new ObjectMapper().readValue(bytes, ModelEntity.class).getExtensions());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Map<String, Extensions> getExtensionMapFromJson(Map<String, Object> map) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String exstensionJson = objectMapper.writeValueAsString(map);
        if (StringUtils.isEmpty(exstensionJson)) {
            return null;
        }
        return objectMapper.readValue(
            exstensionJson,
            objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Extensions.class)
        );
    }

    public static Extensions extensions(String serviceTask, String... processVariables) {
        return extensions(
            serviceTask,
            processVariables(processVariables),
            inputsMappings(processVariables),
            outputsMappings(processVariables)
        );
    }

    public static Extensions extensions(
        String serviceTask,
        Map<String, ProcessVariable> processVariables,
        Map<String, ProcessVariableMapping> inputsMappings,
        Map<String, ProcessVariableMapping> outputsMappings
    ) {
        Extensions extensions = new Extensions();
        TaskVariableMapping taskVariableMapping = new TaskVariableMapping();
        taskVariableMapping.setInputs(inputsMappings);
        taskVariableMapping.setOutputs(outputsMappings);
        extensions.setProcessVariables(processVariables);
        extensions.setVariablesMappings(singletonMap(serviceTask, taskVariableMapping));
        return extensions;
    }

    public static Map<String, ProcessVariable> processVariables(String... processVariables) {
        return Arrays
            .stream(processVariables)
            .map(MockFactory::processVariable)
            .collect(Collectors.toMap(ProcessVariable::getId, Function.identity()));
    }

    public static Map<String, ProcessVariableMapping> inputsMappings(String... processVariables) {
        return Arrays
            .stream(processVariables)
            .collect(Collectors.toMap(Function.identity(), MockFactory::processVariableMapping));
    }

    public static Map<String, ProcessVariableMapping> outputsMappings(String... processVariables) {
        return Arrays
            .stream(processVariables)
            .collect(Collectors.toMap(Function.identity(), MockFactory::processVariableMapping));
    }

    public static ProcessVariable processVariable(String name) {
        String type;
        Object value;
        if (name.startsWith("int")) {
            type = "integer";
            value = name.length();
        } else if (name.startsWith("boolean")) {
            type = "boolean";
            value = true;
        } else if (name.startsWith("date")) {
            type = "date";
            value = new Date(0);
        } else if (name.startsWith("json")) {
            type = "json";
            value = json("json-field-name", name);
        } else {
            type = "string";
            value = name;
        }
        ProcessVariable processVariable = new ProcessVariable();
        processVariable.setId(name);
        processVariable.setName(name);
        processVariable.setType(type);
        processVariable.setValue(value);
        return processVariable;
    }

    public static JsonNode json(String field, String value) {
        return json("{\"" + field + "\": \"" + value + "\"}");
    }

    public static JsonNode json(String json) {
        try {
            return new ObjectMapper().readValue(json, JsonNode.class);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ProcessVariableMapping processVariableMapping(String name) {
        ProcessVariableMapping processVariableMapping = new ProcessVariableMapping();
        processVariableMapping.setType(VALUE);
        processVariableMapping.setValue(name);
        return processVariableMapping;
    }

    public static ModelEntity connectorModel(String name) {
        return new ModelEntity(name, ConnectorModelType.NAME);
    }

    public static ModelEntity connectorModel(ProjectEntity parentProject, String name) {
        return connectorModel(parentProject, name, null);
    }

    public static ModelEntity connectorModel(ProjectEntity parentProject, String name, byte[] content) {
        ModelEntity modelEntity = connectorModel(name);
        modelEntity.addProject(parentProject);
        if (content != null) {
            modelEntity.setContentType(CONTENT_TYPE_JSON);
            modelEntity.setContent(content);
        }
        return modelEntity;
    }

    public static String id() {
        return UUID.randomUUID().toString();
    }

    public static FileContent processFileContent(String processName, byte[] content) {
        return new FileContent(processName + "." + BPMN20_XML, CONTENT_TYPE_XML, content);
    }

    public static FileContent connectorFileContent(String connectorName, byte[] content) {
        return new FileContent(connectorName + "." + JSON, CONTENT_TYPE_JSON, content);
    }

    public static FileContent processFileContentWithCallActivity(
        String mainProcessName,
        Model callActivity,
        byte[] content
    ) {
        return new FileContent(
            mainProcessName + "." + BPMN20_XML,
            CONTENT_TYPE_XML,
            new String(content)
                .replaceFirst("calledElement=\".*\"", "calledElement=\"" + callActivity.getName() + "\"")
                .getBytes()
        );
    }

    public static MockMultipartFile multipartExtensionsFile(Model model, byte[] content) {
        return new MockMultipartFile(
            "file",
            model.getName() + "-extensions.json",
            CONTENT_TYPE_JSON,
            new String(content).replaceAll("\"id\": \".*\"", "\"id\": \"process-" + model.getId() + "\"").getBytes()
        );
    }

    public static MockMultipartFile multipartProcessFile(Model model, byte[] content) {
        return new MockMultipartFile("file", model.getName() + "." + BPMN20_XML, CONTENT_TYPE_XML, content);
    }
}
