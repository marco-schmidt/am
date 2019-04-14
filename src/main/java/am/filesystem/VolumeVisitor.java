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
package am.filesystem;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Stack;
import org.slf4j.LoggerFactory;
import am.app.AppConfig;
import am.model.Directory;
import am.model.File;

/**
 * Visit all files and directories in a volume, an abstraction for a directory tree.
 */
public class VolumeVisitor extends SimpleFileVisitor<Path>
{
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(VolumeScanner.class);
  private final VolumeScanner scanner;
  private final AppConfig config;
  private final Stack<Directory> dirStack;
  private long numDirectories;
  private long numFiles;
  private long numBytes;

  public VolumeVisitor(final VolumeScanner scanner, final AppConfig config)
  {
    dirStack = new Stack<Directory>();
    this.scanner = scanner;
    this.config = config;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException
  {
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug(config.msg("scanner.debug.enter", path.toAbsolutePath()));
    }
    Directory dir;
    if (dirStack.isEmpty())
    {
      dir = new Directory();
      dir.setName("");
      scanner.setRootDirectory(dir, path.toAbsolutePath());
    }
    else
    {
      dir = scanner.addDirectory(path);
      final Directory parent = dirStack.peek();
      parent.add(dir);
    }
    dirStack.push(dir);
    numDirectories++;
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
  {
    if (LOGGER.isTraceEnabled())
    {
      LOGGER.trace(config.msg("scanner.trace.file", file.toAbsolutePath()));
    }

    final Directory dir = dirStack.peek();
    if (dir != null)
    {
      final java.io.File fileSystemFile = file.toFile();
      final File model = new File();
      final long length = fileSystemFile.length();
      model.setByteSize(Long.valueOf(length));
      numBytes += length;
      model.setName(fileSystemFile.getName());
      model.setLastModified(new Date(fileSystemFile.lastModified()));
      dir.add(model);
    }
    numFiles++;
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
  {
    return super.visitFileFailed(file, exc);
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
  {
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug(config.msg("scanner.debug.exit", dir.toAbsolutePath()));
    }
    dirStack.pop();
    return FileVisitResult.CONTINUE;
  }

  public long getNumDirectories()
  {
    return numDirectories;
  }

  public long getNumFiles()
  {
    return numFiles;
  }

  public long getNumBytes()
  {
    return numBytes;
  }
}
