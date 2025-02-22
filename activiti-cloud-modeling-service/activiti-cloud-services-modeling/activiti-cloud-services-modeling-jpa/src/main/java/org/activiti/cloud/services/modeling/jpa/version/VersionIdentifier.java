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
package org.activiti.cloud.services.modeling.jpa.version;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Embeddable version identifier
 */
@Embeddable
public class VersionIdentifier implements Serializable {

    private String version;

    private String versionedEntityId;

    public VersionIdentifier() {}

    public VersionIdentifier(String versionedEntityId, String version) {
        this.versionedEntityId = versionedEntityId;
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionedEntityId() {
        return versionedEntityId;
    }

    public void setVersionedEntityId(String versionedEntityId) {
        this.versionedEntityId = versionedEntityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VersionIdentifier)) {
            return false;
        }

        VersionIdentifier that = (VersionIdentifier) o;
        return (
            Objects.equals(getVersionedEntityId(), that.getVersionedEntityId()) &&
            Objects.equals(getVersion(), that.getVersion())
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVersionedEntityId(), getVersion());
    }

    @Override
    public String toString() {
        return getVersion();
    }
}
