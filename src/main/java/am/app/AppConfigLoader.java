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
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.slf4j.LoggerFactory;
import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Version;
import com.thebuzzmedia.exiftool.exceptions.UnsupportedFeatureException;
import am.db.JdbcSerialization;
import am.filesystem.FileSystemHelper;
import am.filesystem.model.Volume;
import am.processor.hashes.HashConfig;
import am.processor.hashes.HashStrategy;
import am.validators.AbstractValidator;
import am.validators.MovieValidator;

/**
 * Load configuration information from a properties file to an {@link AppConfig} object.
 *
 */
public final class AppConfigLoader
{
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AppConfigLoader.class);
  private static final String LOG_DIR = "logDir";
  private static final String TSV_DIR = "tsvDir";
  private static final String DATABASE_DIR = "databaseDir";
  private static final String IGNORE_DIR_NAMES = "ignoreDirNames";
  private static final String IGNORE_FILE_NAMES = "ignoreFileNames";
  private static final String EXIFTOOL_PATH = "exiftoolPath";
  private static final String CREATE_HASHES = "createHashes";

  private AppConfigLoader()
  {
    // prevent instantiation
  }

  private static void loadVolumes(final AppConfig config, final Properties props)
  {
    final List<Volume> volumes = config.getVolumes();
    int volNr = 1;
    boolean found = true;
    do
    {
      final String key = "volume" + volNr;
      final Object value = props.remove(key);
      if (value == null)
      {
        found = false;
      }
      else
      {
        final Volume vol = new Volume();
        vol.setPath(value.toString());
        volumes.add(vol);
        loadValidator(config, props, vol, volNr);
        volNr++;
      }
    }
    while (found);
    LOGGER.debug(config.msg("init.debug.loaded_volumes", volumes.size()));
  }

  private static void loadValidator(AppConfig config, final Properties props, Volume vol, int volNr)
  {
    final String key = "volumeValidator" + volNr;
    final Object value = props.remove(key);
    if (value == null)
    {
      return;
    }
    final String name = value.toString();
    AbstractValidator validator;
    switch (name)
    {
    case "MovieValidator":
      validator = new MovieValidator();
      break;
    default:
      LOGGER.error(config.msg("init.error.unknown_validator", name, volNr));
      validator = null;
    }
    final Map<String, AbstractValidator> validators = config.getValidators();
    validators.put(FileSystemHelper.normalizePath(vol.getPath()), validator);
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

  public static boolean interpretProperties(final AppConfig config)
  {
    final Properties props = config.getProperties();
    initLogging(config, props);
    loadVolumes(config, props);
    initIgnoreDirNames(config, props);
    initIgnoreFileNames(config, props);
    initExiftool(config, props);
    initHashes(config, props);
    return initDatabase(config, props);
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
          double d = Double.valueOf(perc);
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
        final ExifTool exifTool = new ExifToolBuilder().withPath(path).enableStayOpen().build();
        final Version version = exifTool.getVersion();
        LOGGER.info(config.msg("init.info.exiftool_setup", path, version.toString()));
        config.setExifTool(exifTool);
        config.setExifToolPath(path);
        config.setExifToolMaxUsage(Long.valueOf(2048));
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
    if (props.containsKey(TSV_DIR))
    {
      final Object obj = props.remove(TSV_DIR);
      final String dirName = obj.toString();
      final File dir = new File(dirName);
      if (dir.isDirectory())
      {
        config.setTsvDirectory(dir);
      }
      else
      {
        LOGGER.error(config.msg("init.error.tsv_dir_does_not_exist", dirName));
        return false;
      }
    }
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
        io.connect(dir);
        io.createTables();
        config.setDatabaseSerializer(io);
      }
      else
      {
        LOGGER.error(config.msg("init.error.database_dir_does_not_exist", dirName));
        return false;
      }
    }
    return true;
  }

  public static void loadConfig(final AppConfig config)
  {
    String fileName = config.getConfigFileName();
    File file;
    if (fileName == null)
    {
      final File dir = new File(System.getProperty("user.home"));
      file = new File(dir, ".am.properties");
      fileName = file.getAbsolutePath();
    }
    else
    {
      file = new File(fileName);
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
}
