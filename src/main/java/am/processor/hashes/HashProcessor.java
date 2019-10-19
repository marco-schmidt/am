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
package am.processor.hashes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.FileState;
import am.filesystem.model.Volume;

/**
 * Update hash values.
 *
 * @author Marco Schmidt
 */
public class HashProcessor
{
  private static final Logger LOGGER = LoggerFactory.getLogger(HashProcessor.class);
  private final List<File> files = new ArrayList<>();
  private long fileSizeSum;

  public List<File> getFiles()
  {
    return files;
  }

  public void update(final AppConfig config, final List<Volume> volumes)
  {
    final HashStrategy strategy = config.getHashConfig().getStrategy();
    if (strategy == HashStrategy.None)
    {
      return;
    }
    // put all files that are not missing in a list and add their sizes
    files.clear();
    fileSizeSum = initFileList(files, volumes, 0L);
    // sort list by necessity to compute hash: files without any hash value first, then by age in descending order
    // (oldest hashes first)
    Collections.sort(files, new HashFilePriorityComparator());
    compute(config);
  }

  private long initFileList(final List<File> files, final List<Volume> volumes, final long fileSizeSum)
  {
    long result = fileSizeSum;
    for (final Volume v : volumes)
    {
      result = initFileList(files, v, result);
    }
    return result;
  }

  private long initFileList(final List<File> files, final Volume volume, final long fileSizeSum)
  {
    return initFileList(files, volume.getRoot(), fileSizeSum);
  }

  private long initFileList(final List<File> files, final Directory dir, final long fileSizeSum)
  {
    long result = fileSizeSum;
    for (final File f : dir.getFiles())
    {
      result = initFileList(files, f, result);
    }
    for (final Directory d : dir.getSubdirectories())
    {
      result = initFileList(files, d, result);
    }
    return result;
  }

  private long initFileList(final List<File> files, final File file, final long fileSizeSum)
  {
    long result = fileSizeSum;
    final FileState state = file.getState();
    if (state != null && state != FileState.Missing)
    {
      files.add(file);
      result += file.getByteSize().longValue();
    }
    return result;
  }

  private void compute(AppConfig config)
  {
    final HashCreation creator = new HashCreation();
    final HashConfig hashConfig = config.getHashConfig();
    final HashStrategy strategy = hashConfig.getStrategy();
    final Double percentage = hashConfig.getPercentage();
    final String strategyInfo = formatStrategyInfo(config, strategy, percentage);
    long computedBytes = 0;
    boolean done = false;
    LOGGER.info(config.msg("hashcreation.info.strategy", strategyInfo, files.size()));
    for (final File file : files)
    {
      // LOGGER.debug(config.msg("hashcreation.debug.file_info", file.getEntry().getAbsolutePath(), file.getByteSize(),
      // file.getHashValue() == null ? '-' : file.getHashValue(),
      // file.getHashCreated() == null ? "-" : file.getHashCreated().toString()));
      creator.update(config, file);
      computedBytes += file.getByteSize().longValue();
      switch (strategy)
      {
      case Percentage:
        if (fileSizeSum > 0 && percentage != null)
        {
          final double perc = 100d * computedBytes / fileSizeSum;
          done = perc >= percentage.doubleValue();
        }
        break;
      default:
        break;
      }
      if (done)
      {
        break;
      }
    }
  }

  private String formatStrategyInfo(AppConfig config, HashStrategy strategy, Double percentage)
  {
    final String name = config.msg("hashcreation.info.strategy." + strategy.toString());
    String details = "";
    if (strategy == HashStrategy.Percentage)
    {
      details = ", " + percentage.toString() + "%";
    }
    return name + details;
  }
}
