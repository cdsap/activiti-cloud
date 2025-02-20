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
package org.activiti.cloud.connectors.starter.channels;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.support.ErrorMessage;

public class IntegrationRequestErrorChannelListener {

    public static final String ERROR_CHANNEL = "errorChannel";

    private final IntegrationErrorHandler integrationErrorHandler;

    public IntegrationRequestErrorChannelListener(IntegrationErrorHandler integrationErrorSender) {
        this.integrationErrorHandler = integrationErrorSender;
    }

    @ServiceActivator(inputChannel = ERROR_CHANNEL)
    public void accept(ErrorMessage errorMessage) {
        integrationErrorHandler.handleErrorMessage(errorMessage);
    }
}
