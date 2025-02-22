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

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.cloud.modeling.api.ConnectorModelType;
import org.activiti.cloud.modeling.api.ProcessModelType;
import org.activiti.cloud.modeling.converter.JsonConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Configuration for process model validator
 */
@AutoConfiguration
public class ProcessModelConverterConfiguration {

    @Bean
    public BpmnXMLConverter bpmnXMLConverter() {
        return new BpmnXMLConverter();
    }

    @Bean
    public ConnectorModelContentConverter connectorModelContentConverter(
        ConnectorModelType connectorModelType,
        JsonConverter<ConnectorModelContent> connectorModelContentJsonConverter
    ) {
        return new ConnectorModelContentConverter(connectorModelType, connectorModelContentJsonConverter);
    }

    @Bean
    public ProcessModelContentConverter processModelContentConverter(
        ProcessModelType processModelType,
        BpmnXMLConverter bpmnConverter
    ) {
        return new ProcessModelContentConverter(processModelType, bpmnConverter);
    }
}
