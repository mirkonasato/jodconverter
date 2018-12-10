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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jodconverter.document.DocumentFormat.Builder;

/**
 * A JsonDocumentFormatRegistry contains a collection of {@code DocumentFormat} supported by office
 * that has been loaded loaded from a JSON source.
 */
public class JsonDocumentFormatRegistry extends SimpleDocumentFormatRegistry {

  /**
   * Creates a JsonDocumentFormatRegistry from the given InputStream.
   *
   * @param source The InputStream (JSON format) containing the DocumentFormat collection.
   * @return The created JsonDocumentFormatRegistry.
   * @throws IOException If an I/O error occurs.
   */
  public static JsonDocumentFormatRegistry create(final InputStream source) throws IOException {

    return create(source, null);
  }

  /**
   * Creates a JsonDocumentFormatRegistry from the given InputStream.
   *
   * @param source The InputStream (JSON format) containing the DocumentFormat collection.
   * @param customProperties Custom properties applied when loading or storing documents.
   * @return The created JsonDocumentFormatRegistry.
   * @throws IOException If an I/O error occurs.
   */
  public static JsonDocumentFormatRegistry create(
      final InputStream source, final Map<String, DocumentFormatProperties> customProperties)
      throws IOException {

    return create(IOUtils.toString(source, "UTF-8"), customProperties);
  }

  /**
   * Creates a JsonDocumentFormatRegistry from the given source.
   *
   * @param source The string (JSON format) containing the DocumentFormat collection.
   * @return The created JsonDocumentFormatRegistry.
   */
  public static JsonDocumentFormatRegistry create(final String source) {

    return create(source, null);
  }

  /**
   * Creates a JsonDocumentFormatRegistry from the given source.
   *
   * @param source The string (JSON format) containing the DocumentFormat collection.
   * @param customProperties Custom properties applied when loading or storing documents.
   * @return The created JsonDocumentFormatRegistry.
   */
  public static JsonDocumentFormatRegistry create(
      final String source, final Map<String, DocumentFormatProperties> customProperties) {

    final JsonDocumentFormatRegistry registry = new JsonDocumentFormatRegistry();
    registry.readJsonArray(source, customProperties);
    return registry;
  }

  // Force static function call
  protected JsonDocumentFormatRegistry() { // NOSONAR
    super();
  }

  // Fill the registry from the given JSON source
  protected void readJsonArray(
      final String source, final Map<String, DocumentFormatProperties> customProperties) {

    final Gson gson = new Gson();

    // Deserialization
    final Type collectionType = new TypeToken<Collection<DocumentFormat>>() {}.getType();
    final Collection<DocumentFormat> formats = gson.fromJson(source, collectionType);

    // Fill the registry with loaded formats. Note that we have to use
    // the constructor in order top create read only formats.
    formats
        .stream()
        .map(
            fmt -> {
              if (customProperties == null || !customProperties.containsKey(fmt.getExtension())) {
                return DocumentFormat.unmodifiableCopy(fmt);
              }
              final DocumentFormatProperties props = customProperties.get(fmt.getExtension());
              final Builder builder = DocumentFormat.builder().from(fmt).unmodifiable(true);
              // Add custom load/store properties.
              props.getLoad().forEach(builder::loadProperty);
              props
                  .getStore()
                  .forEach(
                      (family, storeProps) -> {
                        storeProps.forEach(
                            (name, value) -> {
                              builder.storeProperty(family, name, value);
                            });
                      });
              // Build the format.
              return builder.build();
            })
        .forEach(this::addFormat);
  }
}
