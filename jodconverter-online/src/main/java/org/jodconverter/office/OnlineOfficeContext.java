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

import org.apache.http.client.HttpClient;

/** Represents an office context for online conversions. */
public interface OnlineOfficeContext extends OfficeContext {

  /**
   * Gets the HTTP client responsible for request execution to the office server.
   *
   * @return The client that will send the conversion request.
   */
  HttpClient getHttpClient();

  /**
   * Gets the request configuration.
   *
   * @return The request configuration.
   */
  RequestConfig getRequestConfig();
}
