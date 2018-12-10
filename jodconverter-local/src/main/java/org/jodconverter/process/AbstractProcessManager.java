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

package org.jodconverter.process;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all process manager implementations included in the standard JODConverter
 * distribution.
 */
public abstract class AbstractProcessManager implements ProcessManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProcessManager.class);

  /** Initializes a new instance of the class. */
  protected AbstractProcessManager() {
    super();
  }

  private String buildOutput(final List<String> lines) {
    Objects.requireNonNull(lines, "lines must not be null");

    // Ignore empty lines
    return lines.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("\n"));
  }

  /**
   * Executes the specified command and return the output.
   *
   * @param cmdarray An array containing the command to call and its arguments.
   * @return The command execution output.
   * @throws IOException If an I/O error occurs.
   */
  protected List<String> execute(final String[] cmdarray) throws IOException {

    final Process process = Runtime.getRuntime().exec(cmdarray);

    final LinesPumpStreamHandler streamsHandler =
        new LinesPumpStreamHandler(process.getInputStream(), process.getErrorStream());

    streamsHandler.start();
    try {
      process.waitFor();
      streamsHandler.stop();
    } catch (InterruptedException ex) {

      // Log the interruption
      LOGGER.warn(
          "The current thread was interrupted while waiting for command execution output.", ex);
      // Restore the interrupted status
      Thread.currentThread().interrupt();
    }

    final List<String> outLines = streamsHandler.getOutputPumper().getLines();

    if (LOGGER.isDebugEnabled()) {
      final String out = buildOutput(outLines);
      final String err = buildOutput(streamsHandler.getErrorPumper().getLines());

      if (!StringUtils.isBlank(out)) {
        LOGGER.trace("Command Output: {}", out);
      }

      if (!StringUtils.isBlank(err)) {
        LOGGER.trace("Command Error: {}", err);
      }
    }

    return outLines;
  }

  @Override
  public long findPid(final ProcessQuery query) throws IOException {

    final Pattern commandPattern =
        Pattern.compile(
            Pattern.quote(query.getCommand()) + ".*" + Pattern.quote(query.getArgument()));
    final Pattern processLinePattern = getRunningProcessLinePattern();
    final String[] currentProcessesCommand = getRunningProcessesCommand(query.getCommand());

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace(
          "Finding PID using\n"
              + "Command to get current running processes: {}\n"
              + "Regex used to match current running process lines: {}\n"
              + "Regex used to match running office process we are looking for: {}",
          currentProcessesCommand,
          processLinePattern.pattern(), // NOSONAR
          commandPattern.pattern());
    }

    final List<String> lines = execute(currentProcessesCommand);
    for (final String line : lines) {
      if (StringUtils.isBlank(line)) {
        // Skip this one
        continue;
      }
      LOGGER.trace(
          "Checking if process line matches the process line regex\nProcess line: {}", line);
      final Matcher lineMatcher = processLinePattern.matcher(line);
      if (lineMatcher.matches()) {
        final String pid = lineMatcher.group("Pid");
        final String commandLine = lineMatcher.group("CommanLine");
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace(
              "Line matches!\n"
                  + "pid: {}; Command line: {}\n"
                  + "Checking if this command line matches the office command line regex",
              pid,
              commandLine);
        }
        final Matcher commandMatcher = commandPattern.matcher(commandLine);
        if (commandMatcher.find()) {
          LOGGER.debug("Command line matches! Returning pid: {}", pid);
          return Long.parseLong(pid);
        }
      }
    }
    return PID_NOT_FOUND;
  }

  /**
   * Gets the command to be executed to get a snapshot of all the running processes identified by
   * the specified argument (process).
   *
   * @param process The name of the process to query for.
   * @return An array containing the command to call and its arguments.
   */
  protected abstract String[] getRunningProcessesCommand(String process);

  /**
   * Gets the pattern to be used to match an output line containing the information about a running
   * process. The output lines being tested against this pattern are the result of the execution of
   * the command returned by the getRunningProcessesCommand function.
   *
   * @return The pattern.
   * @see #getRunningProcessesCommand(String)
   */
  protected abstract Pattern getRunningProcessLinePattern();
}
