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
package am.db;

import am.filesystem.model.Directory;
import am.filesystem.model.File;
import am.filesystem.model.Volume;

/**
 * Search result consisting of {@link Volume}, {@link Directory} and {@link File}, some of those possibly null.
 *
 * @author Marco Schmidt
 */
public class SearchResult
{
  private Volume volume;
  private Directory directory;
  private File file;

  public void clear()
  {
    directory = null;
    file = null;
    volume = null;
  }

  public Volume getVolume()
  {
    return volume;
  }

  public void setVolume(Volume volume)
  {
    this.volume = volume;
  }

  public Directory getDirectory()
  {
    return directory;
  }

  public void setDirectory(Directory directory)
  {
    this.directory = directory;
  }

  public File getFile()
  {
    return file;
  }

  public void setFile(File file)
  {
    this.file = file;
  }
}
