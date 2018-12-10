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

package org.jodconverter.sample.springboot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.jodconverter.DocumentConverter;
import org.jodconverter.LocalConverter;
import org.jodconverter.document.DefaultDocumentFormatRegistry;
import org.jodconverter.document.DocumentFormat;
import org.jodconverter.office.OfficeException;
import org.jodconverter.office.OfficeManager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller that will process conversion requests. The mapping is the same as LibreOffice Online
 * (/lool/convert-to) so we can use the jodconverter-online module to send request to this
 * controller. This controller does the same as LibreOffice Online, and also support custom
 * conversions through filters and custom load/store properties.
 */
@Controller
@RequestMapping("/lool/convert-to")
@Api("Conversion Operations which emulate a LibreOffice Online server conversion capabilities.")
public class ConverterController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConverterController.class);

  private static final String FILTER_DATA = "FilterData";
  private static final String FILTER_DATA_PREFIX_PARAM = "fd";
  private static final String LOAD_PROPERTIES_PREFIX_PARAM = "l";
  private static final String LOAD_FILTER_DATA_PREFIX_PARAM =
      LOAD_PROPERTIES_PREFIX_PARAM + FILTER_DATA_PREFIX_PARAM;
  private static final String STORE_PROPERTIES_PREFIX_PARAM = "s";
  private static final String STORE_FILTER_DATA_PREFIX_PARAM =
      STORE_PROPERTIES_PREFIX_PARAM + FILTER_DATA_PREFIX_PARAM;

  private final OfficeManager officeManager;

  /**
   * Creates a new controller.
   *
   * @param officeManager The manager used to execute conversions.
   */
  public ConverterController(final OfficeManager officeManager) {
    super();

    this.officeManager = officeManager;
  }

  @ApiOperation(
      "Convert the incoming document to the specified format (provided as request param) and returns the converted document.")
  @ApiResponses(
    value = {
      @ApiResponse(code = 200, message = "Document converted successfully."),
      @ApiResponse(code = 400, message = "The input document or output format is missing."),
      @ApiResponse(code = 500, message = "An unexpected error occured.")
    }
  )
  @PostMapping(produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public Object convertToUsingParam(
      @ApiParam(value = "The input document to convert.", required = true) @RequestParam("data")
          final MultipartFile inputFile,
      @ApiParam(value = "The document format to convert the input document to.", required = true)
          @RequestParam(name = "format")
          final String convertToFormat,
      @ApiParam(value = "The custom options to apply to the conversion.")
          @RequestParam(required = false)
          final Map<String, String> parameters) {

    LOGGER.debug("convertUsingRequestParam > Converting file to {}", convertToFormat);
    return convert(inputFile, convertToFormat, parameters);
  }

  @ApiOperation(
      "Convert the incoming document to the specified format (provided as path param) and returns the converted document.")
  @ApiResponses(
    value = {
      @ApiResponse(code = 200, message = "Document converted successfully."),
      @ApiResponse(code = 400, message = "The input document or output format is missing."),
      @ApiResponse(code = 500, message = "An unexpected error occured.")
    }
  )
  @PostMapping(value = "/{format}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public Object convertToUsingPath(
      @ApiParam(value = "The input document to convert.", required = true) @RequestParam("data")
          final MultipartFile inputFile,
      @ApiParam(value = "The document format to convert the input document to.", required = true)
          @PathVariable(name = "format")
          final String convertToFormat,
      @ApiParam(value = "The custom options to apply to the conversion.")
          @RequestParam(required = false)
          final Map<String, String> parameters) {

    LOGGER.debug("convertUsingPathVariable > Converting file to {}", convertToFormat);
    return convert(inputFile, convertToFormat, parameters);
  }

  private void addFilterDataProperty(
      final Map<String, Object> properties, final String name, final String value) {

    final Boolean bool = BooleanUtils.toBooleanObject(value);
    if (bool != null) {
      properties.put(name, bool);
    }
    try {
      final int ival = Integer.parseInt(value);
      properties.put(name, ival);
    } catch (NumberFormatException nfe) {
      properties.put(name, value);
    }
  }

  private void decodeParameters(
      final Map<String, String> parameters,
      final Map<String, Object> loadProperties,
      final Map<String, Object> storeProperties) {

    if (parameters == null || parameters.isEmpty()) {
      return;
    }

    final Map<String, Object> loadFilterDataProperties = new HashMap<>();
    final Map<String, Object> storeFilterDataProperties = new HashMap<>();
    for (final Map.Entry<String, String> param : parameters.entrySet()) {
      final String key = param.getKey().toLowerCase();
      if (key.startsWith(LOAD_FILTER_DATA_PREFIX_PARAM)) {
        addFilterDataProperty(
            loadFilterDataProperties,
            param.getKey().substring(LOAD_FILTER_DATA_PREFIX_PARAM.length()),
            param.getValue());
      } else if (key.startsWith(LOAD_PROPERTIES_PREFIX_PARAM)) {
        addFilterDataProperty(
            loadProperties,
            param.getKey().substring(LOAD_PROPERTIES_PREFIX_PARAM.length()),
            param.getValue());
      } else if (key.startsWith(STORE_FILTER_DATA_PREFIX_PARAM)) {
        addFilterDataProperty(
            storeFilterDataProperties,
            param.getKey().substring(STORE_FILTER_DATA_PREFIX_PARAM.length()),
            param.getValue());
      } else if (key.startsWith(STORE_PROPERTIES_PREFIX_PARAM)) {
        addFilterDataProperty(
            storeProperties,
            param.getKey().substring(STORE_PROPERTIES_PREFIX_PARAM.length()),
            param.getValue());
      }
    }

    if (!loadFilterDataProperties.isEmpty()) {
      loadProperties.put(FILTER_DATA, loadFilterDataProperties);
    }

    if (!storeFilterDataProperties.isEmpty()) {
      storeProperties.put(FILTER_DATA, storeFilterDataProperties);
    }
  }

  private ResponseEntity<Object> convert(
      final MultipartFile inputFile,
      final String outputFormat,
      final Map<String, String> parameters) {

    if (inputFile.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    if (StringUtils.isBlank(outputFormat)) {
      return ResponseEntity.badRequest().build();
    }

    // Here, we could have a dedicated service that would convert document
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

      final DocumentFormat targetFormat =
          DefaultDocumentFormatRegistry.getFormatByExtension(outputFormat);

      // Decode the parameters to load and store properties.
      final Map<String, Object> loadProperties =
          new HashMap<>(LocalConverter.DEFAULT_LOAD_PROPERTIES);
      final Map<String, Object> storeProperties = new HashMap<>();
      decodeParameters(parameters, loadProperties, storeProperties);

      // Create a converter with the properties.
      final DocumentConverter converter =
          LocalConverter.builder()
              .officeManager(officeManager)
              .loadProperties(loadProperties)
              .storeProperties(storeProperties)
              .build();

      // Convert...
      converter
          .convert(inputFile.getInputStream())
          .as(
              DefaultDocumentFormatRegistry.getFormatByExtension(
                  FilenameUtils.getExtension(inputFile.getOriginalFilename())))
          .to(baos)
          .as(targetFormat)
          .execute();

      final HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType(targetFormat.getMediaType()));
      headers.add(
          "Content-Disposition",
          "attachment; filename="
              + FilenameUtils.getBaseName(inputFile.getOriginalFilename())
              + "."
              + targetFormat.getExtension());
      return ResponseEntity.ok().headers(headers).body(baos.toByteArray());

    } catch (OfficeException | IOException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex);
    }
  }
}
