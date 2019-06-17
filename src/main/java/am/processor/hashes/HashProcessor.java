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
  private final List<File> files = new ArrayList<>();
  private long fileSizeSum;

  public void update(final AppConfig config, final List<Volume> volumes)
  {
    files.clear();
    fileSizeSum = 0;
    for (final Volume v : volumes)
    {
      find(config, v);
    }
    Collections.sort(files, new HashFilePriorityComparator());
    compute(config);
  }

  private void find(final AppConfig config, final Volume volume)
  {
    find(config, volume.getRoot());
  }

  private void find(final AppConfig config, Directory dir)
  {
    for (final File f : dir.getFiles())
    {
      find(config, f);
    }
    for (final Directory d : dir.getSubdirectories())
    {
      find(config, d);
    }
  }

  private void find(final AppConfig config, File file)
  {
    final FileState state = file.getState();
    if (state != null && state != FileState.Missing)
    {
      files.add(file);
      fileSizeSum += file.getByteSize().longValue();
    }
  }

  private void compute(AppConfig config)
  {
    final HashCreation creator = new HashCreation();
    final HashConfig hashConfig = config.getHashConfig();
    final HashStrategy strategy = hashConfig.getStrategy();
    final Double percentage = hashConfig.getPercentage();
    long computedBytes = 0;
    boolean done = false;
    for (final File file : files)
    {
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
}
