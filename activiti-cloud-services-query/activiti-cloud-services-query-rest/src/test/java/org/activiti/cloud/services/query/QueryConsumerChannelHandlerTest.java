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

package org.activiti.cloud.services.query;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import org.activiti.cloud.services.query.app.QueryConsumerChannelHandler;
import org.activiti.cloud.services.query.events.AbstractProcessEngineEvent;
import org.activiti.cloud.services.query.events.ProcessCancelledEvent;
import org.activiti.cloud.services.query.events.handlers.QueryEventHandlerContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class QueryConsumerChannelHandlerTest {

    @InjectMocks
    private QueryConsumerChannelHandler consumer;

    @Mock
    private QueryEventHandlerContext eventHandlerContext;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void receiveShouldHandleReceivedEvent() throws Exception {
        //given
        AbstractProcessEngineEvent event = mock(AbstractProcessEngineEvent.class);

        //when
        AbstractProcessEngineEvent[] events = {event};
        consumer.receive(events);

        //then
        verify(eventHandlerContext).handle(events);
    }


    @Test
    public void testHandleProcessCancelledEvent() throws Exception {
        //given
        AbstractProcessEngineEvent event = mock(ProcessCancelledEvent.class);

        //when
        AbstractProcessEngineEvent[] events = {event};
        consumer.receive(events);

        //then
        verify(eventHandlerContext).handle(events);
    }
}