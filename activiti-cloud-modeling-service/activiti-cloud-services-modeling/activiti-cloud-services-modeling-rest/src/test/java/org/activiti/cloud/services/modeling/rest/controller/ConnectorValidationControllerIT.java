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

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.activiti.cloud.services.common.util.ContentTypeUtils.CONTENT_TYPE_JSON;
import static org.activiti.cloud.services.common.util.FileUtils.resourceAsByteArray;
import static org.activiti.cloud.services.modeling.asserts.AssertResponse.assertThatResponse;
import static org.activiti.cloud.services.modeling.mock.MockFactory.connectorModel;
import static org.activiti.cloud.services.modeling.validation.DNSNameValidator.DNS_LABEL_REGEX;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.cloud.modeling.api.Model;
import org.activiti.cloud.modeling.repository.ModelRepository;
import org.activiti.cloud.services.modeling.config.ModelingRestApplication;
import org.activiti.cloud.services.modeling.security.WithMockModelerUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration tests for connector models validation rest api
 */
@ActiveProfiles("test")
@SpringBootTest(classes = ModelingRestApplication.class)
@Transactional
@WebAppConfiguration
@WithMockModelerUser
public class ConnectorValidationControllerIT {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ModelRepository modelRepository;

    private Model connectorModel;

    @MockBean
    private SecurityManager securityManager;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        when(securityManager.getAuthenticatedUserId()).thenReturn("modeler");
        connectorModel = modelRepository.createModel(connectorModel("connector-name"));
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    public void cleanUp() {
        modelRepository.deleteModel(connectorModel);
    }

    @Test
    public void should_returnStatusNoContent_when_validatingSimpleConnector() throws IOException {
        given()
            .mockMvc(mockMvc)
            .log()
            .everything(true)
            .multiPart(
                "file",
                "simple-connector.json",
                resourceAsByteArray("connector/connector-simple.json"),
                "application/json"
            )
            .contentType("multipart/form-data")
            .post(String.format("/v1/models/%s/validate", connectorModel.getId()))
            .then()
            .expect(status().isNoContent())
            .body(is(emptyString()));
    }

    @Test
    public void should_returnStatusNoContent_when_validatingConnectorTextContentType() throws IOException {
        given()
            .mockMvc(mockMvc)
            .log()
            .everything(true)
            .multiPart(
                "file",
                "simple-connector.json",
                resourceAsByteArray("connector/connector-simple.json"),
                "text/plain"
            )
            .contentType("multipart/form-data")
            .post(String.format("/v1/models/%s/validate", connectorModel.getId()))
            .then()
            .expect(status().isNoContent())
            .body(is(emptyString()));
    }

    @Test
    public void should_returnStatusNoContent_when_validatingConnectorWithEvents() throws IOException {
        given()
            .mockMvc(mockMvc)
            .log()
            .everything(true)
            .multiPart(
                "file",
                "connector-with-events.json",
                resourceAsByteArray("connector/connector-with-events.json"),
                "text/plain"
            )
            .contentType("multipart/form-data")
            .post(String.format("/v1/models/%s/validate", connectorModel.getId()))
            .then()
            .expect(status().isNoContent())
            .body(is(emptyString()));
    }

    @Test
    public void should_throwSemanticValidationException_when_validatingInvalidSimpleConnector() throws IOException {
        assertThatResponse(
            given()
                .mockMvc(mockMvc)
                .log()
                .everything(true)
                .multiPart(
                    "file",
                    "invalid-simple-connector.json",
                    resourceAsByteArray("connector/invalid-simple-connector.json"),
                    "application/json"
                )
                .contentType("multipart/form-data")
                .post(String.format("/v1/models/%s/validate", connectorModel.getId()))
                .then()
                .expect(status().isBadRequest())
        )
            .isSemanticValidationException()
            .hasValidationErrors(
                "extraneous key [icon] is not permitted",
                "extraneous key [output] is not permitted",
                "extraneous key [input] is not permitted",
                "required key [id] not found",
                "required key [name] not found"
            );
    }

    @Test
    public void should_throwSyntacticValidationException_when_validatingJsonInvalidConnector() throws IOException {
        assertThatResponse(
            given()
                .mockMvc(mockMvc)
                .log()
                .everything(true)
                .multiPart(
                    "file",
                    "invalid-json-connector.json",
                    resourceAsByteArray("connector/invalid-json-connector.json"),
                    "application/json"
                )
                .contentType("multipart/form-data")
                .post(String.format("/v1/models/%s/validate", connectorModel.getId()))
                .then()
                .expect(status().isBadRequest())
        )
            .isSyntacticValidationException()
            .hasValidationErrors(
                "org.json.JSONException: A JSONObject text must begin with '{' at 1 [character 2 line 1]"
            );
    }

    @Test
    public void should_throwSyntacticValidationException_when_validatingInvalidConnectorTextContentType()
        throws IOException {
        assertThatResponse(
            given()
                .mockMvc(mockMvc)
                .log()
                .everything(true)
                .multiPart(
                    "file",
                    "invalid-json-connector.json",
                    resourceAsByteArray("connector/invalid-json-connector.json"),
                    "text/plain"
                )
                .contentType("multipart/form-data")
                .post(String.format("/v1/models/%s/validate", connectorModel.getId()))
                .then()
                .expect(status().isBadRequest())
        )
            .isSyntacticValidationException()
            .hasValidationErrors(
                "org.json.JSONException: A JSONObject text must begin with '{' at 1 [character 2 line 1]"
            );
    }

    @Test
    public void should_throwSemanticValidationException_when_validatingInvalidConnectorNameTooLong()
        throws IOException {
        assertThatResponse(
            given()
                .mockMvc(mockMvc)
                .log()
                .everything(true)
                .multiPart(
                    "file",
                    "invalid-connector-name-too-long.json",
                    resourceAsByteArray("connector/invalid-connector-name-too-long.json"),
                    "text/plain"
                )
                .contentType("multipart/form-data")
                .post(String.format("/v1/models/%s/validate", connectorModel.getId()))
                .then()
                .expect(status().isBadRequest())
        )
            .isSemanticValidationException()
            .hasValidationErrors(
                "expected maxLength: 26, actual: 27",
                "string [123456789_123456789_1234567] does not match pattern " + DNS_LABEL_REGEX
            );
    }

    @Test
    public void should_throwSemanticValidationException_when_validatingInvalidConnectorNameEmpty() throws IOException {
        assertThatResponse(
            given()
                .mockMvc(mockMvc)
                .log()
                .everything(true)
                .multiPart(
                    "file",
                    "invalid-connector-name-empty.json",
                    resourceAsByteArray("connector/invalid-connector-name-empty.json"),
                    "text/plain"
                )
                .contentType("multipart/form-data")
                .post(String.format("/v1/models/%s/validate", connectorModel.getId()))
                .then()
                .expect(status().isBadRequest())
        )
            .isSemanticValidationException()
            .hasValidationErrors(
                "expected minLength: 1, actual: 0",
                "string [] does not match pattern " + DNS_LABEL_REGEX
            );
    }

    @Test
    public void should_throwSemanticValidationException_when_validatingInvalidConnectorNameWithUnderscore()
        throws IOException {
        assertThatResponse(
            given()
                .mockMvc(mockMvc)
                .log()
                .everything(true)
                .multiPart(
                    "file",
                    "invalid-connector-name-with-underscore.json",
                    resourceAsByteArray("connector/invalid-connector-name-with-underscore.json"),
                    "text/plain"
                )
                .contentType("multipart/form-data")
                .post(String.format("/v1/models/%s/validate", connectorModel.getId()))
                .then()
                .expect(status().isBadRequest())
        )
            .isSemanticValidationException()
            .hasValidationErrors("string [name_with_underscore] does not match pattern " + DNS_LABEL_REGEX);
    }

    @Test
    public void should_throwSemanticValidationException_when_validatingInvalidConnectorNameWithUppercase()
        throws IOException {
        assertThatResponse(
            given()
                .mockMvc(mockMvc)
                .log()
                .everything(true)
                .multiPart(
                    "file",
                    "invalid-connector-name-with-uppercase.json",
                    resourceAsByteArray("connector/invalid-connector-name-with-uppercase.json"),
                    CONTENT_TYPE_JSON
                )
                .contentType("multipart/form-data")
                .post(String.format("/v1/models/%s/validate", connectorModel.getId()))
                .then()
                .expect(status().isBadRequest())
        )
            .isSemanticValidationException()
            .hasValidationErrors("string [NameWithUppercase] does not match pattern " + DNS_LABEL_REGEX);
    }

    public void should_returnStatusNoContent_when_validatingConnectorWithCustomTypesInEventsAndActions()
        throws IOException {
        given()
            .mockMvc(mockMvc)
            .log()
            .everything(true)
            .multiPart(
                "file",
                "connector-with-custom-type.json",
                resourceAsByteArray("connector/connector-with-custom-type.json"),
                "text/plain"
            )
            .contentType("multipart/form-data")
            .post(String.format("/v1/models/%s/validate", connectorModel.getId()))
            .then()
            .expect(status().isNoContent())
            .body(is(emptyString()));
    }

    @Test
    public void should_returnStatusNoContent_when_validatingConnectorWithErrors() throws IOException {
        given()
            .mockMvc(mockMvc)
            .log()
            .everything(true)
            .multiPart(
                "file",
                "connector-with-errors.json",
                resourceAsByteArray("connector/connector-with-errors.json"),
                "text/plain"
            )
            .contentType("multipart/form-data")
            .post(String.format("/v1/models/%s/validate", connectorModel.getId()))
            .then()
            .expect(status().isNoContent())
            .body(is(emptyString()));
    }

    @Test
    public void should_throwSemanticValidationException_when_validatingInvalidConnectorErrorInvalidProperty()
        throws IOException {
        assertThatResponse(
            given()
                .mockMvc(mockMvc)
                .log()
                .everything(true)
                .multiPart(
                    "file",
                    "connector-with-errors-invalid-property.json",
                    resourceAsByteArray("connector/connector-with-errors-invalid-property.json"),
                    CONTENT_TYPE_JSON
                )
                .contentType("multipart/form-data")
                .post(String.format("/v1/models/%s/validate", connectorModel.getId()))
                .then()
                .expect(status().isBadRequest())
        )
            .isSemanticValidationException()
            .hasValidationErrors("extraneous key [invalid] is not permitted");
    }

    @Test
    public void should_returnStatusNoContent_when_validatingConnectorEventWithModel() throws IOException {
        given()
            .mockMvc(mockMvc)
            .log()
            .everything(true)
            .multiPart(
                "file",
                "connector-with-event-model.json",
                resourceAsByteArray("connector/connector-with-event-model.json"),
                "text/plain"
            )
            .contentType("multipart/form-data")
            .post(String.format("/v1/models/%s/validate", connectorModel.getId()))
            .then()
            .expect(status().isNoContent())
            .body(is(emptyString()));
    }
}
