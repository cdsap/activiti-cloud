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
package org.activiti.cloud.services.modeling.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.activiti.cloud.modeling.api.ModelValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DNSNameValidatorTest {

    private DNSNameValidator dnsNameValidator;

    @BeforeEach
    public void setup() {
        dnsNameValidator = new DNSNameValidator() {};
    }

    @Test
    public void should_returnEmptyStream_when_theInputIsValid() {
        Stream<ModelValidationError> errors = dnsNameValidator.validateDNSName("this-is-a-test", "myType");
        assertThat(errors).isEmpty();
    }

    @Test
    public void should_returnRegexMismatchError_when_itContainsUppercaseLetters() {
        Stream<ModelValidationError> errors = dnsNameValidator.validateDNSName("Invalid-name", "myType");
        assertThat(errors)
            .flatExtracting(ModelValidationError::getErrorCode, ModelValidationError::getDescription)
            .containsOnly(
                "regex.mismatch",
                "The myType name should follow DNS-1035 conventions: it must consist of lower case alphanumeric characters or '-', and must start and end with an alphanumeric character: 'Invalid-name'"
            );
    }

    @Test
    public void should_returnRegexMismatchError_when_itContainsUppercaseSpecialChars() {
        Stream<ModelValidationError> errors = dnsNameValidator.validateDNSName("invalid!name", "myType");
        assertThat(errors)
            .flatExtracting(ModelValidationError::getErrorCode, ModelValidationError::getDescription)
            .containsOnly(
                "regex.mismatch",
                "The myType name should follow DNS-1035 conventions: it must consist of lower case alphanumeric characters or '-', and must start and end with an alphanumeric character: 'invalid!name'"
            );
    }

    @Test
    public void should_returnRegexMismatchError_when_itContainsUppercase() {
        Stream<ModelValidationError> errors = dnsNameValidator.validateDNSName("invalid name", "myType");
        assertThat(errors)
            .flatExtracting(ModelValidationError::getErrorCode, ModelValidationError::getDescription)
            .containsOnly(
                "regex.mismatch",
                "The myType name should follow DNS-1035 conventions: it must consist of lower case alphanumeric characters or '-', and must start and end with an alphanumeric character: 'invalid name'"
            );
    }

    @Test
    public void should_returnFieldRequiredError_when_itIsNull() {
        Stream<ModelValidationError> errors = dnsNameValidator.validateDNSName(null, "myType");
        assertThat(errors)
            .flatExtracting(ModelValidationError::getErrorCode, ModelValidationError::getDescription)
            .containsOnly("field.required", "The myType name is required");
    }

    @Test
    public void should_returnFieldEmptyError_when_itIsAnEmptyString() {
        Stream<ModelValidationError> errors = dnsNameValidator.validateDNSName("", "myType");
        assertThat(errors)
            .flatExtracting(ModelValidationError::getErrorCode, ModelValidationError::getDescription)
            .containsOnly(
                "field.empty",
                "The myType name cannot be empty",
                "regex.mismatch",
                "The myType name should follow DNS-1035 conventions: it must consist of lower case alphanumeric characters or '-', and must start and end with an alphanumeric character: ''"
            );
    }

    @Test
    public void should_returnFieldEmptyError_when_itContainsOnlyBlankSpaces() {
        Stream<ModelValidationError> errors = dnsNameValidator.validateDNSName("   ", "myType");
        assertThat(errors)
            .flatExtracting(ModelValidationError::getErrorCode, ModelValidationError::getDescription)
            .containsOnly(
                "field.empty",
                "The myType name cannot be empty",
                "regex.mismatch",
                "The myType name should follow DNS-1035 conventions: it must consist of lower case alphanumeric characters or '-', and must start and end with an alphanumeric character: '   '"
            );
    }

    @Test
    public void should_returnLengthGreaterError_when_textIsTooLong() {
        Stream<ModelValidationError> errors = dnsNameValidator.validateDNSName("abc-123-def-456-ghi-789-jkl", "myType");
        assertThat(errors)
            .flatExtracting(ModelValidationError::getErrorCode, ModelValidationError::getDescription)
            .containsOnly(
                "length.greater",
                "The myType name length cannot be greater than 26: 'abc-123-def-456-ghi-789-jkl'",
                "regex.mismatch",
                "The myType name should follow DNS-1035 conventions: it must consist of lower case alphanumeric characters or '-', and must start and end with an alphanumeric character: 'abc-123-def-456-ghi-789-jkl'"
            );
    }
}
