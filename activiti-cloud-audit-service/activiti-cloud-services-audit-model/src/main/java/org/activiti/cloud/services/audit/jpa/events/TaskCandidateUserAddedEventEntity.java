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
import org.activiti.api.task.model.TaskCandidateUser;
import org.activiti.api.task.model.impl.TaskCandidateUserImpl;
import org.activiti.cloud.api.task.model.events.CloudTaskCandidateUserAddedEvent;
import org.activiti.cloud.services.audit.jpa.converters.json.TaskCandidateUserJpaJsonConverter;
import org.hibernate.annotations.DynamicInsert;

@Entity(name = TaskCandidateUserAddedEventEntity.TASK_CANDIDATE_USER_ADDED_EVENT)
@DiscriminatorValue(value = TaskCandidateUserAddedEventEntity.TASK_CANDIDATE_USER_ADDED_EVENT)
@DynamicInsert
public class TaskCandidateUserAddedEventEntity extends AuditEventEntity {

    protected static final String TASK_CANDIDATE_USER_ADDED_EVENT = "TaskCandidateUserAddedEvent";

    @Convert(converter = TaskCandidateUserJpaJsonConverter.class)
    @Column(columnDefinition = "text")
    private TaskCandidateUserImpl candidateUser;

    public TaskCandidateUserAddedEventEntity() {}

    public TaskCandidateUserAddedEventEntity(CloudTaskCandidateUserAddedEvent cloudEvent) {
        super(cloudEvent);
        setCandidateUser(cloudEvent.getEntity());
    }

    public TaskCandidateUser getCandidateUser() {
        return candidateUser;
    }

    public void setCandidateUser(TaskCandidateUser candidateUser) {
        this.candidateUser = new TaskCandidateUserImpl(candidateUser.getUserId(), candidateUser.getTaskId());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder
            .append("TaskCandidateUserAddedEventEntity [candidateUser=")
            .append(candidateUser)
            .append(", toString()=")
            .append(super.toString())
            .append("]");
        return builder.toString();
    }
}
