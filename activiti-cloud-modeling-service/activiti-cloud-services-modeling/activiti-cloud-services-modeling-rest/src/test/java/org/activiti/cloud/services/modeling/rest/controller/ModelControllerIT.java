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
package org.activiti.cloud.services.modeling.rest.controller;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.activiti.cloud.modeling.api.ProcessModelType.PROCESS;
import static org.activiti.cloud.services.common.util.ContentTypeUtils.CONTENT_TYPE_JSON;
import static org.activiti.cloud.services.common.util.ContentTypeUtils.CONTENT_TYPE_XML;
import static org.activiti.cloud.services.common.util.FileUtils.resourceAsByteArray;
import static org.activiti.cloud.services.modeling.mock.ConstantsBuilder.constantsFor;
import static org.activiti.cloud.services.modeling.mock.IsObjectEquals.isBooleanEquals;
import static org.activiti.cloud.services.modeling.mock.IsObjectEquals.isDateEquals;
import static org.activiti.cloud.services.modeling.mock.IsObjectEquals.isIntegerEquals;
import static org.activiti.cloud.services.modeling.mock.MockFactory.connectorModel;
import static org.activiti.cloud.services.modeling.mock.MockFactory.extensions;
import static org.activiti.cloud.services.modeling.mock.MockFactory.multipartExtensionsFile;
import static org.activiti.cloud.services.modeling.mock.MockFactory.processModel;
import static org.activiti.cloud.services.modeling.mock.MockFactory.processModelWithContent;
import static org.activiti.cloud.services.modeling.mock.MockFactory.processModelWithExtensions;
import static org.activiti.cloud.services.modeling.mock.MockFactory.project;
import static org.activiti.cloud.services.modeling.mock.MockMultipartRequestBuilder.putMultipart;
import static org.activiti.cloud.services.test.asserts.AssertResponseContent.assertThatResponseContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.cloud.modeling.api.ConnectorModelType;
import org.activiti.cloud.modeling.api.Model;
import org.activiti.cloud.modeling.api.ModelValidationError;
import org.activiti.cloud.modeling.api.Project;
import org.activiti.cloud.modeling.api.process.Extensions;
import org.activiti.cloud.modeling.api.process.ModelScope;
import org.activiti.cloud.modeling.api.process.ProcessVariable;
import org.activiti.cloud.modeling.core.error.SemanticModelValidationException;
import org.activiti.cloud.modeling.repository.ModelRepository;
import org.activiti.cloud.modeling.repository.ProjectRepository;
import org.activiti.cloud.services.modeling.config.ModelingRestApplication;
import org.activiti.cloud.services.modeling.entity.ModelEntity;
import org.activiti.cloud.services.modeling.entity.ProjectEntity;
import org.activiti.cloud.services.modeling.jpa.ModelJpaRepository;
import org.activiti.cloud.services.modeling.jpa.ProjectJpaRepository;
import org.activiti.cloud.services.modeling.mock.MockFactory;
import org.activiti.cloud.services.modeling.security.WithMockModelerUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = ModelingRestApplication.class, properties = "spring.jpa.open-in-view=false")
@WebAppConfiguration
@WithMockModelerUser
@Transactional
public class ModelControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelJpaRepository modelJpaRepository;

    @Autowired
    private ProjectJpaRepository projectJpaRepository;

    @MockBean
    private SecurityManager securityManager;

    @BeforeEach
    public void setUp() {
        when(securityManager.getAuthenticatedUserId()).thenReturn("modeler");
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @AfterEach
    public void cleanUp() {
        modelJpaRepository.deleteAll();
        projectJpaRepository.deleteAll();
    }

    @Test
    public void should_returnAllProjectModels_when_gettingProjectModels() throws Exception {
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("parent-project"));

        modelRepository.createModel(processModel(project, "Process Model 1"));
        modelRepository.createModel(processModel(project, "Process Model 2"));

        final ResultActions resultActions = mockMvc
            .perform(get("/v1/projects/{projectId}/models?type=PROCESS", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.models", hasSize(2)))
            .andExpect(jsonPath("$._embedded.models[0].name", is("Process Model 1")))
            .andExpect(jsonPath("$._embedded.models[1].name", is("Process Model 2")));
    }

    @Test
    public void should_returnStatusCreatedAndProcessModelDetails_when_creatingProcessModel() throws Exception {
        Project project = projectRepository.createProject(project("parent-project"));

        mockMvc
            .perform(
                post("/v1/projects/{projectId}/models", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(processModel("process-model")))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", equalTo("process-model")))
            .andExpect(jsonPath("$.type", equalTo(PROCESS)))
            .andExpect(jsonPath("$.extensions", notNullValue()));
    }

    @Test
    public void should_returnStatusCreatedAndConnectorModelDetails_when_creatingConnectorModel() throws Exception {
        Project project = projectRepository.createProject(project("parent-project"));

        mockMvc
            .perform(
                post("/v1/projects/{projectId}/models", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(connectorModel("connector-model")))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", equalTo("connector-model")))
            .andExpect(jsonPath("$.type", equalTo(ConnectorModelType.NAME)));
    }

    @Test
    public void should_returnStatusCreatedAndProcessModelExtensions_when_creatingProcessModelWithExtensions()
        throws Exception {
        Project project = projectRepository.createProject(project("parent-project"));

        Extensions extensions = extensions("ServiceTask", "variable1", "variable2");
        extensions.setConstants(
            constantsFor("ServiceTask")
                .add("myStringConstant", "myStringConstantValue")
                .add("myIntegerConstant", 10)
                .build()
        );
        Map<String, Extensions> processExtension = new HashMap<>();
        processExtension.put("process-model-extensions", extensions);
        ModelEntity processModel = processModelWithExtensions("process-model-extensions", processExtension);
        mockMvc
            .perform(
                post("/v1/projects/{projectId}/models", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(processModel))
            )
            .andExpect(status().isCreated())
            .andExpect(
                jsonPath(
                    "$.extensions.process-model-extensions.properties",
                    allOf(hasKey("variable1"), hasKey("variable2"))
                )
            )
            .andExpect(
                jsonPath(
                    "$.extensions.process-model-extensions.mappings",
                    hasEntry(
                        equalTo("ServiceTask"),
                        allOf(
                            hasEntry(equalTo("inputs"), allOf(hasKey("variable1"), hasKey("variable2"))),
                            hasEntry(equalTo("outputs"), allOf(hasKey("variable1"), hasKey("variable2")))
                        )
                    )
                )
            )
            .andExpect(
                jsonPath(
                    "$.extensions.process-model-extensions.constants",
                    hasEntry(
                        equalTo("ServiceTask"),
                        hasEntry(equalTo("myStringConstant"), hasEntry("value", "myStringConstantValue"))
                    )
                )
            )
            .andExpect(
                jsonPath(
                    "$.extensions.process-model-extensions.constants",
                    hasEntry(equalTo("ServiceTask"), hasEntry(equalTo("myIntegerConstant"), hasEntry("value", 10)))
                )
            );
    }

    @Test
    public void should_throwBadRequestException_when_creatingModelOfUnknownType() throws Exception {
        Project project = projectRepository.createProject(project("parent-project"));

        Model formModel = new ModelEntity("name", "FORM");

        mockMvc
            .perform(
                post("/v1/projects/{projectId}/models", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(formModel))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    public void should_throwConflictException_when_CreatingModelWithExistingName() throws Exception {
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("parent-project"));
        modelRepository.createModel(processModel(project, "process-model"));

        mockMvc
            .perform(
                post("/v1/projects/{projectId}/models", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(processModel("process-model")))
            )
            .andExpect(status().isConflict());
    }

    @Test
    public void should_returnStatusOk_when_gettingAnExistingModel() throws Exception {
        Model processModel = modelRepository.createModel(processModel("process-model"));

        mockMvc.perform(get("/v1/models/{modelId}", processModel.getId())).andExpect(status().isOk());
    }

    @Test
    public void should_returnStatusOkAndExtensions_when_gettingAnExistingModelWithExtensions() throws Exception {
        Map<String, Extensions> extensions = new HashMap<>();
        extensions.put(
            "process-model-with-extensions",
            extensions(
                "ServiceTask",
                "stringVariable",
                "integerVariable",
                "booleanVariable",
                "dateVariable",
                "jsonVariable"
            )
        );
        Model processModel = modelRepository.createModel(
            processModelWithExtensions("process-model-with-extensions", extensions)
        );
        mockMvc
            .perform(get("/v1/models/{modelId}", processModel.getId()))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath(
                    "$.extensions.process-model-with-extensions.properties",
                    allOf(
                        hasEntry(
                            equalTo("stringVariable"),
                            allOf(
                                hasEntry(equalTo("id"), equalTo("stringVariable")),
                                hasEntry(equalTo("name"), equalTo("stringVariable")),
                                hasEntry(equalTo("type"), equalTo("string")),
                                hasEntry(equalTo("value"), equalTo("stringVariable"))
                            )
                        ),
                        hasEntry(
                            equalTo("integerVariable"),
                            allOf(
                                hasEntry(equalTo("id"), equalTo("integerVariable")),
                                hasEntry(equalTo("name"), equalTo("integerVariable")),
                                hasEntry(equalTo("type"), equalTo("integer")),
                                hasEntry(equalTo("value"), isIntegerEquals(15))
                            )
                        ),
                        hasEntry(
                            equalTo("booleanVariable"),
                            allOf(
                                hasEntry(equalTo("id"), equalTo("booleanVariable")),
                                hasEntry(equalTo("name"), equalTo("booleanVariable")),
                                hasEntry(equalTo("type"), equalTo("boolean")),
                                hasEntry(equalTo("value"), isBooleanEquals(true))
                            )
                        ),
                        hasEntry(
                            equalTo("dateVariable"),
                            allOf(
                                hasEntry(equalTo("id"), equalTo("dateVariable")),
                                hasEntry(equalTo("name"), equalTo("dateVariable")),
                                hasEntry(equalTo("type"), equalTo("date")),
                                hasEntry(equalTo("value"), isDateEquals("1970-01-01T00:00:00.000+0000"))
                            )
                        ),
                        hasEntry(
                            equalTo("jsonVariable"),
                            allOf(
                                hasEntry(equalTo("id"), equalTo("jsonVariable")),
                                hasEntry(equalTo("name"), equalTo("jsonVariable")),
                                hasEntry(equalTo("type"), equalTo("json")),
                                hasEntry(equalTo("value"), isJson(withJsonPath("json-field-name")))
                            )
                        )
                    )
                )
            )
            .andExpect(
                jsonPath(
                    "$.extensions.process-model-with-extensions.mappings",
                    hasEntry(
                        equalTo("ServiceTask"),
                        allOf(
                            hasEntry(
                                equalTo("inputs"),
                                allOf(
                                    hasEntry(
                                        equalTo("stringVariable"),
                                        allOf(
                                            hasEntry(equalTo("type"), equalTo("value")),
                                            hasEntry(equalTo("value"), equalTo("stringVariable"))
                                        )
                                    ),
                                    hasEntry(
                                        equalTo("integerVariable"),
                                        allOf(
                                            hasEntry(equalTo("type"), equalTo("value")),
                                            hasEntry(equalTo("value"), equalTo("integerVariable"))
                                        )
                                    ),
                                    hasEntry(
                                        equalTo("booleanVariable"),
                                        allOf(
                                            hasEntry(equalTo("type"), equalTo("value")),
                                            hasEntry(equalTo("value"), equalTo("booleanVariable"))
                                        )
                                    ),
                                    hasEntry(
                                        equalTo("dateVariable"),
                                        allOf(
                                            hasEntry(equalTo("type"), equalTo("value")),
                                            hasEntry(equalTo("value"), equalTo("dateVariable"))
                                        )
                                    ),
                                    hasEntry(
                                        equalTo("jsonVariable"),
                                        allOf(
                                            hasEntry(equalTo("type"), equalTo("value")),
                                            hasEntry(equalTo("value"), equalTo("jsonVariable"))
                                        )
                                    )
                                )
                            ),
                            hasEntry(
                                equalTo("outputs"),
                                allOf(
                                    hasEntry(
                                        equalTo("stringVariable"),
                                        allOf(
                                            hasEntry(equalTo("type"), equalTo("value")),
                                            hasEntry(equalTo("value"), equalTo("stringVariable"))
                                        )
                                    ),
                                    hasEntry(
                                        equalTo("integerVariable"),
                                        allOf(
                                            hasEntry(equalTo("type"), equalTo("value")),
                                            hasEntry(equalTo("value"), equalTo("integerVariable"))
                                        )
                                    ),
                                    hasEntry(
                                        equalTo("booleanVariable"),
                                        allOf(
                                            hasEntry(equalTo("type"), equalTo("value")),
                                            hasEntry(equalTo("value"), equalTo("booleanVariable"))
                                        )
                                    ),
                                    hasEntry(
                                        equalTo("dateVariable"),
                                        allOf(
                                            hasEntry(equalTo("type"), equalTo("value")),
                                            hasEntry(equalTo("value"), equalTo("dateVariable"))
                                        )
                                    ),
                                    hasEntry(
                                        equalTo("jsonVariable"),
                                        allOf(
                                            hasEntry(equalTo("type"), equalTo("value")),
                                            hasEntry(equalTo("value"), equalTo("jsonVariable"))
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            );
    }

    @Test
    public void should_returnStatusOk_when_creatingProcessModelInProject() throws Exception {
        Project parentProject = projectRepository.createProject(project("parent-project"));

        mockMvc
            .perform(
                post("/v1/projects/{projectId}/models", parentProject.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(processModel("process-model")))
            )
            .andExpect(status().isCreated());
    }

    @Test
    public void should_returnStatusOk_when_updatingModel() throws Exception {
        Model processModel = modelRepository.createModel(processModel("process-model"));

        mockMvc
            .perform(
                put("/v1/models/{modelId}", processModel.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(processModel("new-process-model")))
            )
            .andExpect(status().isOk());

        Optional<Model> optionalModel = modelRepository.findModelById(processModel.getId());
        assertThat(optionalModel)
            .hasValueSatisfying(model -> assertThat(model.getName()).isEqualTo("new-process-model"));
    }

    @Test
    public void should_returnStatusOk_when_updatingModelWithExtensions() throws Exception {
        Map<String, Extensions> extensions = new HashMap<>();
        extensions.put("process-model-extensions", extensions("ServiceTask", "variable1"));
        ModelEntity processModel = processModelWithExtensions("process-model-extensions", extensions);
        modelRepository.createModel(processModel);

        Map<String, Extensions> secondExtensionMap = new HashMap<>();
        extensions.put("process-model-extensions", extensions("variable2", "variable3"));
        ModelEntity newModel = processModelWithExtensions("process-model-extensions", secondExtensionMap);
        mockMvc
            .perform(
                put("/v1/models/{modelId}", processModel.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(newModel))
            )
            .andExpect(status().isOk());
    }

    @Test
    public void should_returnStatusNoContent_when_deletingModel() throws Exception {
        Model processModel = modelRepository.createModel(processModel("process-model"));

        mockMvc.perform(delete("/v1/models/{modelId}", processModel.getId())).andExpect(status().isNoContent());

        assertThat(modelRepository.findModelById(processModel.getId())).isEmpty();
    }

    @Test
    public void should_returnExistingModelTypes_when_gettingModelTypes() throws Exception {
        mockMvc
            .perform(get("/v1/model-types"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.model-types", hasSize(2)))
            .andExpect(jsonPath("$._embedded.model-types[0].name", is(PROCESS)))
            .andExpect(jsonPath("$._embedded.model-types[1].name", is(ConnectorModelType.NAME)));
    }

    @Test
    public void should_returnStatusNoContent_when_validatingProcessModelWithValidContent() throws Exception {
        byte[] validContent = resourceAsByteArray("process/x-19022.bpmn20.xml");
        MockMultipartFile file = new MockMultipartFile("file", "process.xml", CONTENT_TYPE_XML, validContent);

        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        ModelEntity generatedProcess = processModel(project, "process-model");
        generatedProcess.setContent(validContent);

        Model processModel = modelRepository.createModel(generatedProcess);

        final ResultActions resultActions = mockMvc
            .perform(multipart("/v1/models/{model_id}/validate", processModel.getId()).file(file))
            .andExpect(status().isNoContent());
    }

    @Test
    public void should_throwBadRequestException_when_validatingProcessModelWithInvalidContent() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "diagram.bpm",
            CONTENT_TYPE_XML,
            "BPMN diagram".getBytes()
        );
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model processModel = modelRepository.createModel(processModel(project, "process-model"));

        mockMvc
            .perform(multipart("/v1/models/{model_id}/validate", processModel.getId()).file(file))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void should_returnStatusNoContent_when_validatingProcessExtensionsWithValidContent() throws Exception {
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model processModel = modelRepository.createModel(
            processModelWithExtensions(
                project,
                "Process_x",
                new Extensions(),
                resourceAsByteArray("process/x-19022.bpmn20.xml")
            )
        );
        MockMultipartFile file = multipartExtensionsFile(
            processModel,
            resourceAsByteArray("process-extensions/valid-extensions.json")
        );

        mockMvc
            .perform(multipart("/v1/models/{model_id}/validate/extensions", processModel.getId()).file(file))
            .andExpect(status().isNoContent());
    }

    @Test
    public void should_returnStatusNoContent_when_validatingProcessExtensionsWithValidContentAndNoValues()
        throws Exception {
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model processModel = modelRepository.createModel(
            processModelWithExtensions(
                project,
                "Process_x",
                new Extensions(),
                resourceAsByteArray("process/x-19022.bpmn20.xml")
            )
        );

        MockMultipartFile file = multipartExtensionsFile(
            processModel,
            resourceAsByteArray("process-extensions/valid-extensions-no-value.json")
        );

        mockMvc
            .perform(multipart("/v1/models/{model_id}/validate/extensions", processModel.getId()).file(file))
            .andExpect(status().isNoContent());
    }

    @Test
    public void should_returnStatusNoContent_when_validatingProcessExtensionsWithValidMappingType() throws Exception {
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model processModel = modelRepository.createModel(
            processModelWithExtensions(
                project,
                "Process_x",
                new Extensions(),
                resourceAsByteArray("process/x-19022.bpmn20.xml")
            )
        );

        MockMultipartFile file = multipartExtensionsFile(
            processModel,
            resourceAsByteArray("process-extensions/valid-mappingType-extensions.json")
        );

        mockMvc
            .perform(multipart("/v1/models/{model_id}/validate/extensions", processModel.getId()).file(file))
            .andExpect(status().isNoContent());
    }

    @Test
    public void should_throwBadRequestException_when_validatingProcessExtensionsWithInvalidMappingType()
        throws Exception {
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model processModel = modelRepository.createModel(
            processModelWithExtensions(
                project,
                "process-model",
                new Extensions(),
                resourceAsByteArray("process/x-19022.bpmn20.xml")
            )
        );
        MockMultipartFile file = multipartExtensionsFile(
            processModel,
            resourceAsByteArray("process-extensions/invalid-mappingType-extensions.json")
        );

        final ResultActions resultActions = mockMvc.perform(
            multipart("/v1/models/{model_id}/validate/extensions", processModel.getId()).file(file)
        );

        resultActions.andExpect(status().isBadRequest());
        assertThat(resultActions.andReturn().getResponse().getErrorMessage())
            .isEqualTo("Semantic model validation errors encountered: 2 schema violations found");

        final Exception resolvedException = resultActions.andReturn().getResolvedException();
        assertThat(resolvedException).isInstanceOf(SemanticModelValidationException.class);

        SemanticModelValidationException semanticModelValidationException = (SemanticModelValidationException) resolvedException;
        assertThat(semanticModelValidationException.getValidationErrors())
            .hasSize(2)
            .extracting(ModelValidationError::getProblem, ModelValidationError::getDescription)
            .containsOnly(
                tuple(
                    "WRONG_MAPPING_TYPE is not a valid enum value",
                    "#/extensions/Process_test/mappings/ServiceTask_06crg3b/mappingType: " +
                    "WRONG_MAPPING_TYPE is not a valid enum value"
                ),
                tuple(
                    "extraneous key [mappingTypo] is not permitted",
                    "#/extensions/Process_test/mappings/ServiceTask_07fergb: extraneous key [mappingTypo]" +
                    " is not permitted"
                )
            );
    }

    @Test
    public void should_throwBadRequestException_when_validatingProcessExtensionsWithInvalidMappingContent()
        throws Exception {
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model processModel = modelRepository.createModel(
            processModelWithExtensions(
                project,
                "process-model",
                new Extensions(),
                resourceAsByteArray("process/x-19022.bpmn20.xml")
            )
        );
        MockMultipartFile file = multipartExtensionsFile(
            processModel,
            resourceAsByteArray("process-extensions/invalid-mapping-extensions.json")
        );

        final ResultActions resultActions = mockMvc.perform(
            multipart("/v1/models/{model_id}/validate/extensions", processModel.getId()).file(file)
        );
        resultActions.andExpect(status().isBadRequest());
        assertThat(resultActions.andReturn().getResponse().getErrorMessage())
            .isEqualTo("Semantic model validation errors encountered: 2 schema violations found");

        final Exception resolvedException = resultActions.andReturn().getResolvedException();
        assertThat(resolvedException).isInstanceOf(SemanticModelValidationException.class);

        SemanticModelValidationException semanticModelValidationException = (SemanticModelValidationException) resolvedException;
        assertThat(semanticModelValidationException.getValidationErrors())
            .hasSize(2)
            .extracting(ModelValidationError::getProblem, ModelValidationError::getDescription)
            .containsOnly(
                tuple(
                    "extraneous key [inputds] is not permitted",
                    "#/extensions/Process_test/mappings/ServiceTask_06crg3b: extraneous key [inputds] is " +
                    "not permitted"
                ),
                tuple(
                    "extraneous key [outputss] is not permitted",
                    "#/extensions/Process_test/mappings/ServiceTask_06crg3b: extraneous key [outputss] is" +
                    " not permitted"
                )
            );
    }

    @Test
    public void should_thowBadRequestException_when_validatingProcessExtensionsWithInvalidStringVariableContent()
        throws Exception {
        byte[] invalidContent = resourceAsByteArray("process-extensions/invalid-string-variable-extensions.json");
        MockMultipartFile file = new MockMultipartFile("file", "extensions.json", CONTENT_TYPE_JSON, invalidContent);

        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model processModel = modelRepository.createModel(
            processModelWithExtensions(project, "process-model", new Extensions())
        );
        final ResultActions resultActions = mockMvc.perform(
            multipart("/v1/models/{model_id}/validate/extensions", processModel.getId()).file(file)
        );
        resultActions.andExpect(status().isBadRequest());

        final Exception resolvedException = resultActions.andReturn().getResolvedException();
        assertThat(resolvedException).isInstanceOf(SemanticModelValidationException.class);

        SemanticModelValidationException semanticModelValidationException = (SemanticModelValidationException) resolvedException;
        assertThat(semanticModelValidationException.getValidationErrors())
            .extracting(ModelValidationError::getProblem, ModelValidationError::getDescription)
            .containsExactly(
                tuple(
                    "expected type: String, found: Integer",
                    "Mismatch value type - stringVariable(c297ec88-0ecf-4841-9b0f-2ae814957c68). " +
                    "Expected type is string"
                )
            );
    }

    @Test
    public void should_thowBadRequestException_when_validatingProcessExtensionsWithInvalidIntegerVariableContent()
        throws Exception {
        byte[] invalidContent = resourceAsByteArray("process-extensions/invalid-integer-variable-extensions.json");
        MockMultipartFile file = new MockMultipartFile("file", "extensions.json", CONTENT_TYPE_JSON, invalidContent);

        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model processModel = modelRepository.createModel(
            processModelWithExtensions(project, "process-model", new Extensions())
        );
        final ResultActions resultActions = mockMvc.perform(
            multipart("/v1/models/{model_id}/validate/extensions", processModel.getId()).file(file)
        );
        resultActions.andExpect(status().isBadRequest());

        final Exception resolvedException = resultActions.andReturn().getResolvedException();
        assertThat(resolvedException).isInstanceOf(SemanticModelValidationException.class);

        SemanticModelValidationException semanticModelValidationException = (SemanticModelValidationException) resolvedException;
        assertThat(semanticModelValidationException.getValidationErrors())
            .extracting(ModelValidationError::getProblem, ModelValidationError::getDescription)
            .containsExactlyInAnyOrder(
                tuple(
                    "expected type: Integer, found: String",
                    "Mismatch value type - integerVariable" +
                    "(c297ec88-0ecf-4841-9b0f-2ae814957c68). Expected type is integer"
                ),
                tuple(
                    "string [aloha] does not match pattern ^\\$\\{(.*)[\\}]$",
                    "Value format in integerVariable(c297ec88-0ecf-4841-9b0f-2ae814957c68) " +
                    "is not a valid expression"
                )
            );
    }

    @Test
    public void should_thowBadRequestException_when_validatingProcessExtensionsWithInvalidBooleanVariableContent()
        throws Exception {
        byte[] invalidContent = resourceAsByteArray("process-extensions/invalid-boolean-variable-extensions.json");
        MockMultipartFile file = new MockMultipartFile("file", "extensions.json", CONTENT_TYPE_JSON, invalidContent);

        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model processModel = modelRepository.createModel(
            processModelWithExtensions(project, "process-model", new Extensions())
        );
        final ResultActions resultActions = mockMvc.perform(
            multipart("/v1/models/{model_id}/validate/extensions", processModel.getId()).file(file)
        );
        resultActions.andExpect(status().isBadRequest());

        final Exception resolvedException = resultActions.andReturn().getResolvedException();
        assertThat(resolvedException).isInstanceOf(SemanticModelValidationException.class);

        SemanticModelValidationException semanticModelValidationException = (SemanticModelValidationException) resolvedException;
        assertThat(semanticModelValidationException.getValidationErrors())
            .extracting(ModelValidationError::getProblem, ModelValidationError::getDescription)
            .containsExactly(
                tuple(
                    "expected type: Boolean, found: Integer",
                    "Mismatch value type - booleanVariable(c297ec88-0ecf-4841-9b0f-2ae814957c68). " +
                    "Expected type is boolean"
                ),
                tuple(
                    "expected type: String, found: Integer",
                    "Value format in booleanVariable(c297ec88-0ecf-4841-9b0f-2ae814957c68) is not a " +
                    "valid expression"
                )
            );
    }

    @Test
    public void should_returnSuccessful_when_validatingProcessExtensionsWithNonObjectVariableContent()
        throws Exception {
        byte[] invalidContent = resourceAsByteArray("process-extensions/invalid-object-variable-extensions.json");
        MockMultipartFile file = new MockMultipartFile("file", "extensions.json", CONTENT_TYPE_JSON, invalidContent);

        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model processModel = modelRepository.createModel(
            processModelWithExtensions(project, "process-model", new Extensions())
        );
        final ResultActions resultActions = mockMvc.perform(
            multipart("/v1/models/{model_id}/validate/extensions", processModel.getId()).file(file)
        );
        resultActions.andExpect(status().is2xxSuccessful());
    }

    @Test
    public void should_thowBadRequestException_when_validatingProcessExtensionsWithInvalidDateVariableContent()
        throws Exception {
        byte[] invalidContent = resourceAsByteArray("process-extensions/invalid-date-variable-extensions.json");
        MockMultipartFile file = new MockMultipartFile("file", "extensions.json", CONTENT_TYPE_JSON, invalidContent);

        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model processModel = modelRepository.createModel(
            processModelWithExtensions(project, "process-model", new Extensions())
        );
        final ResultActions resultActions = mockMvc.perform(
            multipart("/v1/models/{model_id}/validate/extensions", processModel.getId()).file(file)
        );
        resultActions.andExpect(status().isBadRequest());

        final Exception resolvedException = resultActions.andReturn().getResolvedException();
        assertThat(resolvedException).isInstanceOf(SemanticModelValidationException.class);

        SemanticModelValidationException semanticModelValidationException = (SemanticModelValidationException) resolvedException;
        assertThat(semanticModelValidationException.getValidationErrors())
            .extracting(ModelValidationError::getProblem, ModelValidationError::getDescription)
            .containsExactly(
                tuple(
                    "expected type: String, found: Integer",
                    "Mismatch value type - dateVariable(c297ec88-0ecf-4841-9b0f-2ae814957c68). Expected type is " +
                    "date"
                ),
                tuple(
                    "expected type: String, found: Integer",
                    "Value format in dateVariable(c297ec88-0ecf-4841-9b0f-2ae814957c68) is not a valid expression"
                ),
                tuple(
                    "string [aloha] does not match pattern ^[0-9]{4}-(((0[13578]|(10|12))-(0[1-9]|[1-2][0-9]|3[0-1]))" +
                    "|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)-(0[1-9]|[1-2][0-9]|30)))$",
                    "Invalid date - dateVariable(c297ec88-0ecf-4841-9b0f-2ae814957c68)"
                ),
                tuple(
                    "string [aloha] does not match pattern ^\\$\\{(.*)[\\}]$",
                    "Value format in dateVariable(c297ec88-0ecf-4841-9b0f-2ae814957c68) is not a valid expression"
                )
            );
    }

    @Test
    public void should_thowBadRequestException_when_validatingProcessExtensionsWithInvalidDateTimeVariableContent()
        throws Exception {
        byte[] invalidContent = resourceAsByteArray("process-extensions/invalid-datetime-variable-extensions.json");
        MockMultipartFile file = new MockMultipartFile("file", "extensions.json", CONTENT_TYPE_JSON, invalidContent);

        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model processModel = modelRepository.createModel(
            processModelWithExtensions(project, "process-model", new Extensions())
        );
        final ResultActions resultActions = mockMvc.perform(
            multipart("/v1/models/{model_id}/validate/extensions", processModel.getId()).file(file)
        );
        resultActions.andExpect(status().isBadRequest());

        final Exception resolvedException = resultActions.andReturn().getResolvedException();
        assertThat(resolvedException).isInstanceOf(SemanticModelValidationException.class);

        SemanticModelValidationException semanticModelValidationException = (SemanticModelValidationException) resolvedException;

        assertThat(semanticModelValidationException.getValidationErrors())
            .extracting(ModelValidationError::getProblem, ModelValidationError::getDescription)
            .containsExactlyInAnyOrder(
                tuple(
                    "expected type: String, found: Integer",
                    "Mismatch value type - case4(e0740a3a-fec4-4ee5-bece-61f39df2a47k). Expected type is datetime"
                ),
                tuple(
                    "string [2019-12-06T00:60:00] does not match pattern ^((19|20)[0-9][0-9])[-](0[1-9]|1[012])[-]" +
                    "(0[1-9]|[12][0-9]|3[01])[T]([01][0-9]|[2][0-3])[:]([0-5][0-9])[:]([0-5][0-9])([+|-]" +
                    "([01][0-9]|[2][0-3])[:]([0-5][0-9])){0,1}$",
                    "Invalid datetime - case4(e0740a3a-fec4-4ee5-bece-61f39df2a47g)"
                ),
                tuple(
                    "string [2019-12-06T00:00:60] does not match pattern ^((19|20)[0-9][0-9])[-](0[1-9]|1[012])[-]" +
                    "(0[1-9]|[12][0-9]|3[01])[T]([01][0-9]|[2][0-3])[:]([0-5][0-9])[:]([0-5][0-9])([+|-]" +
                    "([01][0-9]|[2][0-3])[:]([0-5][0-9])){0,1}$",
                    "Invalid datetime - case4(e0740a3a-fec4-4ee5-bece-61f39df2a47f)"
                ),
                tuple(
                    "string [2019-12-06T24:00:00] does not match pattern ^((19|20)[0-9][0-9])[-](0[1-9]|1[012])[-]" +
                    "(0[1-9]|[12][0-9]|3[01])[T]([01][0-9]|[2][0-3])[:]([0-5][0-9])[:]([0-5][0-9])([+|-]" +
                    "([01][0-9]|[2][0-3])[:]([0-5][0-9])){0,1}$",
                    "Invalid datetime - case4(e0740a3a-fec4-4ee5-bece-61f39df2a47e)"
                ),
                tuple(
                    "string [2019-12-06T00:60:00] does not match pattern ^\\$\\{(.*)[\\}]$",
                    "Value format in case4(e0740a3a-fec4-4ee5-bece-61f39df2a47g) is not a valid expression"
                ),
                tuple(
                    "string [2019-12-06T00:00:60] does not match pattern ^\\$\\{(.*)[\\}]$",
                    "Value format in case4(e0740a3a-fec4-4ee5-bece-61f39df2a47f) is not a valid expression"
                ),
                tuple(
                    "string [2019-12-06T24:00:00] does not match pattern ^\\$\\{(.*)[\\}]$",
                    "Value format in case4(e0740a3a-fec4-4ee5-bece-61f39df2a47e) is not a valid expression"
                ),
                tuple(
                    "expected type: String, found: Integer",
                    "Value format in case4(e0740a3a-fec4-4ee5-bece-61f39df2a47k) is not a valid expression"
                )
            );
    }

    @Test
    public void should_thowNotFoundException_when_validaingeModelThatNotExistsShouldThrowException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "diagram.bpm", "text/plain", "BPMN diagram".getBytes());
        mockMvc
            .perform(multipart("/v1/models/{model_id}/validate", "model_id").file(file))
            .andExpect(status().isNotFound());
    }

    @Test
    public void should_thowBadRequestException_when_validatingInvalidProcessModelUsingTextContentType()
        throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "diagram.bpmn20.xml",
            "text/plain",
            "BPMN diagram".getBytes()
        );

        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model processModel = modelRepository.createModel(processModel(project, "process-model"));

        mockMvc
            .perform(multipart("/v1/models/{model_id}/validate", processModel.getId()).file(file))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void should_returnStatusNoContent_when_validatingConnectorValidContent() throws Exception {
        byte[] validContent = resourceAsByteArray("connector/connector-simple.json");
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "connector-simple.json",
            CONTENT_TYPE_JSON,
            validContent
        );

        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model connectorModel = modelRepository.createModel(connectorModel(project, "connector-model"));

        mockMvc
            .perform(multipart("/v1/models/{model_id}/validate", connectorModel.getId()).file(file))
            .andExpect(status().isNoContent());
    }

    @Test
    public void should_returnStatusNoContent_when_validatingConnectorValidContentWithTemplate() throws Exception {
        byte[] validContent = resourceAsByteArray("connector/connector-template.json");
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "connector-template.json",
            CONTENT_TYPE_JSON,
            validContent
        );

        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model connectorModel = modelRepository.createModel(connectorModel(project, "connector-model"));

        mockMvc
            .perform(multipart("/v1/models/{model_id}/validate", connectorModel.getId()).file(file))
            .andExpect(status().isNoContent());
    }

    @Test
    public void should_returnProcessFile_when_exportingProcessModel() throws Exception {
        Model processModel = modelRepository.createModel(
            processModelWithContent("process_model_id", "Process Model Content")
        );
        MvcResult response = mockMvc
            .perform(get("/v1/models/{modelId}/export", processModel.getId()))
            .andExpect(status().isOk())
            .andReturn();

        assertThatResponseContent(response)
            .isFile()
            .hasName("process_model_id.bpmn20.xml")
            .hasContentType(CONTENT_TYPE_XML)
            .hasContent("Process Model Content");
    }

    @Test
    public void should_throwNotFoundException_when_exportingNotExistingModel() throws Exception {
        mockMvc.perform(get("/v1/models/not_existing_model/export")).andExpect(status().isNotFound()).andReturn();
    }

    @Test
    public void should_returnStatusCreated_when_importingProcessModel() throws Exception {
        Project parentProject = projectRepository.createProject(project("parent-project"));

        MockMultipartFile zipFile = new MockMultipartFile(
            "file",
            "x-19022.bpmn20.xml",
            "project/xml",
            resourceAsByteArray("process/x-19022.bpmn20.xml")
        );

        mockMvc
            .perform(
                multipart("/v1/projects/{projectId}/models/import", parentProject.getId())
                    .file(zipFile)
                    .param("type", PROCESS)
                    .accept(APPLICATION_JSON_VALUE)
            )
            .andExpect(status().isCreated());
    }

    @Test
    public void should_throwBadRequestException_when_importingModelWrongFileName() throws Exception {
        Project parentProject = project("parent-project");
        projectRepository.createProject(parentProject);

        MockMultipartFile zipFile = new MockMultipartFile(
            "file",
            "x-19022",
            "project/xml",
            resourceAsByteArray("process/x-19022.bpmn20.xml")
        );

        mockMvc
            .perform(
                multipart("/v1/projects/{projectId}/models/import", parentProject.getId())
                    .file(zipFile)
                    .param("type", PROCESS)
                    .accept(APPLICATION_JSON_VALUE)
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                status().reason(is("Unexpected extension was found for file to import model of type PROCESS: x-19022"))
            );
    }

    @Test
    public void should_throwBadRequestException_when_importingModelWrongModelType() throws Exception {
        Project parentProject = project("parent-project");
        projectRepository.createProject(parentProject);

        MockMultipartFile zipFile = new MockMultipartFile(
            "file",
            "x-19022.xml",
            "project/xml",
            resourceAsByteArray("process/x-19022.bpmn20.xml")
        );

        mockMvc
            .perform(
                multipart("/v1/projects/{projectId}/models/import", parentProject.getId())
                    .file(zipFile)
                    .param("type", "WRONG_TYPE")
            )
            .andExpect(status().isBadRequest())
            .andExpect(status().reason(is("Unknown model type: WRONG_TYPE")));
    }

    @Test
    public void should_throwNotFoundException_when_importingModelFromNotExistingProject() throws Exception {
        MockMultipartFile zipFile = new MockMultipartFile(
            "file",
            "x-19022.xml",
            "project/xml",
            resourceAsByteArray("process/x-19022.bpmn20.xml")
        );

        mockMvc
            .perform(multipart("/v1/projects/not_existing_project/models/import").file(zipFile).param("type", PROCESS))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldOnlyUpdateVersionOnceWhenCreatingProcess() throws Exception {
        Model processModel = modelRepository.createModel(processModel("Process Model 3"));

        mockMvc
            .perform(
                putMultipart("/v1/models/{modelId}/content", processModel.getId())
                    .file(
                        "file",
                        "create-process.xml",
                        CONTENT_TYPE_XML,
                        resourceAsByteArray("process/create-process.xml")
                    )
                    .param("type", PROCESS)
            )
            .andExpect(status().isNoContent());

        // //version should not get incremented here
        mockMvc
            .perform(get("/v1/models/{modelId}", processModel.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version", equalTo("0.0.1")));
    }

    @Test
    public void should_returnStatusOk_when_updatingConnectorTemplate() throws Exception {
        Model connectorModel = modelRepository.createModel(connectorModel("Connector With Template"));

        mockMvc
            .perform(
                putMultipart("/v1/models/{modelId}/content", connectorModel.getId())
                    .file(
                        "file",
                        "connector-template.json",
                        "application/json",
                        resourceAsByteArray("connector/connector-template.json")
                    )
            )
            .andExpect(status().isNoContent());

        mockMvc
            .perform(get("/v1/models/{modelId}", connectorModel.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.template", is("ConnectorTemplate")));
    }

    @Test
    public void should_returnStatusOk_when_updatingConnectorCustom() throws Exception {
        Model connectorModel = modelRepository.createModel(connectorModel("SimpleConnector"));

        mockMvc
            .perform(
                putMultipart("/v1/models/{modelId}/content", connectorModel.getId())
                    .file(
                        "file",
                        "connector-simple.json",
                        "application/json",
                        resourceAsByteArray("connector/connector-simple.json")
                    )
            )
            .andExpect(status().isNoContent());

        mockMvc
            .perform(get("/v1/models/{modelId}", connectorModel.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.template").doesNotExist());
    }

    @Test
    public void should_setProjectScopeByDefault_when_creatingModel() throws Exception {
        Project project = projectRepository.createProject(project("parent-project"));

        ModelEntity model = processModel("process-model");
        model.setScope(null);

        mockMvc
            .perform(
                post("/v1/projects/{projectId}/models", project.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(model))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.scope", is("PROJECT")));
    }

    @Test
    public void should_projectIdBeFilled_when_modelIsInProjectScope() throws Exception {
        Project parentProject = project("parent-project");
        projectRepository.createProject(parentProject);

        Model processModel = processModel("testProcess");
        processModel.addProject(parentProject);
        modelRepository.createModel(processModel);

        mockMvc
            .perform(get("/v1/models/{modelId}", processModel.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scope", is("PROJECT")))
            .andExpect(jsonPath("$.projectIds", hasSize(1)))
            .andExpect(jsonPath("$.projectIds", contains(parentProject.getId())));
    }

    @Test
    public void should_projectIdNotBeIncluded_when_modelIsNotInProjectScope() throws Exception {
        Model processModel = processModel("testProcess");
        processModel.setScope(ModelScope.GLOBAL);
        modelRepository.createModel(processModel);

        mockMvc
            .perform(get("/v1/models/{modelId}", processModel.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scope", is("GLOBAL")));
    }

    @Test
    public void should_retrieveAllProjects_when_modelAppearsInSeveralProjects() throws Exception {
        Project parentProjectOne = project("parent-project-one");
        projectRepository.createProject(parentProjectOne);
        Project parentProjectTwo = project("parent-project-two");
        projectRepository.createProject(parentProjectTwo);
        Model processModel = processModel("testProcess");
        processModel.setScope(ModelScope.GLOBAL);
        processModel.addProject(parentProjectOne);
        processModel.addProject(parentProjectTwo);
        modelRepository.createModel(processModel);

        String[] projectIds = { parentProjectOne.getId(), parentProjectTwo.getId() };

        mockMvc
            .perform(get("/v1/models/{modelId}", processModel.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scope", is("GLOBAL")))
            .andExpect(jsonPath("$.projectIds", hasSize(2)))
            .andExpect(
                jsonPath("$.projectIds", containsInAnyOrder(parentProjectOne.getId(), parentProjectTwo.getId()))
            );
    }

    @Test
    public void should_returnAllProjectModels_when_globalScopeModelsExists() throws Exception {
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("parent-project"));
        ProjectEntity anotherProject = (ProjectEntity) projectRepository.createProject(project("another-project"));

        modelRepository.createModel(processModel(project, "Process Model 1"));
        ModelEntity secondProcessModel = processModel(project, "Process Model 2");
        secondProcessModel.setScope(ModelScope.GLOBAL);
        secondProcessModel.addProject(anotherProject);
        modelRepository.createModel(secondProcessModel);

        mockMvc
            .perform(get("/v1/projects/{projectId}/models?type=PROCESS", project.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.models", hasSize(2)))
            .andExpect(jsonPath("$._embedded.models[0].name", is("Process Model 1")))
            .andExpect(jsonPath("$._embedded.models[0].scope", is("PROJECT")))
            .andExpect(jsonPath("$._embedded.models[1].name", is("Process Model 2")))
            .andExpect(jsonPath("$._embedded.models[1].scope", is("GLOBAL")));

        mockMvc
            .perform(get("/v1/projects/{projectId}/models?type=PROCESS", anotherProject.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.models", hasSize(1)))
            .andExpect(jsonPath("$._embedded.models[0].name", is("Process Model 2")))
            .andExpect(jsonPath("$._embedded.models[0].scope", is("GLOBAL")));
    }

    @Test
    public void should_checkModelNames_when_updatingModel() throws Exception {
        ProjectEntity projectOne = (ProjectEntity) projectRepository.createProject(project("project-1"));
        ProjectEntity projectTwo = (ProjectEntity) projectRepository.createProject(project("project-2"));

        ModelEntity modelOne = processModel(projectOne, "model-1");
        modelRepository.createModel(modelOne);
        ModelEntity modelTwo = processModel(projectOne, "model-2");
        modelTwo.setScope(ModelScope.GLOBAL);
        modelTwo.addProject(projectTwo);
        modelRepository.createModel(modelTwo);

        String subjectModelString = mapper.writeValueAsString(modelTwo);
        Model deserializedStringModel = mapper.readValue(subjectModelString, Model.class);
        deserializedStringModel.setName("model-1");
        deserializedStringModel.addProject(projectOne);
        deserializedStringModel.addProject(projectTwo);

        mockMvc
            .perform(
                put("/v1/models/{modelId}", modelTwo.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(deserializedStringModel))
            )
            .andExpect(status().isConflict());
    }

    @Test
    public void should_returnOnlyGlobalModels_when_retrievingAllModels2() throws Exception {
        createConnectorModel("connector-project-scoped");
        modelJpaRepository.flush();
    }

    @Test
    public void should_returnOnlyGlobalModels_when_retrievingAllModels() throws Exception {
        createConnectorModel("connector-project-scoped");
        createConnectorModel("connector-global-scoped");

        createProcessModel("process-project-scoped");
        createGlobalProcessModel("process-global-scoped-1");
        createGlobalProcessModel("process-global-scoped-2");

        mockMvc
            .perform(get("/v1/models?type=PROCESS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.models", hasSize(2)))
            .andExpect(jsonPath("$._embedded.models[0].name", is("process-global-scoped-1")))
            .andExpect(jsonPath("$._embedded.models[0].scope", is("GLOBAL")))
            .andExpect(jsonPath("$._embedded.models[1].name", is("process-global-scoped-2")))
            .andExpect(jsonPath("$._embedded.models[1].scope", is("GLOBAL")));
    }

    private void createProcessModel(String name) {
        ModelEntity projectScoped = processModel(name);
        modelRepository.createModel(projectScoped);
    }

    private void createConnectorModel(String name) {
        ModelEntity connectorProjectScoped = connectorModel(name);
        modelRepository.createModel(connectorProjectScoped);
    }

    @Test
    public void should_returnGlobalAndModels_when_retrievingAllModelsWithOrphansOption() throws Exception {
        createConnectorModel("connector-project-scoped");
        createConnectorModel("connector-global-scoped");

        createProcessModel("process-project-scoped");
        createGlobalProcessModel("process-global-scoped-1");
        createGlobalProcessModel("process-global-scoped-2");

        mockMvc
            .perform(get("/v1/models?type=PROCESS&includeOrphans=true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.models", hasSize(3)))
            .andExpect(jsonPath("$._embedded.models[0].name", is("process-project-scoped")))
            .andExpect(jsonPath("$._embedded.models[0].scope", is("PROJECT")))
            .andExpect(jsonPath("$._embedded.models[1].name", is("process-global-scoped-1")))
            .andExpect(jsonPath("$._embedded.models[1].scope", is("GLOBAL")))
            .andExpect(jsonPath("$._embedded.models[2].name", is("process-global-scoped-2")))
            .andExpect(jsonPath("$._embedded.models[2].scope", is("GLOBAL")));
    }

    private void createGlobalProcessModel(String name) {
        ModelEntity globalScopedOne = processModel(name);
        globalScopedOne.setScope(ModelScope.GLOBAL);
        modelRepository.createModel(globalScopedOne);
    }

    @Test
    public void should_addProjectModelRelationship_when_scopeIsProjectAndModelHasNoRelationships() throws Exception {
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project"));
        Model model = (ModelEntity) modelRepository.createModel(processModel("process-project-scoped"));

        mockMvc
            .perform(put("/v1/projects/{projectId}/models/{modelId}", project.getId(), model.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scope", is("PROJECT")))
            .andExpect(jsonPath("$.projectIds", hasSize(1)))
            .andExpect(jsonPath("$.projectIds", contains(project.getId())));
    }

    @Test
    public void should_returnStatusConflict_when_scopeIsProjectAndModelHasRelationships() throws Exception {
        ProjectEntity parentProject = (ProjectEntity) projectRepository.createProject(project("parent-project"));
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project"));
        Model model = (ModelEntity) modelRepository.createModel(processModel(parentProject, "process-project-scoped"));

        ResultActions resultActions = mockMvc
            .perform(put("/v1/projects/{projectId}/models/{modelId}", project.getId(), model.getId()))
            .andExpect(status().isConflict());

        assertThat(resultActions.andReturn().getResponse().getErrorMessage())
            .isEqualTo("A model at PROJECT scope can only be associated to one project");
    }

    @Test
    public void should_addProjectModelRelationship_when_scopeIsChangedToGlobalAndModelHasRelationships()
        throws Exception {
        ProjectEntity parentProject = (ProjectEntity) projectRepository.createProject(project("parent-project"));
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project"));
        Model model = (ModelEntity) modelRepository.createModel(processModel(parentProject, "process-project-scoped"));

        mockMvc
            .perform(put("/v1/projects/{projectId}/models/{modelId}?scope=GLOBAL", project.getId(), model.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scope", is("GLOBAL")))
            .andExpect(jsonPath("$.projectIds", hasSize(2)))
            .andExpect(jsonPath("$.projectIds", containsInAnyOrder(parentProject.getId(), project.getId())));
    }

    @Test
    public void should_returnConflict_when_scopeIsChangedToProjectAndModelHasRelationships() throws Exception {
        ProjectEntity parentProject = (ProjectEntity) projectRepository.createProject(project("parent-project"));
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project"));
        Model model = processModel(parentProject, "process-global-scoped");
        model.setScope(ModelScope.GLOBAL);
        model.addProject(project);
        model = (ModelEntity) modelRepository.createModel(model);

        ResultActions resultActions = mockMvc
            .perform(put("/v1/projects/{projectId}/models/{modelId}?scope=PROJECT", project.getId(), model.getId()))
            .andExpect(status().isConflict());

        assertThat(resultActions.andReturn().getResponse().getErrorMessage())
            .isEqualTo("A model at PROJECT scope can only be associated to one project");
    }

    @Test
    public void should_deleteOtherProjectRelationships_when_scopeIsChangedToProjectAndForcedOptionAndModelhasRelationships()
        throws Exception {
        ProjectEntity parentProject = (ProjectEntity) projectRepository.createProject(project("parent-project"));
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project"));
        Model model = processModel(parentProject, "process-global-scoped");
        model.setScope(ModelScope.GLOBAL);
        model.addProject(project);
        model = (ModelEntity) modelRepository.createModel(model);

        mockMvc
            .perform(
                put(
                    "/v1/projects/{projectId}/models/{modelId}?scope=PROJECT&force=true",
                    project.getId(),
                    model.getId()
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scope", is("PROJECT")))
            .andExpect(jsonPath("$.projectIds", hasSize(1)))
            .andExpect(jsonPath("$.projectIds", contains(project.getId())));
    }

    @Test
    public void should_deleteProjectRelationship_when_methodIsCalled() throws Exception {
        ProjectEntity parentProject = (ProjectEntity) projectRepository.createProject(project("parent-project"));
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project"));
        Model model = processModel(parentProject, "process-global-scoped");
        model.setScope(ModelScope.GLOBAL);
        model.addProject(project);
        model = (ModelEntity) modelRepository.createModel(model);

        mockMvc
            .perform(delete("/v1/projects/{projectId}/models/{modelId}", project.getId(), model.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.scope", is("GLOBAL")))
            .andExpect(jsonPath("$.projectIds", hasSize(1)))
            .andExpect(jsonPath("$.projectIds", contains(parentProject.getId())));
    }

    @Test
    public void should_retrieveModelContent_whenModelExists() throws Exception {
        Model processModel = modelRepository.createModel(
            processModelWithContent("process_model_id", "Process Model Content")
        );

        mockMvc
            .perform(get("/v1/models/{modelId}/content", processModel.getId()))
            .andExpect(status().isOk())
            .andExpect(result -> equalTo("Process Model Content"));
    }

    @Test
    public void should_returnStatusNotAcceptable_whenRetrievingModelDiagram() throws Exception {
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project-test"));
        Model processModel = modelRepository.createModel(
            processModelWithExtensions(
                project,
                "process-model",
                new Extensions(),
                resourceAsByteArray("process/RankMovie.bpmn20.xml")
            )
        );

        mockMvc
            .perform(get("/v1/models/{modelId}/content", processModel.getId()).header("Accept", "image/svg+xml"))
            .andExpect(status().isNotAcceptable());
    }

    @Test
    public void should_returnStatusCreatedAndProcessModelDetails_when_creatingProcessModelWithoutProject()
        throws Exception {
        mockMvc
            .perform(
                post("/v1/models")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(processModel("process-model")))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", equalTo("process-model")))
            .andExpect(jsonPath("$.type", equalTo(PROCESS)))
            .andExpect(jsonPath("$.extensions", notNullValue()));
    }

    @Test
    public void should_returnConflict_when_creatingNewRelationshipWithSameModelName() throws Exception {
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("project"));

        Model projectContentModel = processModel(project, "model-with-same-name");
        projectContentModel.setScope(ModelScope.PROJECT);
        projectContentModel = (ModelEntity) modelRepository.createModel(projectContentModel);

        Model globalContentModel = processModel("model-with-same-name");
        globalContentModel.setScope(ModelScope.GLOBAL);
        globalContentModel = (ModelEntity) modelRepository.createModel(globalContentModel);

        ResultActions resultActions = mockMvc
            .perform(put("/v1/projects/{projectId}/models/{modelId}", project.getId(), globalContentModel.getId()))
            .andExpect(status().isConflict());

        assertThat(resultActions.andReturn().getResponse().getErrorMessage())
            .isEqualTo(
                String.format(
                    "A model with the same type already exists within the project with id: %s",
                    project.getId()
                )
            );
    }

    @ParameterizedTest
    @MethodSource("projectModelsByNameArguments")
    public void should_returnProjectModels_when_gettingProjectModelsByName(
        String input,
        int expectedSize,
        String firstName,
        String secondName
    ) throws Exception {
        ProjectEntity project = (ProjectEntity) projectRepository.createProject(project("parent-project"));

        modelRepository.createModel(processModel(project, "Process Model 1"));
        modelRepository.createModel(processModel(project, "Process Model 2"));
        modelRepository.createModel(connectorModel(project, "Connector Model 1"));
        modelRepository.createModel(connectorModel(project, "Connector Model 2"));

        final ResultActions resultActions = mockMvc
            .perform(
                get("/v1/projects/{projectId}/models/findByName?name={input}", project.getId(), input)
                    .header("accept", "application/json")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.list.entries", hasSize(expectedSize)));
        if (expectedSize > 0) {
            resultActions
                .andExpect(jsonPath("$.list.entries[0].entry.name", is(firstName)))
                .andExpect(jsonPath("$.list.entries[1].entry.name", is(secondName)));
        }
    }

    private static Stream<Arguments> projectModelsByNameArguments() {
        return Stream.of(
            Arguments.of("p", 2, "Process Model 1", "Process Model 2"),
            Arguments.of("process", 2, "Process Model 1", "Process Model 2"),
            Arguments.of("PROCESS", 2, "Process Model 1", "Process Model 2"),
            Arguments.of("Process Model", 2, "Process Model 1", "Process Model 2"),
            Arguments.of("Connector", 2, "Connector Model 1", "Connector Model 2"),
            Arguments.of("1", 2, "Process Model 1", "Connector Model 1"),
            Arguments.of("wrong input", 0, null, null),
            Arguments.of("", 0, null, null),
            Arguments.of(null, 0, null, null)
        );
    }

    @Test
    public void should_returnSanitizedProcessModelExtensions_when_creatingProcessModelWithSvgExtensions()
        throws Exception {
        final String svg =
            "data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI" +
            "yNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0Ij48ZyBpZD0ic2F2ZS0yNHB4XzFfIiBkYXRhLW5hbWU9InNhdmUtMjR" +
            "weCAoMSkiIG9wYWNpdHk9IjEiPjxwYXRoIGlkPSJQYXRoXzE3OTY1IiBkYXRhLW5hbWU9IlBhdGggMTc5NjUiIGQ9Ik0wLDBIMjR" +
            "WMjRIMFoiIGZpbGw9Im5vbmUiLz48cGF0aCBpZD0iUGF0aF8xNzk2NiIgZGF0YS1uYW1lPSJQYXRoIDE3OTY2IiBkPSJNMTcsM0g" +
            "1QTIsMiwwLDAsMCwzLDVWMTlhMiwyLDAsMCwwLDIsMkgxOWEyLjAwNiwyLjAwNiwwLDAsMCwyLTJWN1ptMiwxNkg1VjVIMTYuMTd" +
            "MMTksNy44M1ptLTctN2EzLDMsMCwxLDAsMywzQTMsMywwLDAsMCwxMiwxMlpNNiw2aDl2NEg2WiIvPjwvZz48L3N2Zz4K";

        Project project = projectRepository.createProject(project("parent-project"));

        ProcessVariable processVariable = MockFactory.processVariable("image");
        processVariable.setValue(svg);
        Map<String, ProcessVariable> processVariables = Map.of("image", processVariable);
        Extensions extensions = new Extensions();
        extensions.setProcessVariables(processVariables);
        Map<String, Extensions> processExtension = new HashMap<>();
        processExtension.put("process-model-extensions", extensions);
        ModelEntity processModel = processModelWithExtensions("process-model-extensions", processExtension);

        ResultActions resultActions = mockMvc.perform(
            post("/v1/projects/{projectId}/models", project.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(processModel))
        );
        resultActions
            .andExpect(status().isCreated())
            .andExpect(
                jsonPath(
                    "$.extensions.process-model-extensions.properties.image.value",
                    startsWith("data:image/png;base64,")
                )
            );
    }
}
