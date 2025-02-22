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
package org.activiti.cloud.services.modeling.jpa.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.activiti.api.runtime.shared.security.SecurityManager;
import org.activiti.cloud.services.modeling.jpa.config.ModelingJpaApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;

@SpringBootTest(classes = ModelingJpaApplication.class)
public class AuditorAwareIT {

    @Autowired
    private AuditorAware<String> auditorAware;

    @Autowired
    private DateTimeProvider localDateTimeProvider;

    @MockBean
    private SecurityManager securityManager;

    @MockBean
    private BuildProperties buildProperties;

    @Test
    public void testCurrentAuditor() {
        // GIVEN
        when(securityManager.getAuthenticatedUserId()).thenReturn("test_user");

        // WHEN
        assertThat(auditorAware.getCurrentAuditor())
            .hasValueSatisfying(currentUser ->
                // THEN
                assertThat(currentUser).isEqualTo("test_user")
            );
    }

    @Test
    public void testLocalDateTimeProvider() {
        // WHEN
        assertThat(localDateTimeProvider.getNow())
            .hasValueSatisfying(now -> {
                // THEN
                assertThat(LocalDateTime.from(now).getNano()).isLessThanOrEqualTo(999_999_000);
            });
    }
}
