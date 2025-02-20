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
package org.activiti.cloud.services.modeling.asserts;

import static org.assertj.core.api.Assertions.*;

import org.activiti.cloud.modeling.api.ModelValidationError;
import org.activiti.cloud.modeling.core.error.SemanticModelValidationException;
import org.activiti.cloud.modeling.core.error.SyntacticModelValidationException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Asserts for validation exceptions
 */
public class AssertValidationException {

    private Exception validationException;

    public AssertValidationException(MethodArgumentNotValidException validationException) {
        this.validationException = validationException;
    }

    public AssertValidationException(SemanticModelValidationException validationException) {
        this.validationException = validationException;
    }

    public AssertValidationException(SyntacticModelValidationException validationException) {
        this.validationException = validationException;
    }

    public AssertValidationException hasValidationErrorCodes(String... errorCodes) {
        assertThat(validationException).isInstanceOf(MethodArgumentNotValidException.class);
        assertThat(((MethodArgumentNotValidException) validationException).getBindingResult().getAllErrors())
            .extracting(ObjectError::getCode)
            .contains(errorCodes);
        return this;
    }

    public AssertValidationException hasValidationErrors(String... errors) {
        if (validationException instanceof MethodArgumentNotValidException) {
            assertThat(((MethodArgumentNotValidException) validationException).getBindingResult().getAllErrors())
                .extracting(ObjectError::getCodes)
                .contains(errors);
        } else if (validationException instanceof SemanticModelValidationException) {
            assertThat(((SemanticModelValidationException) validationException).getValidationErrors())
                .hasSize(errors.length)
                .flatExtracting(ModelValidationError::getProblem)
                .contains(errors);
        } else if (validationException instanceof SyntacticModelValidationException) {
            assertThat(errors.length).isEqualTo(1);
            assertThat(validationException.getMessage()).isEqualTo(errors[0]);
        } else {
            fail("Unknown exception class: " + validationException.getClass());
        }
        return this;
    }

    public AssertValidationException hasValidationErrorMessages(String... errorMessages) {
        if (validationException instanceof MethodArgumentNotValidException) {
            assertThat(((MethodArgumentNotValidException) validationException).getBindingResult().getAllErrors())
                .extracting(ObjectError::getDefaultMessage)
                .contains(errorMessages);
        } else if (validationException instanceof SemanticModelValidationException) {
            assertThat(((SemanticModelValidationException) validationException).getValidationErrors())
                .hasSize(errorMessages.length)
                .flatExtracting(ModelValidationError::getDescription)
                .contains(errorMessages);
        } else if (validationException instanceof SyntacticModelValidationException) {
            assertThat(errorMessages.length).isEqualTo(1);
            assertThat(validationException.getMessage()).isEqualTo(errorMessages[0]);
        } else {
            fail("Unknown exception class: " + validationException.getClass());
        }
        return this;
    }
}
