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

import static org.activiti.cloud.services.rest.controllers.TaskSamples.buildDefaultAssignedTask;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.activiti.api.runtime.shared.query.Page;
import org.activiti.api.task.model.Task;
import org.activiti.api.task.model.builders.TaskPayloadBuilder;
import org.activiti.api.task.model.payloads.AssignTaskPayload;
import org.activiti.api.task.model.payloads.AssignTasksPayload;
import org.activiti.api.task.model.payloads.UpdateTaskPayload;
import org.activiti.api.task.runtime.TaskAdminRuntime;
import org.activiti.cloud.alfresco.config.AlfrescoWebAutoConfiguration;
import org.activiti.cloud.services.core.pageable.SpringPageConverter;
import org.activiti.cloud.services.events.ProcessEngineChannels;
import org.activiti.cloud.services.events.configuration.CloudEventsAutoConfiguration;
import org.activiti.cloud.services.events.configuration.ProcessEngineChannelsConfiguration;
import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.activiti.cloud.services.events.listeners.CloudProcessDeployedProducer;
import org.activiti.cloud.services.rest.conf.ServicesRestWebMvcAutoConfiguration;
import org.activiti.cloud.services.rest.config.StreamConfig;
import org.activiti.common.util.conf.ActivitiCoreCommonUtilAutoConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.runtime.api.query.impl.PageImpl;
import org.activiti.spring.process.conf.ProcessExtensionsAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskAdminControllerImpl.class)
@EnableSpringDataWebSupport
@AutoConfigureMockMvc
@Import(
    {
        RuntimeBundleProperties.class,
        CloudEventsAutoConfiguration.class,
        ProcessEngineChannelsConfiguration.class,
        TaskSamples.class,
        ActivitiCoreCommonUtilAutoConfiguration.class,
        ProcessExtensionsAutoConfiguration.class,
        ServicesRestWebMvcAutoConfiguration.class,
        AlfrescoWebAutoConfiguration.class,
        StreamConfig.class,
    }
)
public class TaskAdminControllerImplIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private TaskAdminRuntime taskAdminRuntime;

    @MockBean
    private RepositoryService repositoryService;

    @SpyBean
    private SpringPageConverter pageConverter;

    @Autowired
    private ProcessEngineChannels processEngineChannels;

    @MockBean
    private CloudProcessDeployedProducer processDeployedProducer;

    @BeforeEach
    public void setUp() {
        assertThat(pageConverter).isNotNull();
        assertThat(processEngineChannels).isNotNull();
        assertThat(processDeployedProducer).isNotNull();
    }

    @Test
    public void getTasks() throws Exception {
        List<Task> taskList = Collections.singletonList(buildDefaultAssignedTask());
        Page<Task> tasks = new PageImpl<>(taskList, taskList.size());
        when(taskAdminRuntime.tasks(any())).thenReturn(tasks);

        this.mockMvc.perform(get("/admin/v1/tasks?page=0&size=10").accept(MediaTypes.HAL_JSON_VALUE))
            .andExpect(status().isOk());
    }

    @Test
    public void getTasksShouldUseAlfrescoGuidelineWhenMediaTypeIsApplicationJson() throws Exception {
        List<Task> taskList = Collections.singletonList(buildDefaultAssignedTask());
        Page<Task> taskPage = new PageImpl<>(taskList, taskList.size());
        when(taskAdminRuntime.tasks(any())).thenReturn(taskPage);

        this.mockMvc.perform(get("/admin/v1/tasks?skipCount=10&maxItems=10").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    public void deleteTask() throws Exception {
        given(taskAdminRuntime.delete(any())).willReturn(buildDefaultAssignedTask());
        this.mockMvc.perform(delete("/admin/v1/tasks/{taskId}", 1)).andExpect(status().isOk());
    }

    @Test
    public void updateTask() throws Exception {
        given(taskAdminRuntime.update(any())).willReturn(buildDefaultAssignedTask());
        UpdateTaskPayload updateTaskCmd = TaskPayloadBuilder
            .update()
            .withTaskId("1")
            .withName("update-task")
            .withDescription("update-description")
            .build();

        this.mockMvc.perform(
                put("/admin/v1/tasks/{taskId}", 1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(updateTaskCmd))
            )
            .andExpect(status().isOk());
    }

    @Test
    public void completeTask() throws Exception {
        given(taskAdminRuntime.complete(any())).willReturn(buildDefaultAssignedTask());
        this.mockMvc.perform(post("/admin/v1/tasks/{taskId}/complete", 1)).andExpect(status().isOk());
    }

    @Test
    public void assignTask() throws Exception {
        given(taskAdminRuntime.assign(any())).willReturn(buildDefaultAssignedTask());
        AssignTaskPayload assignTaskCmd = TaskPayloadBuilder.assign().withTaskId("1").withAssignee("assignee").build();

        this.mockMvc.perform(
                post("/admin/v1/tasks/{taskId}/assign", 1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(assignTaskCmd))
            )
            .andExpect(status().isOk());
    }

    @Test
    public void assignMultipleTasks() throws Exception {
        given(taskAdminRuntime.assignMultiple(any())).willReturn(new PageImpl(List.of(buildDefaultAssignedTask()), 1));
        AssignTasksPayload assignTasksCmd = TaskPayloadBuilder
            .assignMultiple()
            .withTaskId("1")
            .withAssignee("assignee")
            .build();

        this.mockMvc.perform(
                post("/admin/v1/tasks/assign")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(assignTasksCmd))
            )
            .andExpect(status().isOk());
    }
}
