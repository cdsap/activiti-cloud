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
package org.activiti.cloud.services.modeling.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.activiti.cloud.modeling.api.config.ModelingApiAutoConfiguration;
import org.activiti.cloud.services.modeling.validation.magicnumber.FileContentValidatorConfiguration;
import org.activiti.cloud.services.modeling.validation.magicnumber.FileMagicNumberValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = { FileContentValidatorConfiguration.class, ModelingApiAutoConfiguration.class })
class FileMagicNumberValidatorTest {

    @Autowired
    private FileMagicNumberValidator fileContentValidator;

    @Test
    void checkFileIsNotExecutableIfSizeIsLessMinMagicNumberSize() {
        byte[] firstBytes = new byte[] { 22 };
        assertThat(fileContentValidator.checkFileIsExecutable(firstBytes)).isFalse();
    }

    @Test
    void checkFileIsExecutableJavaClass() throws IOException {
        //use a fake extension to verify if the test works regardless of extension
        File file = new File("src/test/resources/executables/javaClass");
        byte[] firstBytes = readBytes(file);
        assertThat(fileContentValidator.checkFileIsExecutable(firstBytes)).isTrue();
    }

    @Test
    void checkFileIsExecutableExe() throws IOException {
        //use a fake extension to verify if the test works regardless of extension
        File file = new File("src/test/resources/executables/windows-file.windows");
        byte[] firstBytes = readBytes(file);
        assertThat(fileContentValidator.checkFileIsExecutable(firstBytes)).isTrue();
    }

    @Test
    void checkFileIsExecutableMsi() throws IOException {
        File file = new File("src/test/resources/executables/Chrome.msi");
        byte[] firstBytes = readBytes(file);
        assertThat(fileContentValidator.checkFileIsExecutable(firstBytes)).isTrue();
    }

    @Test
    void checkFileIsExecutableBash() throws IOException {
        File file = new File("src/test/resources/executables/test.sh");
        byte[] firstBytes = readBytes(file);
        assertThat(fileContentValidator.checkFileIsExecutable(firstBytes)).isTrue();
    }

    @Test
    void checkFileIsExecutableLinux() throws IOException {
        File file = new File("src/test/resources/executables/linuxExFile");
        byte[] firstBytes = readBytes(file);
        assertThat(fileContentValidator.checkFileIsExecutable(firstBytes)).isTrue();
    }

    @Test
    void checkFileIsNotExecutablePlainText() throws IOException {
        File file = new File("src/test/resources/executables/text.txt");
        byte[] firstBytes = readBytes(file);
        assertThat(fileContentValidator.checkFileIsExecutable(firstBytes)).isFalse();
    }

    @Test
    void checkFileIsNotExecutablePngImage() throws IOException {
        File file = new File("src/test/resources/executables/image.png");
        byte[] firstBytes = readBytes(file);
        assertThat(fileContentValidator.checkFileIsExecutable(firstBytes)).isFalse();
    }

    @Test
    void checkFileIsNotExecutableZipFile() throws IOException {
        //use a fake extension to verify if the test works regardless of extension
        File file = new File("src/test/resources/executables/testzip.isAZip");
        byte[] firstBytes = readBytes(file);
        assertThat(fileContentValidator.checkFileIsExecutable(firstBytes)).isFalse();
    }

    private byte[] readBytes(File file) throws IOException {
        return new FileInputStream(file).readAllBytes();
    }
}
