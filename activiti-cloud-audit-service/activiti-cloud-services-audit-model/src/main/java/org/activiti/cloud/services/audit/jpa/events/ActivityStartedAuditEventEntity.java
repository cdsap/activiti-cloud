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
import org.activiti.cloud.api.process.model.events.CloudBPMNActivityStartedEvent;
import org.hibernate.annotations.DynamicInsert;

@Entity(name = ActivityStartedAuditEventEntity.ACTIVITY_STARTED_EVENT)
@DiscriminatorValue(value = ActivityStartedAuditEventEntity.ACTIVITY_STARTED_EVENT)
@DynamicInsert
public class ActivityStartedAuditEventEntity extends BPMNActivityAuditEventEntity {

    protected static final String ACTIVITY_STARTED_EVENT = "ActivityStartedEvent";

    public ActivityStartedAuditEventEntity() {}

    public ActivityStartedAuditEventEntity(CloudBPMNActivityStartedEvent cloudEvent) {
        super(cloudEvent);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ActivityStartedAuditEventEntity [toString()=").append(super.toString()).append("]");
        return builder.toString();
    }
}
