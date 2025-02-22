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
package org.activiti.cloud.modeling.api.process;

import java.util.Objects;

public class TaskTemplateDefinition {

    private TemplateMapping assignee;

    private TemplateMapping candidate;

    public TemplateMapping getAssignee() {
        return assignee;
    }

    public void setAssignee(TemplateMapping assignee) {
        this.assignee = assignee;
    }

    public TemplateMapping getCandidate() {
        return candidate;
    }

    public void setCandidate(TemplateMapping candidate) {
        this.candidate = candidate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskTemplateDefinition that = (TaskTemplateDefinition) o;
        return Objects.equals(assignee, that.assignee) && Objects.equals(candidate, that.candidate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignee, candidate);
    }
}
