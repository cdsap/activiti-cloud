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
package org.activiti.cloud.services.audit.jpa.events;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.activiti.api.process.model.ProcessCandidateStarterUser;
import org.activiti.api.runtime.model.impl.ProcessCandidateStarterUserImpl;
import org.activiti.cloud.api.process.model.events.CloudProcessCandidateStarterUserAddedEvent;
import org.activiti.cloud.services.audit.jpa.converters.json.ProcessCandidateStarterUserJpaJsonConverter;
import org.hibernate.annotations.DynamicInsert;

@Entity(name = ProcessCandidateStarterUserAddedEventEntity.PROCESS_CANDIDATE_STARTER_USER_ADDED_EVENT)
@DiscriminatorValue(value = ProcessCandidateStarterUserAddedEventEntity.PROCESS_CANDIDATE_STARTER_USER_ADDED_EVENT)
@DynamicInsert
public class ProcessCandidateStarterUserAddedEventEntity extends AuditEventEntity {

    protected static final String PROCESS_CANDIDATE_STARTER_USER_ADDED_EVENT = "ProcessCandidateStarterUserAddedEvent";

    @Convert(converter = ProcessCandidateStarterUserJpaJsonConverter.class)
    @Column(columnDefinition = "text")
    private ProcessCandidateStarterUserImpl candidateStarterUser;

    public ProcessCandidateStarterUserAddedEventEntity() {}

    public ProcessCandidateStarterUserAddedEventEntity(CloudProcessCandidateStarterUserAddedEvent cloudEvent) {
        super(cloudEvent);
        setCandidateStarterUser(cloudEvent.getEntity());
    }

    public ProcessCandidateStarterUser getCandidateStarterUser() {
        return candidateStarterUser;
    }

    public void setCandidateStarterUser(ProcessCandidateStarterUser candidateUser) {
        this.candidateStarterUser =
            new ProcessCandidateStarterUserImpl(candidateUser.getProcessDefinitionId(), candidateUser.getUserId());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder
            .append("ProcessCandidateStarterUserAddedEventEntity [candidateStarterUser=")
            .append(candidateStarterUser)
            .append(", toString()=")
            .append(super.toString())
            .append("]");
        return builder.toString();
    }
}
