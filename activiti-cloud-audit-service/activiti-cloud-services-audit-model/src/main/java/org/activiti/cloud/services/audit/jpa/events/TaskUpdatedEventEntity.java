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

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.activiti.cloud.api.task.model.events.CloudTaskUpdatedEvent;
import org.hibernate.annotations.DynamicInsert;

@Entity(name = TaskUpdatedEventEntity.TASK_UPDATED_EVENT)
@DiscriminatorValue(value = TaskUpdatedEventEntity.TASK_UPDATED_EVENT)
@DynamicInsert
public class TaskUpdatedEventEntity extends TaskAuditEventEntity {

    protected static final String TASK_UPDATED_EVENT = "TaskUpdatedEvent";

    public TaskUpdatedEventEntity() {}

    public TaskUpdatedEventEntity(CloudTaskUpdatedEvent cloudEvent) {
        super(cloudEvent);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TaskUpdatedEventEntity [toString()=").append(super.toString()).append("]");
        return builder.toString();
    }
}
