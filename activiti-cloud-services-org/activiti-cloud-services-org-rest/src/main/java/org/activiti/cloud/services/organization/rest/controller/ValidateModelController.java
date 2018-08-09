/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.services.organization.rest.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.activiti.cloud.organization.api.Model;
import org.activiti.cloud.organization.core.model.ValidationErrorRepresentation;
import org.activiti.cloud.organization.core.rest.client.ModelService;
import org.activiti.cloud.organization.repository.ModelRepository;
import org.activiti.cloud.services.organization.rest.assembler.ValidationErrorResourceAssembler;
import org.activiti.cloud.services.organization.rest.resource.ValidationErrorResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.activiti.cloud.services.organization.rest.config.RepositoryRestConfig.API_VERSION;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * Controller for validation the content of a {@link Model}
 */
@RestController
@RequestMapping(value = API_VERSION)
public class ValidateModelController {

    private final ModelRepository modelRepository;

    private final ValidationErrorResourceAssembler validationErrorResourceAssembler;

    private final ModelService modelService;

    @Autowired
    public ValidateModelController(ModelRepository modelRepository,
                                   ModelService modelService,
                                   ValidationErrorResourceAssembler validationErrorResourceAssembler) {
        this.modelRepository = modelRepository;
        this.modelService = modelService;
        this.validationErrorResourceAssembler = validationErrorResourceAssembler;
    }

    @RequestMapping(value = "/models/{modelId}/validate", method = RequestMethod.POST, produces = MediaTypes.HAL_JSON_VALUE)
    public Resources<ValidationErrorResource> validateModel(@PathVariable(value = "modelId") String modelId,
                                                            @RequestParam("file") MultipartFile file) throws IOException {

        byte[] fileContent = file.getBytes();
        Optional<Model> optionalModel = modelRepository.findModelById(modelId);
        List<ValidationErrorRepresentation> validationResult = optionalModel
                .map(model -> modelService.validateResourceContent(model.getType(),
                                                                   fileContent))
                .orElseThrow(ResourceNotFoundException::new);

        return new Resources<>(validationErrorResourceAssembler.toResources(validationResult),
                               linkTo(ValidateModelController.class).withSelfRel());
    }
}
