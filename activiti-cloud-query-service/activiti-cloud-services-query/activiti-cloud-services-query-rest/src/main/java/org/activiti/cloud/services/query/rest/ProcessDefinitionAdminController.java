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
package org.activiti.cloud.services.query.rest;

import static org.activiti.cloud.services.query.rest.RestDocConstants.PREDICATE_DESC;
import static org.activiti.cloud.services.query.rest.RestDocConstants.PREDICATE_EXAMPLE;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Optional;
import org.activiti.cloud.alfresco.data.domain.AlfrescoPagedModelAssembler;
import org.activiti.cloud.api.process.model.CloudProcessDefinition;
import org.activiti.cloud.services.query.app.repository.ProcessDefinitionRepository;
import org.activiti.cloud.services.query.model.ProcessDefinitionEntity;
import org.activiti.cloud.services.query.rest.assembler.ProcessDefinitionRepresentationModelAssembler;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ExposesResourceFor(ProcessDefinitionEntity.class)
@RequestMapping(
    value = "/admin/v1/process-definitions",
    produces = { MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE }
)
public class ProcessDefinitionAdminController {

    private ProcessDefinitionRepository repository;

    private AlfrescoPagedModelAssembler<ProcessDefinitionEntity> pagedCollectionModelAssembler;

    private ProcessDefinitionRepresentationModelAssembler processDefinitionRepresentationModelAssembler;

    public ProcessDefinitionAdminController(
        ProcessDefinitionRepository repository,
        AlfrescoPagedModelAssembler<ProcessDefinitionEntity> pagedCollectionModelAssembler,
        ProcessDefinitionRepresentationModelAssembler processDefinitionRepresentationModelAssembler
    ) {
        this.repository = repository;
        this.pagedCollectionModelAssembler = pagedCollectionModelAssembler;
        this.processDefinitionRepresentationModelAssembler = processDefinitionRepresentationModelAssembler;
    }

    @GetMapping
    public PagedModel<EntityModel<CloudProcessDefinition>> findAllProcessAdmin(
        @Parameter(description = PREDICATE_DESC, example = PREDICATE_EXAMPLE) @QuerydslPredicate(
            root = ProcessDefinitionEntity.class
        ) Predicate predicate,
        Pageable pageable
    ) {
        predicate = Optional.ofNullable(predicate).orElseGet(BooleanBuilder::new);

        return pagedCollectionModelAssembler.toModel(
            pageable,
            repository.findAll(predicate, pageable),
            processDefinitionRepresentationModelAssembler
        );
    }
}
