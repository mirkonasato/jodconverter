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

package org.jodconverter.office;

/**
 * This class holds the configuration of an {@link SimpleOfficeManagerPoolEntry} when no office
 * instance are required to execute conversion.
 *
 * @see SimpleOfficeManagerPoolEntry
 * @see OfficeManagerPoolEntryConfig
 */
class SimpleOfficeManagerPoolEntryConfig implements OfficeManagerPoolEntryConfig {

  private long taskExecutionTimeout = DEFAULT_TASK_EXECUTION_TIMEOUT;

  @Override
  public long getTaskExecutionTimeout() {
    return taskExecutionTimeout;
  }

  @Override
  public void setTaskExecutionTimeout(final long taskExecutionTimeout) {
    this.taskExecutionTimeout = taskExecutionTimeout;
  }
}
