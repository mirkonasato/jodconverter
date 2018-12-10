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

package org.jodconverter.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.jodconverter.AbstractOfficeITest;
import org.jodconverter.LocalConverter;
import org.jodconverter.filter.text.PageCounterFilter;
import org.jodconverter.filter.text.PageSelectorFilter;

public class PageCounterFilterITest extends AbstractOfficeITest {

  private static final String SOURCE_FILENAME = "test_multi_page.doc";
  private static final File SOURCE_FILE = new File(DOCUMENTS_DIR, SOURCE_FILENAME);

  @ClassRule public static TemporaryFolder testFolder = new TemporaryFolder();

  /**
   * Test the conversion of a document, choosing a specific page.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void doFilter_SelectPage2BetweenCounter_ShouldCount3Then1() throws Exception {

    final File targetFile = new File(testFolder.getRoot(), SOURCE_FILENAME + ".page2.txt");

    final PageCounterFilter countFilter1 = new PageCounterFilter();
    final PageSelectorFilter selectorFilter = new PageSelectorFilter(2);
    final PageCounterFilter countFilter2 = new PageCounterFilter();

    // Test the filter
    LocalConverter.builder()
        .filterChain(countFilter1, selectorFilter, countFilter2)
        .build()
        .convert(SOURCE_FILE)
        .to(targetFile)
        .execute();

    final String content = FileUtils.readFileToString(targetFile, Charset.forName("UTF-8"));
    assertThat(content)
        .contains("Test document Page 2")
        .doesNotContain("Test document Page 1")
        .doesNotContain("Test document Page 3");
    assertThat(countFilter1.getPageCount()).isEqualTo(3);
    assertThat(countFilter2.getPageCount()).isEqualTo(1);
  }
}
