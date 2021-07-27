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

import java.io.File;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import org.slf4j.LoggerFactory;
import am.db.JdbcSerialization;
import am.filesystem.VolumeScanner;
import am.filesystem.model.Volume;
import am.processor.MetadataExtraction;
import am.processor.VolumeProcessor;
import am.processor.hashes.HashProcessor;
import am.validators.AbstractValidator;
import am.validators.MovieValidator;
import am.validators.PersonalDocumentValidator;
import am.validators.TvSeriesValidator;

/**
 * Application's main class.
 *
 * @author Marco Schmidt
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
      if (!AppConfigUtils.loadConfig(config))
      {
        return false;
      }
      if (!AppConfigUtils.interpretProperties(config))
      {
        return false;
      }
      if (config.isShowEnvironment())
      {
        info.print(config);
      }
      Runtime.getRuntime().addShutdownHook(new ShutdownThread(config));
    }
    else
    {
      success = false;
    }
    return success;
  }

  private void printVersion(final AppConfig config)
  {
    String version = config.getSystemInfo().getApplicationVersion();
    if (version == null)
    {
      // when run inside of IDE
      version = "";
    }
    else
    {
      version += " ";
    }
    LOGGER.info(String.format(Locale.ROOT, "%s %s<%s>", SystemInfo.APP_NAME, version, SystemInfo.APP_URI));
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
    final JdbcSerialization io = config.getDatabaseSerializer();
    if (io != null)
    {
      final List<Volume> loadedVolumes = io.loadAll();
      final List<Volume> scannedVolumes = new ArrayList<>();
      for (final Volume loadedVol : loadedVolumes)
      {
        final String path = loadedVol.getPath();
        final File dir = new File(path);
        if (dir.exists())
        {
          if (dir.isDirectory())
          {
            final Volume vol = new Volume();
            vol.setPath(loadedVol.getPath());
            vol.setEntry(dir);
            scannedVolumes.add(vol);
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

      final VolumeProcessor proc = new VolumeProcessor();
      proc.setConfig(config);
      final List<Volume> mergedVolumes = proc.processVolumes(scannedVolumes, loadedVolumes);

      final MetadataExtraction extraction = new MetadataExtraction();
      extraction.update(config, mergedVolumes);

      validate(config, mergedVolumes);

      final HashProcessor hashProcessor = new HashProcessor();
      hashProcessor.update(config, mergedVolumes);

      io.saveAll(mergedVolumes);
    }
  }

  private void validate(final AppConfig config, final List<Volume> volumes)
  {
    for (final Volume vol : volumes)
    {
      final AbstractValidator validator = createValidator(config, vol.getValidator(), vol.getId());
      if (validator != null)
      {
        validator.setConfig(config);
        validator.validate(config, vol);
      }
    }
  }

  private static void registerValidators()
  {
    if (!AbstractValidator.hasRegisteredValidators())
    {
      AbstractValidator.register(MovieValidator.class);
      AbstractValidator.register(TvSeriesValidator.class);
    }
  }

  private AbstractValidator createValidator(final AppConfig config, String validatorName, Long volNr)
  {
    if (validatorName == null)
    {
      return null;
    }
    switch (validatorName)
    {
    case "MovieValidator":
      return new MovieValidator();
    case "TvSeriesValidator":
      return new TvSeriesValidator();
    case "PersonalDocumentValidator":
      return new PersonalDocumentValidator();
    default:
      LOGGER.error(config.msg("init.error.unknown_validator", validatorName, volNr));
      return null;
    }
  }

  private void process(final AppConfig config)
  {
    printVersion(config);
    switch (config.getMode())
    {
    case AddVolume:
    {
      new DatabaseService().addVolume(config);
      break;
    }
    case DeleteVolume:
    {
      new DatabaseService().deleteVolume(config);
      break;
    }
    case ShowHelp:
    {
      printHelp(config);
      break;
    }
    case ShowVersion:
    {
      break;
    }
    default:
    {
      processVolumes(config);
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
    config.setMode(ProcessMode.Check);
    final LoggingHandler log = new LoggingHandler();
    config.setLoggingHandler(log);
    log.initialize(config);
    registerValidators();
    if (app.initialize(config, args))
    {
      app.process(config);
    }
  }
}
