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

import java.io.File;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;
import org.slf4j.LoggerFactory;
import am.db.JdbcSerialization;
import am.db.TsvSerialization;
import am.filesystem.VolumeScanner;
import am.filesystem.model.Volume;
import am.processor.MetadataExtraction;
import am.processor.VolumeProcessor;
import am.processor.hashes.HashProcessor;
import am.validators.AbstractValidator;

/**
 */
public class App
{
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(App.class);

  private boolean initialize(final AppConfig config, final String... args)
  {
    boolean success = true;
    final ResourceBundle bundle = ResourceBundle.getBundle("Messages", config.getLocale());
    config.setBundle(bundle);
    final ArgumentParser parser = new ArgumentParser();
    if (parser.parse(config, args))
    {
      final SystemInfo info = new SystemInfo();
      config.setSystemInfo(info);
      info.initialize(config, args);
      AppConfigLoader.loadConfig(config);
      if (!AppConfigLoader.interpretProperties(config))
      {
        return false;
      }
      if (config.isShowEnvironment())
      {
        info.print(config);
      }
    }
    else
    {
      success = false;
    }
    return success;
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

  private void processVolumes(final AppConfig config)
  {
    for (final Volume vol : config.getVolumes())
    {
      final String path = vol.getPath();
      final File dir = new File(path);
      if (dir.exists())
      {
        if (dir.isDirectory())
        {
          final VolumeScanner scanner = new VolumeScanner(config, vol);
          scanner.scan();
        }
        else
        {
          LOGGER.error(config.msg("processor.error.not_a_directory", path));
        }
      }
      else
      {
        LOGGER.error(config.msg("processor.error.directory_invalid", path));
      }
    }
    final TsvSerialization tsv = new TsvSerialization();
    final List<Volume> loadedVolumes = tsv.load(config);
    LOGGER.info(config.msg("init.info.loaded_volumes", loadedVolumes.size()));
    final VolumeProcessor proc = new VolumeProcessor();
    proc.setConfig(config);
    final List<Volume> mergedVolumes = proc.processVolumes(config.getVolumes(), loadedVolumes);
    final MetadataExtraction extraction = new MetadataExtraction();
    extraction.update(config, mergedVolumes);
    validate(config, mergedVolumes);
    final HashProcessor hashProcessor = new HashProcessor();
    hashProcessor.update(config, mergedVolumes);
    tsv.save(config, mergedVolumes);
  }

  private void validate(final AppConfig config, final List<Volume> volumes)
  {
    final Map<String, AbstractValidator> validators = config.getValidators();
    for (final Volume vol : volumes)
    {
      final AbstractValidator validator = validators.get(vol.getPath());
      if (validator != null)
      {
        validator.setConfig(config);
        validator.validate(config, vol);
      }
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
      processVolumes(config);
      break;
    }
    }
    final JdbcSerialization io = config.getDatabaseSerializer();
    if (io != null)
    {
      io.close();
    }
  }

  public static void main(String[] args) throws Exception
  {
    final App app = new App();
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    final AppConfig config = new AppConfig();
    config.setLocale(Locale.ENGLISH);
    config.setMode(ProcessMode.Check);
    final LoggingHandler log = new LoggingHandler();
    config.setLoggingHandler(log);
    log.initialize(config);
    if (app.initialize(config, args))
    {
      app.process(config);
    }
  }
}
