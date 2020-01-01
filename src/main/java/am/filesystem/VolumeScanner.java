/*
 * Copyright 2019, 2020 the original author or authors.
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
package am.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.filesystem.model.Directory;
import am.filesystem.model.Volume;

/**
 * Scan a directory tree for entries and put them into a {@link Volume} object.
 */
public class VolumeScanner
{
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(VolumeScanner.class);
  private final Volume volume;
  private final AppConfig config;

  public VolumeScanner(final AppConfig config, final Volume v)
  {
    this.volume = v;
    this.config = config;
  }

  public void scan()
  {
    final String dirName = volume.getPath();
    try
    {
      final long millis = System.currentTimeMillis();
      LOGGER.debug(config.msg("scanner.debug.start", dirName));
      final VolumeVisitor visitor = new VolumeVisitor(this, config);
      Files.walkFileTree(Paths.get(dirName), visitor);
      LOGGER.debug(config.msg("scanner.debug.end", dirName, visitor.getNumDirectories(), visitor.getNumFiles(),
          visitor.getNumBytes(), System.currentTimeMillis() - millis));
    }
    catch (final IOException e)
    {
      LOGGER.error(config.msg("scanner.error.scanning_directory", dirName), e);
    }
  }

  public Directory addDirectory(Path path)
  {
    final Directory dir = new Directory();
    final Path fileName = path == null ? null : path.getFileName();
    dir.setName(fileName == null ? null : fileName.toString());
    return dir;
  }

  public void setRootDirectory(Directory dir, Path path)
  {
    volume.setRoot(dir);
    volume.setPath(FileSystemHelper.normalizePath(path.toString()));
  }
}
