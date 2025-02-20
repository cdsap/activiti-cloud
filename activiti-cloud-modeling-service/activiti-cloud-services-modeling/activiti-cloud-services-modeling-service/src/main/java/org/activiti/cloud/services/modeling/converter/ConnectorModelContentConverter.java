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
package org.activiti.cloud.services.modeling.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.activiti.cloud.modeling.api.ConnectorModelType;
import org.activiti.cloud.modeling.api.ModelContentConverter;
import org.activiti.cloud.modeling.api.ModelType;
import org.activiti.cloud.modeling.converter.JsonConverter;
import org.activiti.cloud.modeling.core.error.ImportModelException;
import org.activiti.cloud.services.common.file.FileContent;

/**
 * Implementation of {@link ModelContentConverter} for connectors models
 */
public class ConnectorModelContentConverter implements ModelContentConverter<ConnectorModelContent> {

    private final ConnectorModelType connectorModelType;

    private final JsonConverter<ConnectorModelContent> connectorModelContentJsonConverter;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ConnectorModelContentConverter(
        ConnectorModelType connectorModelType,
        JsonConverter<ConnectorModelContent> connectorModelContentJsonConverter
    ) {
        this.connectorModelType = connectorModelType;
        this.connectorModelContentJsonConverter = connectorModelContentJsonConverter;
    }

    @Override
    public ModelType getHandledModelType() {
        return connectorModelType;
    }

    @Override
    public Optional<ConnectorModelContent> convertToModelContent(byte[] bytes) {
        return Optional.ofNullable(connectorModelContentJsonConverter.convertToEntity(bytes));
    }

    @Override
    public byte[] convertToBytes(ConnectorModelContent connectorModelContent) {
        return connectorModelContentJsonConverter.convertToJsonBytes(connectorModelContent);
    }

    @Override
    public FileContent overrideModelId(FileContent fileContent, Map<String, String> modelIdentifiers) {
        try {
            ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(fileContent.getFileContent());
            this.updateConnectorIdFromFileContent(jsonNode, modelIdentifiers);
            return new FileContent(
                fileContent.getFilename(),
                fileContent.getContentType(),
                objectMapper.writeValueAsBytes(jsonNode)
            );
        } catch (IOException e) {
            throw new ImportModelException(e);
        }
    }

    private void updateConnectorIdFromFileContent(ObjectNode jsonNode, Map<String, String> modelIdentifiers) {
        String actualId = jsonNode.get("id") != null ? modelIdentifiers.get(jsonNode.get("id").asText()) : null;
        if (actualId != null) {
            jsonNode.put("id", actualId);
        }
    }
}
