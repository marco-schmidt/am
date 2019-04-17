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

import java.io.File;

/**
 * Helper routines to deal with the file system.
 *
 * @author Marco Schmidt
 */
public final class FileSystemHelper
{
  private FileSystemHelper()
  {
    // prevent instantiation
  }

  /**
   * Find file with highest (most recent) date of last modification in an array of {@link java.io.File} objects.
   *
   * @param files
   *          array of {@link File} objects, elements or array itself possibly null
   * @return {@link File} object with highest value for {@link java.io.File#lastModified()} or null
   */
  public static File findNewest(File... files)
  {
    if (files == null || files.length < 1)
    {
      return null;
    }

    // find first non-null element
    File max = null;
    int index = 0;
    do
    {
      max = files[index++];
    }
    while (max == null && index < files.length);
    if (max == null)
    {
      return null;
    }
    long lastMod = max.lastModified();

    // find maximum value for lastModified in the rest of the array
    while (index < files.length)
    {
      final File cand = files[index++];
      if (cand != null)
      {
        final long lastModCand = cand.lastModified();
        if (lastModCand > lastMod)
        {
          max = cand;
          lastMod = lastModCand;
        }
      }
    }
    return max;
  }
}
