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

import static graphql.schema.GraphQLScalarType.newScalar;

import com.introproventures.graphql.jpa.query.autoconfigure.EnableGraphQLJpaQuerySchema;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLJPASchemaBuilderCustomizer;
import com.introproventures.graphql.jpa.query.autoconfigure.GraphQLSchemaBuilderAutoConfiguration;
import com.introproventures.graphql.jpa.query.schema.JavaScalars;
import graphql.GraphQL;
import java.util.Date;
import org.activiti.cloud.services.query.model.ProcessInstanceEntity;
import org.activiti.cloud.services.query.model.VariableValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto configuration of Activiti GraphQL Query Service components
 */
@AutoConfiguration(before = GraphQLSchemaBuilderAutoConfiguration.class)
@ConditionalOnClass({ GraphQL.class, ProcessInstanceEntity.class })
@ConditionalOnProperty(
    name = "spring.activiti.cloud.services.notifications.graphql.jpa-query.enabled",
    matchIfMissing = true
)
@EnableGraphQLJpaQuerySchema(basePackageClasses = ProcessInstanceEntity.class)
public class ActivitiGraphQLSchemaAutoConfiguration {

    @Bean
    GraphQLJPASchemaBuilderCustomizer graphQLJPASchemaBuilderCustomizer(
        @Value("${activiti.cloud.graphql.jpa-query.date-format:yyyy-MM-dd'T'HH:mm:ss.SSSX}") String dateFormatString
    ) {
        return builder ->
            builder
                .name("Query")
                .description("Activiti Cloud Query Schema")
                .scalar(
                    VariableValue.class,
                    newScalar()
                        .name("VariableValue")
                        .description("VariableValue type")
                        .coercing(new JavaScalars.GraphQLObjectCoercing())
                        .build()
                )
                .scalar(
                    Date.class,
                    newScalar()
                        .name("Date")
                        .description("Date type with '" + dateFormatString + "' format")
                        .coercing(new JavaScalars.GraphQLDateCoercing(dateFormatString))
                        .build()
                );
    }
}
