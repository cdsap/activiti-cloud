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
package org.activiti.cloud.services.modeling.mock;

import static org.springframework.http.HttpMethod.PUT;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

/**
 * Builder for multipart MockMvc requests.
 * It can be used for making PUT with multipart calls.
 */
public class MockMultipartRequestBuilder {

    private MockMultipartHttpServletRequestBuilder multipartRequestBuilder;

    MockMultipartRequestBuilder(MockMultipartHttpServletRequestBuilder multipartRequestBuilder) {
        this.multipartRequestBuilder = multipartRequestBuilder;
    }

    public static MockMultipartRequestBuilder putMultipart(String urlTemplate, Object... uriVars) {
        return multipart(urlTemplate, uriVars).put();
    }

    public static MockMultipartRequestBuilder multipart(String urlTemplate, Object... uriVars) {
        return new MockMultipartRequestBuilder(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart(urlTemplate, uriVars)
        );
    }

    public MockMultipartRequestBuilder put() {
        multipartRequestBuilder.with(request -> {
            request.setMethod(PUT.name());
            return request;
        });
        return this;
    }

    public MockMultipartHttpServletRequestBuilder file(MockMultipartFile file) {
        multipartRequestBuilder.file(file);
        return multipartRequestBuilder;
    }

    public MockMultipartHttpServletRequestBuilder file(
        String controlName,
        String fileName,
        String contentType,
        byte[] bytes
    ) {
        return file(new MockMultipartFile(controlName, fileName, contentType, bytes));
    }
}
