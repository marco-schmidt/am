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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import com.thebuzzmedia.exiftool.ExifTool;
import am.conversion.StrUtil;
import am.db.JdbcSerialization;
import am.filesystem.model.Volume;
import am.processor.hashes.HashConfig;
import am.validators.AbstractValidator;

/**
 * Configuration for {@link App} application.
 */
public class AppConfig
{
  /**
   * Logging Mapped Diagnostic Context identifier for the machine.
   */
  public static final String MDC_MACHINE = "machine";

  /**
   * Default logging pattern.
   */
  public static final String DEFAULT_LOGGING_PATTERN = "%date{yyyy-MM-dd'T'HH:mm:ss.SSSZ}\t%X{" + MDC_MACHINE
      + "}\t%thread\t%level\t%message%n";

  /**
   * If no argument is specified, use this many threads per CPU as returned by {@link Runtime#availableProcessors()}.
   */
  public static final int DEFAULT_NUMBER_OF_THREADS_PER_CPU = 4;

  private boolean showEnvironment;
  private ResourceBundle bundle;
  private ProcessMode mode = ProcessMode.ShowHelp;
  private SystemInfo systemInfo;
  private Integer numberOfThreads;
  private Locale locale;
  private boolean quiet;
  private Properties properties;
  private final List<Volume> volumes;
  private LoggingHandler loggingHandler;
  private String configFileName;
  private File tsvDirectory;
  private Set<String> ignoreDirNames;
  private Set<String> ignoreFileNames;
  private ExifTool exifTool;
  private String exifToolPath;
  private Long exifToolMaxUsage;
  private HashConfig hashConfig;
  private Map<String, AbstractValidator> validators;
  private boolean queryWikidata = true;
  private File databaseDirectory;
  private JdbcSerialization databaseSerializer;

  public AppConfig()
  {
    volumes = new ArrayList<>();
    setProperties(new Properties());
    loggingHandler = new LoggingHandler();
    hashConfig = new HashConfig();
    validators = new HashMap<>();
  }

  public ResourceBundle getBundle()
  {
    return bundle;
  }

  public final void setBundle(final ResourceBundle bundle)
  {
    this.bundle = bundle;
  }

  /**
   * Look up message in resource bundle.
   *
   * @param key
   *          message key
   * @return looked-up message
   */
  public String msg(final String key)
  {
    return bundle != null && bundle.containsKey(key) ? bundle.getString(key) : "";
  }

  /**
   * Look up message in resource bundle and format using arguments.
   *
   * @param key
   *          message key
   * @param args
   *          arguments used for formatting
   * @return looked-up message
   */
  public String msg(final String key, final Object... args)
  {
    String result;
    final String pattern = msg(key);
    if (pattern.isEmpty())
    {
      result = "";
    }
    else
    {
      final MessageFormat formatter = new MessageFormat(pattern, getLocale());
      result = formatter.format(args);
      result = StrUtil.escapeControl(result);
    }
    return result;
  }

  public boolean hasMsg(final String key)
  {
    return bundle != null && bundle.containsKey(key);
  }

  public ProcessMode getMode()
  {
    return mode;
  }

  public void setMode(final ProcessMode mode)
  {
    this.mode = mode;
  }

  public boolean isShowEnvironment()
  {
    return showEnvironment;
  }

  public void setShowEnvironment(final boolean showEnvironment)
  {
    this.showEnvironment = showEnvironment;
  }

  public SystemInfo getSystemInfo()
  {
    return systemInfo;
  }

  public void setSystemInfo(final SystemInfo systemInfo)
  {
    this.systemInfo = systemInfo;
  }

  public Integer getNumberOfThreads()
  {
    return numberOfThreads;
  }

  public void setNumberOfThreads(final Integer numberOfThreads)
  {
    this.numberOfThreads = numberOfThreads;
  }

  public Locale getLocale()
  {
    return locale;
  }

  public void setLocale(final Locale locale)
  {
    this.locale = locale;
  }

  public boolean isQuiet()
  {
    return quiet;
  }

  public void setQuiet(final boolean quiet)
  {
    this.quiet = quiet;
  }

  public Properties getProperties()
  {
    return properties;
  }

  public void setProperties(Properties properties)
  {
    this.properties = properties;
  }

  public List<Volume> getVolumes()
  {
    return volumes;
  }

  public LoggingHandler getLoggingHandler()
  {
    return loggingHandler;
  }

  public void setLoggingHandler(LoggingHandler loggingHandler)
  {
    this.loggingHandler = loggingHandler;
  }

  public String getConfigFileName()
  {
    return configFileName;
  }

  public void setConfigFileName(String configFileName)
  {
    this.configFileName = configFileName;
  }

  public File getTsvDirectory()
  {
    return tsvDirectory;
  }

  public void setTsvDirectory(File tsvDirectory)
  {
    this.tsvDirectory = tsvDirectory;
  }

  public Set<String> getIgnoreDirNames()
  {
    return ignoreDirNames;
  }

  public void setIgnoreDirNames(Set<String> ignoreDirNames)
  {
    this.ignoreDirNames = ignoreDirNames;
  }

  public Set<String> getIgnoreFileNames()
  {
    return ignoreFileNames;
  }

  public void setIgnoreFileNames(Set<String> ignoreFileNames)
  {
    this.ignoreFileNames = ignoreFileNames;
  }

  public ExifTool getExifTool()
  {
    return exifTool;
  }

  public void setExifTool(ExifTool exifTool)
  {
    this.exifTool = exifTool;
  }

  public String getExifToolPath()
  {
    return exifToolPath;
  }

  public void setExifToolPath(String exifToolPath)
  {
    this.exifToolPath = exifToolPath;
  }

  public Long getExifToolMaxUsage()
  {
    return exifToolMaxUsage;
  }

  public void setExifToolMaxUsage(Long exifToolMaxUsage)
  {
    this.exifToolMaxUsage = exifToolMaxUsage;
  }

  public HashConfig getHashConfig()
  {
    return hashConfig;
  }

  public void setHashConfig(HashConfig hashConfig)
  {
    this.hashConfig = hashConfig;
  }

  public Map<String, AbstractValidator> getValidators()
  {
    return validators;
  }

  public void setValidators(Map<String, AbstractValidator> validators)
  {
    this.validators = validators;
  }

  public boolean isQueryWikidata()
  {
    return queryWikidata;
  }

  public void setQueryWikidata(boolean queryWikidata)
  {
    this.queryWikidata = queryWikidata;
  }

  public File getDatabaseDirectory()
  {
    return databaseDirectory;
  }

  public void setDatabaseDirectory(File databaseDirectory)
  {
    this.databaseDirectory = databaseDirectory;
  }

  public JdbcSerialization getDatabaseSerializer()
  {
    return databaseSerializer;
  }

  public void setDatabaseSerializer(JdbcSerialization databaseSerializer)
  {
    this.databaseSerializer = databaseSerializer;
  }
}
