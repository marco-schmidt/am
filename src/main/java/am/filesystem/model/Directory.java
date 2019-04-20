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
package am.filesystem.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data model class representing a directory as part of a {@link Volume}. Has a name, contains subdirectories and files.
 */
public class Directory
{
  private String name;
  private final List<Directory> subdirectories = new ArrayList<>();
  private final List<File> files = new ArrayList<>();
  private final Map<String, Directory> subdirectoryMap = new HashMap<>();
  private final Map<String, File> fileMap = new HashMap<>();

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public void add(final Directory d)
  {
    subdirectories.add(d);
    subdirectoryMap.put(d.getName(), d);
  }

  public void add(final File f)
  {
    files.add(f);
    fileMap.put(f.getName(), f);
  }

  public List<Directory> getSubdirectories()
  {
    return new ArrayList<>(subdirectories);
  }

  public List<File> getFiles()
  {
    return new ArrayList<>(files);
  }

  public Directory findOrCreateSubdirectory(String name)
  {
    Directory result = subdirectoryMap.get(name);
    if (result == null)
    {
      result = new Directory();
      result.setName(name);
      subdirectories.add(result);
      subdirectoryMap.put(name, result);
    }
    return result;
  }
}
