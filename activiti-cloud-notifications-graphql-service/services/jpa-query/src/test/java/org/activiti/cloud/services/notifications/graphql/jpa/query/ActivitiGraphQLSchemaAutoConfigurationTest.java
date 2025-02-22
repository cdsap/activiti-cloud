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
package org.activiti.cloud.services.notifications.graphql.jpa.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.introproventures.graphql.jpa.query.schema.JavaScalars;
import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.Coercing;
import graphql.schema.GraphQLSchema;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(properties = "spring.data.jpa.repositories.bootstrap-mode=default")
@TestPropertySource("classpath:application-test.properties")
class ActivitiGraphQLSchemaAutoConfigurationTest {

    @Autowired(required = false)
    private GraphQLSchema schema;

    @SpringBootApplication
    static class TestApplication {}

    @Test
    void contextLoads() {
        assertThat(schema).isNotNull();
    }

    @Test
    void correctlyDerivesSchemaFromGivenEntities() {
        //when

        // then
        assertThat(schema).describedAs("Ensure the result is returned").isNotNull();

        //then
        assertThat(schema.getQueryType().getFieldDefinition("Task").getArgument("id"))
            .describedAs("Ensure that identity can be queried on")
            .isNotNull();

        //then
        assertThat(schema.getQueryType().getFieldDefinition("Task").getArguments())
            .describedAs("Ensure query has correct number of arguments")
            .hasSize(1);

        //then
        assertThat(schema.getQueryType().getFieldDefinition("ProcessInstance").getArgument("id").getType())
            .isEqualTo(Scalars.GraphQLString);

        //then
        assertThat(schema.getQueryType().getFieldDefinition("ProcessInstance").getArguments())
            .describedAs("Ensure query has correct number of arguments")
            .hasSize(1);

        //then
        assertThat(schema.getQueryType().getFieldDefinition("ProcessVariable").getArgument("id").getType())
            .isEqualTo(ExtendedScalars.GraphQLLong);

        //then
        assertThat(schema.getQueryType().getFieldDefinition("ProcessVariable").getArguments())
            .describedAs("Ensure query has correct number of arguments")
            .hasSize(1);
    }

    @Test
    void correctlyDerivesPageableSchemaFromGivenEntities() {
        //when

        // then
        assertThat(schema).describedAs("Ensure the result is returned").isNotNull();

        assertThat(schema.getQueryType().getFieldDefinition("ProcessInstances").getArgument("where"))
            .describedAs("Ensure that collections can be queried")
            .isNotNull();

        assertThat(schema.getQueryType().getFieldDefinition("ProcessInstances").getArgument("page"))
            .describedAs("Ensure that collections can be paged")
            .isNotNull();

        assertThat(schema.getQueryType().getFieldDefinition("Tasks").getArgument("page"))
            .describedAs("Ensure that collections can be queried on by page")
            .isNotNull();

        assertThat(schema.getQueryType().getFieldDefinition("Tasks").getArgument("page"))
            .describedAs("Ensure that collections can be queried on by page")
            .isNotNull();

        assertThat(schema.getQueryType().getFieldDefinition("Tasks").getArguments())
            .describedAs("Ensure query has correct number of arguments")
            .hasSize(2);

        assertThat(schema.getQueryType().getFieldDefinition("ProcessVariables").getArgument("page"))
            .describedAs("Ensure that collections can be queried on by page")
            .isNotNull();

        assertThat(schema.getQueryType().getFieldDefinition("ProcessVariables").getArguments())
            .describedAs("Ensure query has correct number of arguments")
            .hasSize(2);

        assertThat(schema.getQueryType().getFieldDefinition("TaskVariables").getArgument("page"))
            .describedAs("Ensure that collections can be queried on by page")
            .isNotNull();

        assertThat(schema.getQueryType().getFieldDefinition("TaskVariables").getArguments())
            .describedAs("Ensure query has correct number of arguments")
            .hasSize(2);
    }

    @Test
    void correctlyCoercesDateToISO8601FormatWithTimeAndZoneOffset() {
        TimeZone timeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        try {
            // given
            Coercing<?, ?> subject = JavaScalars.of(Date.class).getCoercing();

            // when
            Object result = subject.parseValue(Date.from(Instant.EPOCH));

            // then
            assertThat(result).isEqualTo("1970-01-01T00:00:00.000Z");
        } finally {
            TimeZone.setDefault(timeZone);
        }
    }
}
