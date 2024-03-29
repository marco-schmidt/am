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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.slf4j.LoggerFactory;
import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Version;
import com.thebuzzmedia.exiftool.exceptions.UnsupportedFeatureException;
import am.db.JdbcSerialization;
import am.filesystem.FileSystemHelper;
import am.processor.hashes.HashConfig;
import am.processor.hashes.HashStrategy;

/**
 * Helper methods to load and initialize {@link AppConfig} objects.
 */
public final class AppConfigUtils
{
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AppConfigUtils.class);
  private static final String LOG_DIR = "logDir";
  private static final String DATABASE_DIR = "databaseDir";
  private static final String IGNORE_DIR_NAMES = "ignoreDirNames";
  private static final String IGNORE_FILE_NAMES = "ignoreFileNames";
  private static final String EXIFTOOL_PATH = "exiftoolPath";
  private static final String CREATE_HASHES = "createHashes";
  private static final String WIKIDATA = "wikidata";

  private AppConfigUtils()
  {
    // prevent instantiation
  }

  private static void initLogging(final AppConfig config, final Properties props)
  {
    final LoggingHandler loggingHandler = config.getLoggingHandler();
    if (props.containsKey(LOG_DIR))
    {
      final Object logDir = props.remove(LOG_DIR);
      final String path = logDir.toString();
      final File dir = new File(path);
      if (dir.isDirectory())
      {
        loggingHandler.setLogDirectory(dir);
        loggingHandler.initializeFile(config);
      }
      else
      {
        LOGGER.error(config.msg("init.error.invalid_log_directory", path));
      }
    }
  }

  private static void initWikidata(final AppConfig config, final Properties props)
  {
    if (props.containsKey(WIKIDATA))
    {
      final Object obj = props.remove(WIKIDATA);
      final Boolean wikidata = Boolean.valueOf(obj.toString());
      config.getWikidataConfiguration().setEnabled(wikidata.booleanValue());
    }
  }

  public static boolean interpretProperties(final AppConfig config)
  {
    final Properties props = config.getProperties();
    initLogging(config, props);
    initIgnoreDirNames(config, props);
    initIgnoreFileNames(config, props);
    initExiftool(config, props);
    initHashes(config, props);
    initWikidata(config, props);
    boolean success = initDatabase(config, props);
    if (success && !props.isEmpty())
    {
      LOGGER.error(config.msg("init.error.unknown_config_key", props.keys().nextElement().toString()));
      success = false;
    }
    return success;
  }

  private static void initHashes(AppConfig config, Properties props)
  {
    if (props.containsKey(CREATE_HASHES))
    {
      final HashConfig hashConfig = config.getHashConfig();
      final String s = props.remove(CREATE_HASHES).toString();
      if ("always".equalsIgnoreCase(s))
      {
        hashConfig.setStrategy(HashStrategy.All);
      }
      if ("never".equalsIgnoreCase(s))
      {
        hashConfig.setStrategy(HashStrategy.None);
      }
      if (s.endsWith("%"))
      {
        final String perc = s.substring(0, s.length() - 1);
        try
        {
          double d = Double.parseDouble(perc);
          d = Math.max(d, 0.0d);
          d = Math.min(d, 100.0d);
          hashConfig.setPercentage(Double.valueOf(d));
          hashConfig.setStrategy(HashStrategy.Percentage);
        }
        catch (final NumberFormatException nfe)
        {
          LOGGER.error(config.msg("init.error.hash_percentage", s));
        }
      }
    }
  }

  private static void initExiftool(AppConfig config, Properties props)
  {
    if (props.containsKey(EXIFTOOL_PATH))
    {
      final Object exiftoolPath = props.remove(EXIFTOOL_PATH);
      final String path = exiftoolPath.toString();
      try
      {
        final long millis = System.currentTimeMillis();
        final ExifTool exifTool = new ExifToolBuilder().withPath(path).enableStayOpen().build();
        final Version version = exifTool.getVersion();
        LOGGER.debug(
            config.msg("init.debug.exiftool_setup", path, version.toString(), System.currentTimeMillis() - millis));
        config.setExifTool(exifTool);
        config.setExifToolPath(path);
        config.setExifToolMaxUsage(Long.valueOf(512));
      }
      catch (final UnsupportedFeatureException ex)
      {
        LOGGER.error(config.msg("init.error.exiftool_setup", path), ex);
      }
    }
    else
    {
      LOGGER.debug(config.msg("init.debug.exiftool_undefined"));
    }
  }

  private static void initIgnoreDirNames(final AppConfig config, final Properties props)
  {
    Set<String> names;
    if (props.containsKey(IGNORE_DIR_NAMES))
    {
      final Object value = props.remove(IGNORE_DIR_NAMES);
      names = FileSystemHelper.splitFileNames(value.toString(), ",");
    }
    else
    {
      names = new HashSet<>();
    }
    config.setIgnoreDirNames(names);
  }

  private static void initIgnoreFileNames(final AppConfig config, final Properties props)
  {
    Set<String> names;
    if (props.containsKey(IGNORE_FILE_NAMES))
    {
      final Object value = props.remove(IGNORE_FILE_NAMES);
      names = FileSystemHelper.splitFileNames(value.toString(), ",");
    }
    else
    {
      names = new HashSet<>();
    }
    config.setIgnoreFileNames(names);
  }

  private static boolean initDatabase(AppConfig config, Properties props)
  {
    if (props.containsKey(DATABASE_DIR))
    {
      final Object obj = props.remove(DATABASE_DIR);
      final String dirName = obj.toString();
      final File dir = new File(dirName);
      if (dir.isDirectory())
      {
        config.setDatabaseDirectory(dir);
        final JdbcSerialization io = new JdbcSerialization();
        io.setConfig(config);
        if (!io.connect(dir))
        {
          return false;
        }
        io.createTables();
        config.setDatabaseSerializer(io);
        return true;
      }
      else
      {
        LOGGER.error(config.msg("init.error.database_dir_does_not_exist", dirName));
        return false;
      }
    }
    else
    {
      return true;
    }
  }

  public static boolean loadConfig(final AppConfig config)
  {
    String fileName = config.getConfigFileName();
    File file;
    boolean defaultFileName;
    if (fileName == null)
    {
      final File dir = new File(System.getProperty("user.home"));
      file = new File(dir, ".am.properties");
      fileName = file.getAbsolutePath();
      defaultFileName = true;
    }
    else
    {
      file = new File(fileName);
      defaultFileName = false;
    }

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
        return true;
      }
      catch (final IOException e)
      {
        LOGGER.error(config.msg("init.error.failure_load_configuration", fileName, e.getMessage()));
        return false;
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
      if (defaultFileName)
      {
        LOGGER.info(config.msg("init.info.skip_load_configuration", fileName));
        return true;
      }
      else
      {
        LOGGER.error(config.msg("init.error.configuration_file_does_not_exist", fileName));
        return false;
      }
    }
  }
}
