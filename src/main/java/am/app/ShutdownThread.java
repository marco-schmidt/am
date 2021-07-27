/*
 * Copyright 2019, 2020, 2021 the original author or authors.
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

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.slf4j.LoggerFactory;
import com.thebuzzmedia.exiftool.ExifTool;
import am.db.JdbcSerialization;
import am.services.wikidata.WikidataConfiguration;
import ch.qos.logback.classic.LoggerContext;

/**
 * Thread to close open resources when application is closed.
 *
 * @author Marco Schmidt
 */
public class ShutdownThread extends Thread
{
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ShutdownThread.class);
  private final AppConfig config;

  ShutdownThread(AppConfig c)
  {
    super("shutdn");
    config = c;
  }

  @Override
  public void run()
  {
    LOGGER.info(config.msg("shutdown.info.shutting_down"));

    // database first, its proper state is most important
    final JdbcSerialization io = config.getDatabaseSerializer();
    if (io != null)
    {
      io.close();
      config.setDatabaseSerializer(null);
    }

    // close instance(s) of exiftool
    final ExifTool exifTool = config.getExifTool();
    if (exifTool != null)
    {
      try
      {
        exifTool.close();
      }
      catch (final Exception e)
      {
        LOGGER.error(config.msg("exiftool.error.failed_to_close"), e);
      }
      config.setExifTool(null);
    }

    final WikidataConfiguration wikiConfig = config.getWikidataConfiguration();
    final RepositoryConnection connection = wikiConfig.getConnection();
    if (connection != null)
    {
      connection.close();
    }

    // shut down logging last so that everyone can log until the end
    final LoggingHandler loggingHandler = config.getLoggingHandler();
    final LoggerContext loggerContext = loggingHandler.getLoggerContext();
    loggerContext.stop();
  }
}
