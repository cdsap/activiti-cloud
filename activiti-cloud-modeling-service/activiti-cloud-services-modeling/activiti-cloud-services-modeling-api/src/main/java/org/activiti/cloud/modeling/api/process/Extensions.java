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

package org.activiti.cloud.modeling.api.process;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * Model extensions
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public class Extensions {

    @JsonProperty("properties")
    private Map<String, ProcessVariable> processVariables = new HashMap<>();

    @JsonProperty("mappings")
    private Map<String, TaskVariableMapping> variablesMappings = new HashMap<>();

    @JsonProperty("constants")
    private Map<String, Map<String, Constant>> constants = new HashMap<>();

    @JsonProperty("templates")
    private TemplatesDefinition templates = new TemplatesDefinition();

    public Map<String, ProcessVariable> getProcessVariables() {
        return processVariables;
    }

    public void setProcessVariables(Map<String, ProcessVariable> processVariables) {
        this.processVariables = processVariables;
    }

    public Map<String, TaskVariableMapping> getVariablesMappings() {
        return variablesMappings;
    }

    public void setVariablesMappings(Map<String, TaskVariableMapping> variablesMappings) {
        this.variablesMappings = variablesMappings;
    }

    public Map<String, Map<String, Constant>> getConstants() {
        return constants;
    }

    public void setConstants(Map<String, Map<String, Constant>> constants) {
        this.constants = constants;
    }

    public TemplatesDefinition getTemplates() {
        return templates;
    }

    public void setTemplates(TemplatesDefinition templates) {
        this.templates = templates;
    }

    public Map<String, Object> getAsMap() {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("properties", this.processVariables);
        extensions.put("mappings", this.variablesMappings);
        extensions.put("constants", this.constants);
        extensions.put("templates", this.templates);
        return extensions;
    }
}
