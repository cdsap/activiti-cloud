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
package org.activiti.cloud.services.rest.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessAdminRuntime;
import org.activiti.api.runtime.model.impl.ProcessInstanceImpl;
import org.activiti.api.runtime.model.impl.VariableInstanceImpl;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.activiti.cloud.alfresco.config.AlfrescoWebAutoConfiguration;
import org.activiti.cloud.services.events.ProcessEngineChannels;
import org.activiti.cloud.services.events.configuration.CloudEventsAutoConfiguration;
import org.activiti.cloud.services.events.configuration.ProcessEngineChannelsConfiguration;
import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.activiti.cloud.services.events.listeners.CloudProcessDeployedProducer;
import org.activiti.cloud.services.rest.assemblers.CollectionModelAssembler;
import org.activiti.cloud.services.rest.assemblers.ProcessInstanceVariableRepresentationModelAssembler;
import org.activiti.cloud.services.rest.conf.ServicesRestWebMvcAutoConfiguration;
import org.activiti.cloud.services.rest.config.StreamConfig;
import org.activiti.common.util.conf.ActivitiCoreCommonUtilAutoConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.spring.process.conf.ProcessExtensionsAutoConfiguration;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@WebMvcTest(ProcessInstanceVariableAdminControllerImpl.class)
@EnableSpringDataWebSupport
@AutoConfigureMockMvc
@Import(
    {
        RuntimeBundleProperties.class,
        CloudEventsAutoConfiguration.class,
        ProcessEngineChannelsConfiguration.class,
        ActivitiCoreCommonUtilAutoConfiguration.class,
        ProcessExtensionsAutoConfiguration.class,
        ServicesRestWebMvcAutoConfiguration.class,
        AlfrescoWebAutoConfiguration.class,
        StreamConfig.class,
    }
)
public class ProcessInstanceVariableAdminControllerImplIT {

    private static final String PROCESS_INSTANCE_ID = UUID.randomUUID().toString();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProcessAdminRuntime processAdminRuntime;

    @MockBean
    private Map<String, ProcessExtensionModel> processExtensionModelMap;

    @MockBean
    private TaskAdminRuntime taskAdminRuntime;

    @MockBean(name = ProcessEngineChannels.COMMAND_RESULTS)
    private MessageChannel commandResults;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CollectionModelAssembler resourcesAssembler;

    @MockBean
    private ProcessInstanceVariableRepresentationModelAssembler variableRepresentationModelAssembler;

    @MockBean
    private RepositoryService repositoryService;

    @Autowired
    private ProcessEngineChannels processEngineChannels;

    @MockBean
    private CloudProcessDeployedProducer processDeployedProducer;

    @BeforeEach
    public void setUp() {
        ProcessInstanceImpl processInstance;
        processInstance = new ProcessInstanceImpl();
        processInstance.setId("1");
        processInstance.setProcessDefinitionKey("1");

        this.mockMvc =
            MockMvcBuilders
                .standaloneSetup(
                    new ProcessInstanceVariableAdminControllerImpl(
                        variableRepresentationModelAssembler,
                        processAdminRuntime,
                        resourcesAssembler
                    )
                )
                .setControllerAdvice(new RuntimeBundleExceptionHandler())
                .build();

        given(processAdminRuntime.processInstance(any())).willReturn(processInstance);
    }

    @Test
    public void shouldGetVariables() throws Exception {
        VariableInstanceImpl<String> name = new VariableInstanceImpl<>(
            "name",
            String.class.getName(),
            "Paul",
            PROCESS_INSTANCE_ID,
            null
        );
        VariableInstanceImpl<Integer> age = new VariableInstanceImpl<>(
            "age",
            Integer.class.getName(),
            12,
            PROCESS_INSTANCE_ID,
            null
        );
        given(processAdminRuntime.variables(any())).willReturn(Arrays.asList(name, age));

        this.mockMvc.perform(
                get("/admin/v1/process-instances/{processInstanceId}/variables", 1, 1).accept(MediaTypes.HAL_JSON_VALUE)
            )
            .andExpect(status().isOk());
    }

    @Test
    public void shouldReturn200WithEmptyErrorListWhenSetVariablesWithCorrectNamesAndTypes() throws Exception {
        //GIVEN
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "Alice");
        variables.put("age", 24);
        variables.put("subscribe", false);
        String expectedResponseBody = "";

        //WHEN
        ResultActions resultActions = mockMvc
            .perform(
                put("/admin/v1/process-instances/1/variables", 1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentType(MediaTypes.HAL_JSON_VALUE)
                    .content(
                        mapper.writeValueAsString(
                            ProcessPayloadBuilder
                                .setVariables()
                                .withProcessInstanceId("1")
                                .withVariables(variables)
                                .build()
                        )
                    )
            )
            //THEN
            .andExpect(status().isOk());
        MvcResult result = resultActions.andReturn();
        String actualResponseBody = result.getResponse().getContentAsString();

        assertThat(expectedResponseBody).isEqualTo(actualResponseBody);
        verify(processAdminRuntime).setVariables(any());
    }

    @Test
    public void deleteVariables() throws Exception {
        this.mockMvc.perform(
                delete("/admin/v1/process-instances/{processInstanceId}/variables", "1")
                    .accept(MediaTypes.HAL_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        mapper.writeValueAsString(
                            ProcessPayloadBuilder
                                .removeVariables()
                                .withVariableNames(Arrays.asList("varName1", "varName2"))
                                .build()
                        )
                    )
            )
            .andExpect(status().isOk());
        verify(processAdminRuntime).removeVariables(any());
    }
}
