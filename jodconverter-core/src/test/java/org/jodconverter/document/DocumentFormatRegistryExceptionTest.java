/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jodconverter.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JsonDocumentFormatRegistry.class)
public class DocumentFormatRegistryExceptionTest {

  @Test
  public void create_IoExceptionThrownWhileLoading_ThrowDocumentFormatRegistryException()
      throws Exception {

    mockStatic(JsonDocumentFormatRegistry.class);
    when(JsonDocumentFormatRegistry.create(isA(InputStream.class))).thenThrow(IOException.class);
    try {
      DefaultDocumentFormatRegistry.getInstance();
      fail("ExceptionInInitializerError should be thrown");
    } catch (Throwable err) {
      assertThat(err).isExactlyInstanceOf(ExceptionInInitializerError.class);
      assertThat(((ExceptionInInitializerError) err).getException())
          .isExactlyInstanceOf(DocumentFormatRegistryException.class)
          .hasCauseExactlyInstanceOf(IOException.class);
    }
  }
}
