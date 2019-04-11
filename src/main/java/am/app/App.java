/*
 * Copyright 2019 the original author or authors.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

/**
 */
public class App
{
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(App.class);
  static
  {
    // try
    // {
    // exifTool = new ExifToolBuilder().withPath("/usr/local/bin/exiftool").enableStayOpen().build();
    // }
    // catch (final UnsupportedFeatureException ex)
    // {
    // exifTool = new ExifToolBuilder().build();
    // }
  }

  private void loadConfig(final AppConfig config)
  {
    final File dir = new File(System.getProperty("user.home"));
    final File file = new File(dir, ".am.properties");
    final String fileName = file.getAbsolutePath();
    if (file.exists())
    {
      BufferedReader reader = null;
      try
      {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        final Properties props = new Properties();
        props.load(reader);
        config.setProperties(props);
        LOGGER.debug(config.msg("init.info.success_load_configuration", fileName, props.size()));
      }
      catch (final IOException e)
      {
        LOGGER.error(config.msg("init.error.failure_load_configuration", fileName, e.getMessage()));
      }
      finally
      {
        try
        {
          if (reader != null)
          {
            reader.close();
          }
        }
        catch (final IOException e)
        {
          LOGGER.error(config.msg("init.error.failure_close_configuration", fileName, e.getMessage()));
        }
      }
    }
    else
    {
      LOGGER.info(config.msg("init.info.skip_load_configuration", fileName));
    }
  }

  private boolean initialize(final AppConfig config, final String... args)
  {
    boolean success = true;
    final ResourceBundle bundle = ResourceBundle.getBundle("Messages", config.getLocale());
    config.setBundle(bundle);
    final ArgumentParser parser = new ArgumentParser();
    if (parser.parse(config, args))
    {
      // for (final String dirName : config.getDirectoryNames())
      // {
      // try
      // {
      // LOGGER.debug(config.msg("args.debug.scanning_directory", dirName));
      // // Files.walkFileTree(Paths.get(dirName), new CollectAllFilesVisitor(config));
      // }
      // catch (final IOException e)
      // {
      // LOGGER.error(config.msg("args.error.scanning_directory", dirName), e);
      // }
      // }

      final SystemInfo info = new SystemInfo();
      config.setSystemInfo(info);
      info.initialize(config, args);
      if (config.isShowEnvironment())
      {
        info.print(config);
      }
      loadConfig(config);
    }
    else
    {
      success = false;
    }
    return success;
  }

  /*
   * public static void wd() throws MediaWikiApiErrorException { final WikibaseDataFetcher wbdf =
   * WikibaseDataFetcher.getWikidataDataFetcher(); final EntityDocument q42 = wbdf.getEntityDocument("Q42");
   * System.out.println("The current revision of the data for entity Q42 is " + q42.getRevisionId()); final
   * List<WbSearchEntitiesResult> list = wbdf.searchEntities("Wikidata Toolkit"); for (final WbSearchEntitiesResult res
   * : list) { System.out.println(res.getTitle() + " " + res.getLabel()); final EntityDocument ent =
   * wbdf.getEntityDocument(res.getEntityId()); if (ent instanceof ItemDocument) { final ItemDocument doc =
   * (ItemDocument) ent; final Iterator<Statement> stats = doc.getAllStatements(); while (stats.hasNext()) { final
   * Statement s = stats.next(); System.out.println(s.toString()); } } break; } }
   */

  private static LoggerContext initLoggerContext(final LoggerContext loggerContext)
  {
    loggerContext.reset();
    return loggerContext;
  }

  private static void initLogger(final AppConfig config)
  {
    final ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory
        .getLogger(Logger.ROOT_LOGGER_NAME);
    final LoggerContext loggerContext = initLoggerContext(rootLogger.getLoggerContext());

    try
    {
      MDC.put(AppConfig.MDC_MACHINE, InetAddress.getLocalHost().getHostName());
    }
    catch (IllegalArgumentException | UnknownHostException e)
    {
      LOGGER.error(config.msg("system.error.failed_to_look_up_host", e.getMessage()));
    }

    final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setContext(loggerContext);
    // encoder.setPattern("%message%n");
    encoder.setPattern(AppConfig.DEFAULT_LOGGING_PATTERN);
    encoder.start();

    final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
    appender.setContext(loggerContext);
    appender.setEncoder(encoder);
    appender.start();

    rootLogger.addAppender(appender);
    rootLogger.setLevel(Level.INFO);
  }

  private void printVersion(final AppConfig config)
  {
    LOGGER.info(String.format("%s %s", SystemInfo.APP_NAME, config.getSystemInfo().getApplicationVersion()));
  }

  private void printHelp(final AppConfig config)
  {
    for (final AbstractParameter param : ArgumentParser.getParameters())
    {
      LOGGER.info(ArgumentParser.format(config, param));
    }
  }

  private void process(final AppConfig config)
  {
    switch (config.getMode())
    {
    case ShowHelp:
    {
      printHelp(config);
      break;
    }
    case ShowVersion:
    {
      printVersion(config);
      break;
    }
    default:
    {
      // TODO: process volumes
      break;
    }
    }
  }

  public static void main(String[] args) throws Exception
  {
    final App app = new App();
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    final AppConfig config = new AppConfig();
    config.setLocale(Locale.ENGLISH);
    initLogger(config);
    if (app.initialize(config, args))
    {
      app.process(config);
    }
    // wd();
  }
}
