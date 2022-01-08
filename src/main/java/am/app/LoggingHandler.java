/*
 * Copyright 2019, 2020, 2021, 2022 the original author or authors.
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
package am.app;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;

/**
 * This class deals with setting up and shutting down all logging-related functionality.
 */
public class LoggingHandler
{
  private static final Logger LOGGER = LoggerFactory.getLogger(LoggingHandler.class);
  private ch.qos.logback.classic.Logger rootLogger;
  private PatternLayoutEncoder encoder;
  private LoggerContext loggerContext;
  private File logDirectory;

  private LoggerContext initLoggerContext(final LoggerContext context)
  {
    context.reset();
    return context;
  }

  public void initialize(final AppConfig config)
  {
    rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    loggerContext = initLoggerContext(rootLogger.getLoggerContext());

    try
    {
      MDC.put(AppConfig.MDC_MACHINE, InetAddress.getLocalHost().getHostName());
    }
    catch (IllegalArgumentException | UnknownHostException e)
    {
      LOGGER.error(config.msg("system.error.failed_to_look_up_host", e.getMessage()));
    }

    encoder = new PatternLayoutEncoder();
    encoder.setContext(loggerContext);
    encoder.setPattern(AppConfig.DEFAULT_LOGGING_PATTERN);
    encoder.start();

    final ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<ILoggingEvent>();
    consoleAppender.setContext(loggerContext);
    consoleAppender.setEncoder(encoder);
    consoleAppender.start();
    rootLogger.addAppender(consoleAppender);

    rootLogger.setLevel(Level.INFO);
  }

  public void initializeFile(final AppConfig config)
  {
    if (logDirectory != null)
    {
      final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ROOT);
      final String name = formatter.format(new Date()) + ".log";
      final File file = new File(logDirectory, name);

      final FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
      fileAppender.setContext(loggerContext);
      fileAppender.setEncoder(encoder);
      fileAppender.setFile(file.getAbsolutePath());
      fileAppender.start();
      rootLogger.addAppender(fileAppender);
    }
  }

  public File getLogDirectory()
  {
    return logDirectory;
  }

  public void setLogDirectory(File logDirectory)
  {
    this.logDirectory = logDirectory;
  }

  public LoggerContext getLoggerContext()
  {
    return loggerContext;
  }

  public void setLoggerContext(LoggerContext loggerContext)
  {
    this.loggerContext = loggerContext;
  }
}
